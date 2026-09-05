package com.bengj.hirers.security.jwt;

import com.bengj.hirers.constant.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Qualifier("publicPaths")
    private final List<String> publicPaths;
    
    @Override
    /**
        * This method is called for each incoming HTTP request. It checks for the presence of a JWT token in the Authorization header,
          validates it, and sets the authentication in the security context if valid.
        * If the token is expired or invalid, it responds with an appropriate error message.
        
        * @param request The incoming HTTP request.
        * @param response The HTTP response to be sent back to the client.
        * @param filterChain The filter chain to pass the request and response to the next filter
        
        * @throws ServletException If an error occurs during request processing.
        * @throws IOException If an I/O error occurs during request processing.
     */
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
                                    throws ServletException, IOException {

        // Get the Authorization header from the request
        String authHeader = request.getHeader(ApplicationConstants.JWT_HEADER);
        if(authHeader != null){
            try {
                // Extract the JWT token from the Authorization header
                String jwt = authHeader.substring(7);
                Environment env = getEnvironment();
                
                // Get the secret key for JWT validation from the environment or use a default value
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY);

                // Create a SecretKey object using the secret key for HMAC SHA algorithm
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();
                
                // Extract the username and roles from the claims in the JWT token
                String username = String.valueOf(claims.get("email"));
                String roles = String.valueOf(claims.get("roles"));
                
                // Create an Authentication object with the extracted username and roles
                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(roles));
                
                // Set the authentication in the SecurityContext to indicate that the user is authenticated 
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Authentication successful");

            } catch (ExpiredJwtException exception) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token Expired");
                return;
            } catch (Exception e) {
                throw new BadCredentialsException("Invalid token ");
            }
        }
        // Continue the filter chain to allow the request to proceed to the next filter or the target resource
        filterChain.doFilter(request, response);
    }


    @Override
    /**
     * This method determines whether the filter should be applied to the incoming request. It checks if the request URI matches any of the public paths defined in the application.
     * If it does, the filter will not be applied, allowing public access to those endpoints.
     
     * @param request The incoming HTTP request.
     * @return true if the filter should not be applied (i.e., the request is for a public path), false otherwise.
     */
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return publicPaths.stream().anyMatch(publicPath -> pathMatcher.match(publicPath, path));
    }
}
