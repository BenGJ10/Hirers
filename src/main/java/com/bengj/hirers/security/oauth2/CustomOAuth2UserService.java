package com.bengj.hirers.security.oauth2;

import com.bengj.hirers.enums.AuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Override the loadUser method to handle different OAuth2 providers
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("google".equalsIgnoreCase(registrationId)) {
            return processGoogleUser(oAuth2User);
        } else if ("github".equalsIgnoreCase(registrationId)) {
            return processGithubUser(userRequest, oAuth2User);
        }

        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }

    // Method to process Google user information
    private OAuth2User processGoogleUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("picture");

        return new CustomOAuth2User(oAuth2User, AuthProvider.GOOGLE, providerId, email, name, avatarUrl);
    }

    // Method to process GitHub user information and fetch email if not provided
    private OAuth2User processGithubUser(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Object idObj = attributes.get("id");
        String providerId = idObj != null ? String.valueOf(idObj) : null;
        String name = (String) attributes.get("name");
        if (name == null || name.isBlank()) {
            name = (String) attributes.get("login");
        }
        String avatarUrl = (String) attributes.get("avatar_url");
        String email = (String) attributes.get("email");

        // If email is private on GitHub profile, query GitHub User Emails API
        if (email == null || email.isBlank()) {
            email = fetchGithubEmail(userRequest.getAccessToken().getTokenValue());
        }

        return new CustomOAuth2User(oAuth2User, AuthProvider.GITHUB, providerId, email, name, avatarUrl);
    }

    // Method to fetch verified email from GitHub API
    private String fetchGithubEmail(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.add("Accept", "application/vnd.github.v3+json");

            RequestEntity<Void> request = RequestEntity.get(URI.create("https://api.github.com/user/emails"))
                    .headers(headers)
                    .build();

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> emails = response.getBody();
            if (emails != null) {
                // Find primary and verified email
                for (Map<String, Object> emailObj : emails) {
                    Boolean primary = (Boolean) emailObj.get("primary");
                    Boolean verified = (Boolean) emailObj.get("verified");
                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return (String) emailObj.get("email");
                    }
                }
                // Fallback to any verified email
                for (Map<String, Object> emailObj : emails) {
                    Boolean verified = (Boolean) emailObj.get("verified");
                    if (Boolean.TRUE.equals(verified)) {
                        return (String) emailObj.get("email");
                    }
                }
                // Fallback to first email
                if (!emails.isEmpty()) {
                    return (String) emails.get(0).get("email");
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch verified email from GitHub API", e);
        }
        return null;
    }
}
