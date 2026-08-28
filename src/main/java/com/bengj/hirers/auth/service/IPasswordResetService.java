package com.bengj.hirers.auth.service;

public interface IPasswordResetService {

    /**
     * Generates a 6-digit password reset OTP and sends it to the user's email.
     *
     * @param email The registered email address
     */
    void sendPasswordResetOtp(String email);

    /**
     * Verifies the 6-digit OTP and updates the user's password.
     *
     * @param email       The user's email address
     * @param otpCode     The 6-digit reset code
     * @param newPassword The new plaintext password to encode and save
     */
    void resetPassword(String email, String otpCode, String newPassword);
}
