package com.bengj.hirers.repository;

import com.bengj.hirers.entity.EmailVerificationToken;
import com.bengj.hirers.entity.HirersUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByUser(HirersUser user);

    Optional<EmailVerificationToken> findByUserEmail(String email);

    Optional<EmailVerificationToken> findByUserEmailAndOtpCode(String email, String otpCode);

    void deleteByUser(HirersUser user);
}
