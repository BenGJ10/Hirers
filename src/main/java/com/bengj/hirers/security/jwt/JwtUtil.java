package com.bengj.hirers.security.jwt;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.entity.HirersUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final Environment env;
    private static final long EXPIRATION_TIME = 30L * 24 * 60 * 60 * 1000;

    /*
     * Generates a JWT token for the authenticated user.
    
     * @param authentication is The authentication object containing user details.
     * @return The generated JWT token.
     */
    public String generateToken(Authentication authentication){
        String token;

        // Get the secret key from the environment variables or use the default value
        String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
        
                // Create a SecretKey object from the secret key string
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // Fetch the user details from the authentication object
        HirersUser fetchedUser = (HirersUser) authentication.getPrincipal();
        if(fetchedUser == null){
            return null;
        }

        // Fetch the user's roles and convert them to a comma-separated string'
        String roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        // Build the JWT token using the Jwts builder
        token = Jwts.builder()
                .issuer("Hirers")
                .subject("")
                .claim("name", fetchedUser.getName())
                .claim("email", fetchedUser.getEmail())
                .claim("mobileNumber", fetchedUser.getMobileNumber())
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();

        return token;
    }
}
