package com.bengj.hirers.security;

import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.repository.HirersUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class HirersAuthenticationProvider implements AuthenticationProvider {

    private final HirersUserRepository hirersUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    /**
     * Authenticates a user based on their email and password. 
     * Used instead of the default DaoAuthenticationProvider to allow for custom authentication logic.
     * This method retrieves the user from the database using their email, checks if the provided password matches the stored password hash,
     * and returns an authenticated user with their authorities if successful. If the user is not found or the password is incorrect, it throws an appropriate exception.
     *
     * @param authentication the authentication request containing the user's email and password
     * @return the authenticated user with their authorities
     * @throws AuthenticationException if the authentication fails
     */
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = Objects.requireNonNull(authentication.getCredentials()).toString();

        HirersUser user = hirersUserRepository.findUserByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getName()));

        if(passwordEncoder.matches(password, user.getPasswordHash())){
            return new UsernamePasswordAuthenticationToken(user, null, authorities);
        } else{
            throw new BadCredentialsException("Invalid password");
        }
    }

    
    /**
     * Indicates whether this authentication provider supports the specified authentication type.
     *
     * @param authentication the class of the authentication object
     * @return true if the authentication type is supported, false otherwise
     */
    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
