package com.bengj.hirers.security.oauth2;

import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Role;
import com.bengj.hirers.enums.AuthProvider;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.RoleRepository;
import com.bengj.hirers.security.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final HirersUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String defaultRedirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to frontend OAuth callback.");
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2User oAuth2User)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        String email = null;
        String name = null;
        String providerId = null;
        AuthProvider provider = AuthProvider.LOCAL;

        if (principal instanceof CustomOAuth2User customUser) {
            email = customUser.getEmail();
            name = customUser.getName();
            providerId = customUser.getProviderId();
            provider = customUser.getAuthProvider();
        } else {
            email = (String) oAuth2User.getAttributes().get("email");
            name = (String) oAuth2User.getAttributes().get("name");
            Object sub = oAuth2User.getAttributes().get("sub");
            providerId = sub != null ? String.valueOf(sub) : String.valueOf(oAuth2User.getAttributes().get("id"));
        }

        if (email == null || email.isBlank()) {
            log.error("OAuth2 user has no verified email address available");
            String errorUrl = UriComponentsBuilder.fromUriString(defaultRedirectUri)
                    .queryParam("error", "Email not provided by OAuth provider")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        // Find or create HirersUser
        HirersUser user = processUserAccount(email, name, provider, providerId);

        // Build internal Authentication Token for JWT generation
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getName()));
        Authentication jwtAuth = new UsernamePasswordAuthenticationToken(user, null, authorities);
        String token = jwtUtil.generateToken(jwtAuth);

        // Redirect to Frontend OAuth2 callback with token
        String targetUrl = UriComponentsBuilder.fromUriString(defaultRedirectUri)
                .queryParam("token", URLEncoder.encode(token, StandardCharsets.UTF_8))
                .build().toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private HirersUser processUserAccount(String email, String name, AuthProvider provider, String providerId) {
        Optional<HirersUser> existingUserOpt = userRepository.findUserByEmail(email);

        if (existingUserOpt.isPresent()) {
            HirersUser existingUser = existingUserOpt.get();
            // Account conversion / linking: switch provider and clear local password hash
            existingUser.setAuthProvider(provider);
            existingUser.setProviderId(providerId);
            existingUser.setPasswordHash(null);
            existingUser.setEmailVerified(true);
            return userRepository.save(existingUser);
        }

        // Create new HirersUser
        Role jobSeekerRole = roleRepository.findRoleByName("ROLE_JOB_SEEKER")
                .orElseThrow(() -> new RuntimeException("Default ROLE_JOB_SEEKER not found"));

        HirersUser newUser = new HirersUser();
        newUser.setEmail(email);
        newUser.setName(name != null && !name.isBlank() ? name : email.split("@")[0]);
        newUser.setPasswordHash(null); // OAuth users don't have local password
        newUser.setAuthProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setRole(jobSeekerRole);
        newUser.setEmailVerified(true);

        HirersUser savedUser = userRepository.save(newUser);
        return savedUser;
    }
}
