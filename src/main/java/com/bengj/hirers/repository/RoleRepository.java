package com.bengj.hirers.repository;

import com.bengj.hirers.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}