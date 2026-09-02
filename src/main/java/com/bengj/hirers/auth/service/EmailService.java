package com.bengj.hirers.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:gjdevelopment1015@gmail.com}")
    private String fromEmail;

    @Override
    public void sendVerificationOtpEmail(String toEmail, String userName, String otpCode) {
        try {
            log.info("Preparing verification email for user: {} ({})", userName, toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail, "Hirers");
            helper.setTo(toEmail);
            helper.setSubject(otpCode + " is your Hirers verification code");

            String htmlBody = buildOtpHtmlTemplate(userName, otpCode);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Verification OTP email successfully dispatched to {}", toEmail);
        } catch (MessagingException e) {
            log.error("MessagingException while sending email to {}: {}", toEmail, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during email dispatch to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendPasswordResetOtpEmail(String toEmail, String userName, String otpCode) {
        try {
            log.info("Preparing password reset email for user: {} ({})", userName, toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail, "Hirers");
            helper.setTo(toEmail);
            helper.setSubject(otpCode + " is your Hirers password reset code");

            String htmlBody = buildPasswordResetOtpHtmlTemplate(userName, otpCode);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Password reset OTP email successfully dispatched to {}", toEmail);
        } catch (MessagingException e) {
            log.error("MessagingException while sending password reset email to {}: {}", toEmail, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during password reset email dispatch to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Builds a modern, clean HTML password reset email matching the Hirers design system.
     */
    private String buildPasswordResetOtpHtmlTemplate(String userName, String otpCode) {
        String safeName = (userName != null && !userName.isBlank()) ? userName : "there";

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Reset your Hirers password</title>
          <style>
            body {
              margin: 0;
              padding: 0;
              background-color: #f4f2ef;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
              color: #111827;
              -webkit-font-smoothing: antialiased;
            }
            .wrapper {
              width: 100%%;
              background-color: #f4f2ef;
              padding: 40px 16px;
            }
            .card {
              max-width: 500px;
              margin: 0 auto;
              background: #ffffff;
              border-radius: 24px;
              border: 1px solid #e5e2dd;
              box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.04);
              overflow: hidden;
            }
            .header-bar {
              padding: 28px 32px 20px;
              border-bottom: 1px solid #f1f0ee;
            }
            .logo-table {
              border-collapse: collapse;
            }
            .logo-text {
              font-size: 20px;
              font-weight: 800;
              color: #111827;
              letter-spacing: -0.5px;
              line-height: 1.1;
            }
            .logo-slogan {
              font-size: 9px;
              font-weight: 700;
              color: #4F46E5;
              letter-spacing: 1.5px;
              text-transform: uppercase;
              margin-top: 3px;
            }
            .body-content {
              padding: 32px 32px 28px;
            }
            .heading {
              font-size: 22px;
              font-weight: 800;
              color: #111827;
              letter-spacing: -0.5px;
              margin: 0 0 12px 0;
            }
            .text {
              font-size: 14px;
              line-height: 1.6;
              color: #4b5563;
              margin: 0 0 24px 0;
            }
            .otp-container {
              background-color: #fbfbfb;
              border: 1px solid #e5e2dd;
              border-radius: 18px;
              padding: 24px 20px;
              text-align: center;
              margin: 0 0 24px 0;
            }
            .otp-label {
              font-size: 10px;
              font-weight: 800;
              text-transform: uppercase;
              letter-spacing: 1.2px;
              color: #6b7280;
              margin-bottom: 8px;
            }
            .otp-number {
              font-size: 36px;
              font-weight: 900;
              letter-spacing: 8px;
              color: #111827;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, monospace;
              padding-left: 8px;
            }
            .expiry-pill {
              display: inline-block;
              margin-top: 10px;
              padding: 4px 12px;
              background-color: #f4f2ef;
              border: 1px solid #e5e2dd;
              border-radius: 20px;
              font-size: 11px;
              font-weight: 700;
              color: #4b5563;
            }
            .security-text {
              font-size: 12px;
              line-height: 1.5;
              color: #9ca3af;
              border-top: 1px solid #f3f4f6;
              padding-top: 20px;
              margin: 0;
            }
            .footer-bar {
              background-color: #faf9f8;
              border-top: 1px solid #f1f0ee;
              padding: 20px 32px;
              text-align: center;
              font-size: 11px;
              color: #9ca3af;
              line-height: 1.6;
            }
            .footer-bar a {
              color: #4f46e5;
              text-decoration: none;
              font-weight: 600;
            }
          </style>
        </head>
        <body>
          <div class="wrapper">
            <div class="card">
              <!-- Top Brand Header with Official Hirers Logo -->
              <div class="header-bar">
                <table class="logo-table" cellpadding="0" cellspacing="0" border="0">
                  <tr>
                    <td style="vertical-align: middle; padding-right: 12px;">
                      <!-- SVG Logo Mark (36x36) with #2F3FAE background and connected H shape -->
                      <svg width="36" height="36" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" style="display: block; border-radius: 9px;">
                        <rect x="0" y="0" width="40" height="40" rx="10" fill="#2F3FAE"/>
                        <path d="M10 10 L13 11.8 C13.65 12.2 14 12.9 14 13.65 V18.4 C17.8 17.65 20.8 18.55 23.7 20.25 C24.75 20.9 25.5 21.35 26 21.55 V13.65 C26 12.9 26.35 12.2 27 11.8 L30 10 V30 L27 28.2 C26.35 27.8 26 27.1 26 26.35 V23.35 C22.8 22.8 20.5 21.75 17.55 20.05 C16.25 19.3 15.1 18.95 14 19.1 V26.35 C14 27.1 13.65 27.8 13 28.2 L10 30 V10 Z" fill="#FFFFFF"/>
                      </svg>
                    </td>
                    <td style="vertical-align: middle;">
                      <div class="logo-text">Hirers</div>
                      <div class="logo-slogan">Find &bull; Build &bull; Grow</div>
                    </td>
                  </tr>
                </table>
              </div>

              <!-- Main Content -->
              <div class="body-content">
                <h1 class="heading">Password Reset Request</h1>
                <p class="text">
                  Hi <strong>%s</strong>, we received a request to reset your Hirers password. Use the 6-digit verification code below to set a new password.
                </p>

                <!-- Clean OTP Box -->
                <div class="otp-container">
                  <div class="otp-label">Password Reset Code</div>
                  <div class="otp-number">%s</div>
                  <div>
                    <span class="expiry-pill">⏱️ Valid for 15 minutes</span>
                  </div>
                </div>

                <p class="security-text">
                  <strong>Security Note:</strong> If you did not request a password reset, you can safely ignore this email. Your password will remain unchanged.
                </p>
              </div>

              <!-- Footer -->
              <div class="footer-bar">
                &copy; 2026 Hirers Platform &bull; Empowering Careers & Recruitment<br>
                Need assistance? Contact <a href="mailto:gjdevelopment1015@gmail.com">gjdevelopment1015@gmail.com</a>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.formatted(safeName, otpCode);
    }

    /**
     * Builds a modern, clean HTML email template matching the Hirers webpage design system.
     */
    private String buildOtpHtmlTemplate(String userName, String otpCode) {
        String safeName = (userName != null && !userName.isBlank()) ? userName : "there";

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Verify your Hirers account</title>
          <style>
            body {
              margin: 0;
              padding: 0;
              background-color: #f4f2ef;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
              color: #111827;
              -webkit-font-smoothing: antialiased;
            }
            .wrapper {
              width: 100%%;
              background-color: #f4f2ef;
              padding: 40px 16px;
            }
            .card {
              max-width: 500px;
              margin: 0 auto;
              background: #ffffff;
              border-radius: 24px;
              border: 1px solid #e5e2dd;
              box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.04);
              overflow: hidden;
            }
            .header-bar {
              padding: 28px 32px 20px;
              border-bottom: 1px solid #f1f0ee;
            }
            .logo-table {
              border-collapse: collapse;
            }
            .logo-text {
              font-size: 20px;
              font-weight: 800;
              color: #111827;
              letter-spacing: -0.5px;
              line-height: 1.1;
            }
            .logo-slogan {
              font-size: 9px;
              font-weight: 700;
              color: #4F46E5;
              letter-spacing: 1.5px;
              text-transform: uppercase;
              margin-top: 3px;
            }
            .body-content {
              padding: 32px 32px 28px;
            }
            .heading {
              font-size: 22px;
              font-weight: 800;
              color: #111827;
              letter-spacing: -0.5px;
              margin: 0 0 12px 0;
            }
            .text {
              font-size: 14px;
              line-height: 1.6;
              color: #4b5563;
              margin: 0 0 24px 0;
            }
            .otp-container {
              background-color: #fbfbfb;
              border: 1px solid #e5e2dd;
              border-radius: 18px;
              padding: 24px 20px;
              text-align: center;
              margin: 0 0 24px 0;
            }
            .otp-label {
              font-size: 10px;
              font-weight: 800;
              text-transform: uppercase;
              letter-spacing: 1.2px;
              color: #6b7280;
              margin-bottom: 8px;
            }
            .otp-number {
              font-size: 36px;
              font-weight: 900;
              letter-spacing: 8px;
              color: #111827;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, monospace;
              padding-left: 8px;
            }
            .expiry-pill {
              display: inline-block;
              margin-top: 10px;
              padding: 4px 12px;
              background-color: #f4f2ef;
              border: 1px solid #e5e2dd;
              border-radius: 20px;
              font-size: 11px;
              font-weight: 700;
              color: #4b5563;
            }
            .security-text {
              font-size: 12px;
              line-height: 1.5;
              color: #9ca3af;
              border-top: 1px solid #f3f4f6;
              padding-top: 20px;
              margin: 0;
            }
            .footer-bar {
              background-color: #faf9f8;
              border-top: 1px solid #f1f0ee;
              padding: 20px 32px;
              text-align: center;
              font-size: 11px;
              color: #9ca3af;
              line-height: 1.6;
            }
            .footer-bar a {
              color: #4f46e5;
              text-decoration: none;
              font-weight: 600;
            }
          </style>
        </head>
        <body>
          <div class="wrapper">
            <div class="card">
              <!-- Top Brand Header with Official Hirers Logo -->
              <div class="header-bar">
                <table class="logo-table" cellpadding="0" cellspacing="0" border="0">
                  <tr>
                    <td style="vertical-align: middle; padding-right: 12px;">
                      <!-- SVG Logo Mark (36x36) with #2F3FAE background and connected H shape -->
                      <svg width="36" height="36" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" style="display: block; border-radius: 9px;">
                        <rect x="0" y="0" width="40" height="40" rx="10" fill="#2F3FAE"/>
                        <path d="M10 10 L13 11.8 C13.65 12.2 14 12.9 14 13.65 V18.4 C17.8 17.65 20.8 18.55 23.7 20.25 C24.75 20.9 25.5 21.35 26 21.55 V13.65 C26 12.9 26.35 12.2 27 11.8 L30 10 V30 L27 28.2 C26.35 27.8 26 27.1 26 26.35 V23.35 C22.8 22.8 20.5 21.75 17.55 20.05 C16.25 19.3 15.1 18.95 14 19.1 V26.35 C14 27.1 13.65 27.8 13 28.2 L10 30 V10 Z" fill="#FFFFFF"/>
                      </svg>
                    </td>
                    <td style="vertical-align: middle;">
                      <div class="logo-text">Hirers</div>
                      <div class="logo-slogan">Find &bull; Build &bull; Grow</div>
                    </td>
                  </tr>
                </table>
              </div>

              <!-- Main Content -->
              <div class="body-content">
                <h1 class="heading">Verify your email address</h1>
                <p class="text">
                  Hi <strong>%s</strong>, welcome to Hirers. Use the 6-digit verification code below to verify your email address and activate your account.
                </p>

                <!-- Clean OTP Box -->
                <div class="otp-container">
                  <div class="otp-label">Verification Code</div>
                  <div class="otp-number">%s</div>
                  <div>
                    <span class="expiry-pill">⏱️ Valid for 15 minutes</span>
                  </div>
                </div>

                <p class="security-text">
                  <strong>Security Note:</strong> If you did not create an account on Hirers, you can safely ignore this email. Never share this code with anyone.
                </p>
              </div>

              <!-- Footer -->
              <div class="footer-bar">
                &copy; 2026 Hirers Platform &bull; Empowering Careers & Recruitment<br>
                Need assistance? Contact <a href="mailto:gjdevelopment1015@gmail.com">gjdevelopment1015@gmail.com</a>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.formatted(safeName, otpCode);
    }
}
