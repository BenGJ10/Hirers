package com.bengj.hirers.auth.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.PasswordResetToken;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements IPasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final HirersUserRepository userRepository;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void sendPasswordResetOtp(String email) {
        String cleanEmail = email.trim().toLowerCase();
        Optional<HirersUser> userOpt = userRepository.findUserByEmail(cleanEmail);

        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", cleanEmail);
            // Return silently so as not to reveal whether the account exists
            return;
        }

        HirersUser user = userOpt.get();
        String otpCode = generateSecureOtpCode();
        Instant expiryDate = Instant.now().plus(ApplicationConstants.OTP_VALIDITY_MINUTES, ChronoUnit.MINUTES);

        // Update or insert reset token
        Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUserEmail(cleanEmail);
        PasswordResetToken token;
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            token.setOtpCode(otpCode);
            token.setExpiryDate(expiryDate);
        } else {
            token = new PasswordResetToken(user, otpCode, expiryDate);
        }

        tokenRepository.saveAndFlush(token);
        log.info("Generated password reset OTP for user {} (ID: {})", cleanEmail, user.getId());

        // Send branded password reset email
        emailService.sendPasswordResetOtpEmail(user.getEmail(), user.getName(), otpCode);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword) {
        String cleanEmail = email.trim().toLowerCase();
        String cleanOtp = otpCode.trim();

        HirersUser user = userRepository.findUserByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + cleanEmail));

        PasswordResetToken token = tokenRepository.findByUserEmailAndOtpCode(cleanEmail, cleanOtp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset code. Please check and try again."));

        if (token.isExpired()) {
            tokenRepository.delete(token);
            throw new IllegalStateException("The password reset code has expired. Please request a new code.");
        }

        // Check if the new password is compromised
        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(newPassword);
        if (decision.isCompromised()) {
            throw new IllegalArgumentException("The chosen password has been exposed in a data breach. Please choose a stronger password.");
        }

        // Update user password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);

        // Delete used token
        tokenRepository.delete(token);
        log.info("Successfully reset password for user {}", cleanEmail);
    }

    // Generates a 6-digit cryptographically secure numeric OTP string (100000 - 999999)
    private String generateSecureOtpCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}
