package com.bengj.hirers.repository;

import com.bengj.hirers.entity.HirersUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HirersUserRepository extends JpaRepository<HirersUser, Long> {
    Optional<HirersUser> readUserByEmailOrMobileNumber(String email, String mobileNumber);

    Optional<HirersUser> findUserByEmail(String email);

    Page<HirersUser> findAllByIsActiveTrue(Pageable pageable);
}