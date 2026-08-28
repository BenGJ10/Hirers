package com.bengj.hirers.auth.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.entity.EmailVerificationToken;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.repository.EmailVerificationTokenRepository;
import com.bengj.hirers.repository.HirersUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService implements IVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final HirersUserRepository userRepository;
    private final IEmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    // Method to create and send a verification OTP to the user's email
    @Override
    @Transactional
    public String createAndSendVerificationOtp(HirersUser user) {
        // Generate a secure OTP and set its expiry date
        String otpCode = generateSecureOtpCode();
        Instant expiryDate = Instant.now().plus(ApplicationConstants.OTP_VALIDITY_MINUTES, ChronoUnit.MINUTES);

        // Remove or update any existing token for this user
        Optional<EmailVerificationToken> existingTokenOpt = tokenRepository.findByUserEmail(user.getEmail());
        EmailVerificationToken token;
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            token.setOtpCode(otpCode);
            token.setExpiryDate(expiryDate);
        } else {
            token = new EmailVerificationToken(user, otpCode, expiryDate);
        }

        // Save the token and flush to ensure it's persisted before sending the email
        tokenRepository.saveAndFlush(token);
        log.info("Saved verification OTP {} for user {} (ID: {})", otpCode, user.getEmail(), user.getId());

        // Dispatch async email
        emailService.sendVerificationOtpEmail(user.getEmail(), user.getName(), otpCode);

        return otpCode;
    }

    // Method to verify the OTP provided by the user for email verification
    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        HirersUser user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Check if the user is already verified
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            log.info("User {} is already verified", email);
            return true;
        }

        // Fetch the token and validate it
        EmailVerificationToken token = tokenRepository.findByUserEmailAndOtpCode(email, otpCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code. Please check and try again."));

        if (token.isExpired()) {
            tokenRepository.delete(token);
            throw new IllegalStateException("Verification code has expired. Please request a new code.");
        }

        // Mark user verified
        user.setEmailVerified(true);
        userRepository.save(user);

        // Clean up token
        tokenRepository.delete(token);
        log.info("User {} successfully verified their email address", email);

        return true;
    }

    // Method to resend a new verification OTP to the user's email
    @Override
    @Transactional
    public void resendOtp(String email) {
        HirersUser user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account registered with email: " + email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Your email is already verified. You can log in directly.");
        }

        createAndSendVerificationOtp(user);
        log.info("Resent fresh verification OTP to {}", email);
    }

    // Generates a 6-digit cryptographically secure numeric OTP string (100000 - 999999)
    private String generateSecureOtpCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}
