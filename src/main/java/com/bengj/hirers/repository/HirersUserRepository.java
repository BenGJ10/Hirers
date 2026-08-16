package com.bengj.hirers.repository;

import com.bengj.hirers.entity.HirersUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HirersUserRepository extends JpaRepository<HirersUser, Long> {
}