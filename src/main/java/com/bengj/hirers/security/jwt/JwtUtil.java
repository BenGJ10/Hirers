package com.bengj.hirers.security.jwt;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.entity.HirersUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@PropertySource(value = "classpath:jwt.properties")
public class JwtUtil {

    private final Environment env;

    @Value("${jwt.issuer:Hirers}")
    private String jwtIssuer;

    @Value("${jwt.subject:Hirers Json Web Token}")
    private String jwtSubject;

    @Value("${jwt.expiration.hours:1}")
    private int jwtExpirationHours;

    @Value("${jwt.prod.expiration.hours:1}")
    private int jwtProdExpirationHours;

    /**
     * Generates a JWT token for the authenticated user.
    
     * @param authentication is The authentication object containing user details.
     * @return The generated JWT token.
     */
    public String generateToken(Authentication authentication){
        String token;

        int expirationHours = jwtExpirationHours;
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if(profiles.contains("prod")){
            expirationHours = jwtProdExpirationHours;
        }

        // Get the secret key from the environment variables or use the default value
        String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY);
        
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
                .issuer(jwtIssuer)
                .subject(jwtSubject)
                .claim("userId", fetchedUser.getId())
                .claim("name", fetchedUser.getName())
                .claim("email", fetchedUser.getEmail())
                .claim("mobileNumber", fetchedUser.getMobileNumber())
                .claim("roles", roles)
                .claim("authProvider", fetchedUser.getAuthProvider() != null ? fetchedUser.getAuthProvider().name() : "LOCAL")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (long) expirationHours * 60 * 60 * 1000))
                .signWith(secretKey)
                .compact();

        return token;
    }
}
