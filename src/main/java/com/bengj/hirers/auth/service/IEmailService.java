package com.bengj.hirers.auth.service;

public interface IEmailService {

    /**
     * Sends an HTML verification email containing a 6-digit OTP code to the user.
     *
     * @param toEmail   Recipient email address
     * @param userName  Recipient full name
     * @param otpCode   6-digit one-time password
     */
    void sendVerificationOtpEmail(String toEmail, String userName, String otpCode);

    /**
     * Sends an HTML password reset email containing a 6-digit OTP code to the user.
     *
     * @param toEmail   Recipient email address
     * @param userName  Recipient full name
     * @param otpCode   6-digit one-time password
     */
    void sendPasswordResetOtpEmail(String toEmail, String userName, String otpCode);
}
