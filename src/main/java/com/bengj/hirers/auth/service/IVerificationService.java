package com.bengj.hirers.auth.service;

import com.bengj.hirers.entity.HirersUser;

public interface IVerificationService {

    /**
     * Generates and saves a 6-digit OTP token for the given user, then dispatches the email.
     *
     * @param user The user needing verification
     * @return The 6-digit OTP code generated
     */
    String createAndSendVerificationOtp(HirersUser user);

    /**
     * Verifies the provided 6-digit OTP for the given email.
     * If valid and not expired, marks user.emailVerified = true and removes the token.
     *
     * @param email   User's email
     * @param otpCode 6-digit OTP
     * @return true if successfully verified
     */
    boolean verifyOtp(String email, String otpCode);

    /**
     * Resends a fresh 6-digit OTP to the user's email if the account is unverified.
     *
     * @param email User's email
     */
    void resendOtp(String email);
}
