package com.bengj.hirers.repository;

import com.bengj.hirers.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
