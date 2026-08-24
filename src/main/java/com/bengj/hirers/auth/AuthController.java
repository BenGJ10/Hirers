package com.bengj.hirers.auth;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.LoginRequestDto;
import com.bengj.hirers.dto.LoginResponseDto;
import com.bengj.hirers.dto.RegisterRequestDto;
import com.bengj.hirers.dto.UserDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Role;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.RoleRepository;
import com.bengj.hirers.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HirersUserRepository hirersUserRepository;
    private final AuthenticationManager authenticationManager;
    private final CompromisedPasswordChecker compromisedPasswordChecker;

    /**
     * Handles user login requests. It authenticates the user using the provided credentials, generates a JWT token upon successful authentication,
     * and returns a response containing the token and user details. If authentication fails, it returns an appropriate error response.
     *
     * @param loginRequestDto The login request containing the user's email and password.
     * @return A ResponseEntity containing the login response or an error message.
     */
    @PostMapping(value = "/login/public", version = "1.0")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        try{
            // Authenticate the user using the provided credentials
            Authentication authenticationResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.username(), loginRequestDto.password()));
            
            // Generate a JWT token for the authenticated user
            String jwtToken = jwtUtil.generateToken(authenticationResult);
            
            // Create a UserDto object to include in the response (you can populate it with user details as needed)
            UserDto userDto = new UserDto();
            
            // Copy properties from the authenticated user to the UserDto
            HirersUser loggedInUser = (HirersUser) authenticationResult.getPrincipal();
            BeanUtils.copyProperties(loggedInUser, userDto);
            
            // Set additional properties in the UserDto if needed
            userDto.setRole(loggedInUser.getRole().getName());
            userDto.setUserId(loggedInUser.getId());
            userDto.setCompanyId(loggedInUser.getCompany() != null ? loggedInUser.getCompany().getId() : null);
            userDto.setCompanyName(loggedInUser.getCompany() != null ? loggedInUser.getCompany().getName() : null);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new LoginResponseDto(HttpStatus.OK.getReasonPhrase(),
                            userDto, jwtToken));

        } catch (BadCredentialsException ex) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                    "Invalid username or password");
        } catch (AuthenticationException ex) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                    "Authentication failed");
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred");
        }
    }

    
    /**
     * Handles user registration requests. It creates a new user in the system with the provided details, encodes the password, assigns a default role, and saves the user to the database.
     * If the registration is successful, it returns a success response; otherwise, it returns an appropriate error response.
     * 
     * @param registerRequestDto The registration request containing user details.
     * @return A ResponseEntity indicating the result of the registration process.
     */
    @PostMapping(value = "/register/public", version = "1.0")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequestDto){

        HirersUser user = new HirersUser();
        BeanUtils.copyProperties(registerRequestDto, user);

        // Encode the user's password and set the deaf before saving the user to the database
        user.setPasswordHash(passwordEncoder.encode(registerRequestDto.password()));
        Role role = roleRepository.findRoleByName(ApplicationConstants.ROLE_JOB_SEEKER).orElseThrow(
                () -> new IllegalStateException("Role not found: " + ApplicationConstants.ROLE_JOB_SEEKER));
        user.setRole(role);

        // Save the new user to the database and return a success response
        hirersUserRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }   

    
    // Custom method to build error responses for login and registration endpoints
    private ResponseEntity<LoginResponseDto> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new LoginResponseDto(message, null, null));
    }
}
