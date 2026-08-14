package com.bengj.hirers.repository;

import com.bengj.hirers.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}