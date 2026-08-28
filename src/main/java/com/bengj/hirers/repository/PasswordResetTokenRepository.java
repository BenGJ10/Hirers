package com.bengj.hirers.repository;

import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUser(HirersUser user);

    Optional<PasswordResetToken> findByUserEmail(String email);

    Optional<PasswordResetToken> findByUserEmailAndOtpCode(String email, String otpCode);

    void deleteByUser(HirersUser user);
}
