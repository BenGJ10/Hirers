package com.bengj.hirers.auth.controller;

import com.bengj.hirers.auth.service.IPasswordResetService;
import com.bengj.hirers.auth.service.IVerificationService;
import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.ForgotPasswordRequestDto;
import com.bengj.hirers.dto.LoginRequestDto;
import com.bengj.hirers.dto.LoginResponseDto;
import com.bengj.hirers.dto.RegisterRequestDto;
import com.bengj.hirers.dto.ResendOtpRequestDto;
import com.bengj.hirers.dto.ResetPasswordRequestDto;
import com.bengj.hirers.dto.UserDto;
import com.bengj.hirers.dto.VerifyEmailRequestDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Role;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.RoleRepository;
import com.bengj.hirers.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
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
    private final IVerificationService verificationService;
    private final IPasswordResetService passwordResetService;

    /**
     * Handles user login requests. It authenticates the user using the provided credentials, generates a JWT token upon successful authentication,
     * and returns a response containing the token and user details. If authentication fails, it returns an appropriate error response.
     *
     * @param loginRequestDto The login request containing the user's email and password.
     * @return A ResponseEntity containing the login response or an error message.
     */
    @PostMapping(path = "/login/public", version = "1.0")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        try{
            // Authenticate the user using the provided credentials
            Authentication authenticationResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.username(), loginRequestDto.password()));
            
            // Generate a JWT token for the authenticated user
            String jwtToken = jwtUtil.generateToken(authenticationResult);
            
            // Create a UserDto object to include in the response
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

        } catch (DisabledException ex) {
            return buildErrorResponse(HttpStatus.FORBIDDEN,
                    ex.getMessage());
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
     * Handles user registration requests. It creates a new user in the system with emailVerified = false,
     * encodes the password, assigns a default role, saves and flushes the user, and sends a 6-digit verification OTP email.
     * 
     * @param registerRequestDto The registration request containing user details.
     * @return A ResponseEntity indicating the result of the registration process.
     */
    @Transactional
    @PostMapping(path = "/register/public", version = "1.0")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequestDto){
        try {
            if (hirersUserRepository.findUserByEmail(registerRequestDto.email()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "An account with this email address already exists."));
            }

            HirersUser user = new HirersUser();
            user.setName(registerRequestDto.name().trim());
            user.setEmail(registerRequestDto.email().trim().toLowerCase());
            user.setMobileNumber(registerRequestDto.mobileNumber() != null ? registerRequestDto.mobileNumber().trim() : null);
            user.setEmailVerified(false);

            // Encode the user's password and set role
            user.setPasswordHash(passwordEncoder.encode(registerRequestDto.password()));
            Role role = roleRepository.findRoleByName(ApplicationConstants.ROLE_JOB_SEEKER).orElseThrow(
                    () -> new IllegalStateException("Role not found: " + ApplicationConstants.ROLE_JOB_SEEKER));
            user.setRole(role);

            // Save and flush the new user to the database so the ID is immediately available
            HirersUser savedUser = hirersUserRepository.saveAndFlush(user);

            // Generate and send 6-digit OTP email
            verificationService.createAndSendVerificationOtp(savedUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Registration successful. Please enter the 6-digit verification code sent to your email.",
                    "email", savedUser.getEmail()
            ));
        } catch (Exception e) {
            log.error("Error during registration for {}: {}", registerRequestDto.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Registration failed. Please try again."));
        }
    }

    /**
     * Verifies a user's email using the 6-digit OTP code sent to their inbox.
     *
     * @param requestDto Contains user email and 6-digit OTP
     * @return Success message or validation error
     */
    @PostMapping(path = "/verify-email/public", version = "1.0")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyEmailRequestDto requestDto) {
        try {
            verificationService.verifyOtp(requestDto.email(), requestDto.otpCode());
            return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully! You can now log in."
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Verification failed. Please try again."));
        }
    }

    /**
     * Resends a fresh 6-digit OTP to the user's email address if still unverified.
     *
     * @param requestDto Contains user email
     * @return Confirmation message
     */
    @PostMapping(path = "/resend-otp/public", version = "1.0")
    public ResponseEntity<?> resendOtp(@RequestBody @Valid ResendOtpRequestDto requestDto) {
        try {
            verificationService.resendOtp(requestDto.email());
            return ResponseEntity.ok(Map.of(
                    "message", "A fresh 6-digit verification code has been dispatched to your email."
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to resend code. Please try again."));
        }
    }

    /**
     * Initiates a password reset flow by sending a 6-digit OTP to the user's email.
     *
     * @param requestDto Contains user email
     * @return Confirmation message
     */
    @PostMapping(path = "/forgot-password/public", version = "1.0")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto requestDto) {
        try {
            passwordResetService.sendPasswordResetOtp(requestDto.email());
            return ResponseEntity.ok(Map.of(
                    "message", "If an account with that email exists, a password reset code has been sent."
            ));
        } catch (Exception e) {
            log.error("Error in forgot-password for {}: {}", requestDto.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to process request. Please try again."));
        }
    }

    /**
     * Verifies the 6-digit OTP and resets the user's password.
     *
     * @param requestDto Contains user email, 6-digit OTP, and a new password
     * @return Confirmation message
     */
    @PostMapping(path = "/reset-password/public", version = "1.0")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequestDto requestDto) {
        try {
            passwordResetService.resetPassword(requestDto.email(), requestDto.otpCode(), requestDto.newPassword());
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully! You can now log in with your new password."
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error in reset-password for {}: {}", requestDto.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to reset password. Please try again."));
        }
    }
    
    // Custom method to build error responses for login and registration endpoints
    private ResponseEntity<LoginResponseDto> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new LoginResponseDto(message, null, null));
    }
}
