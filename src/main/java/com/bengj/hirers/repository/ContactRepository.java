package com.bengj.hirers.repository;

import com.bengj.hirers.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findContactsByStatus(String status);
}