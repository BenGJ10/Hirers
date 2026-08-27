package com.bengj.hirers.repository;

import com.bengj.hirers.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    void deleteByUserIdAndJobId(Long userId, Long jobId);

    List<JobApplication> findByUserIdOrderByAppliedAtDesc(Long userId);

    List<JobApplication> findByJobIdOrderByAppliedAtAsc(Long jobId);

    @Modifying
    int updateStatusAndNotesById(@Param("status") String status, @Param("notes") String notes,
                                 @Param("id") Long id, @Param("updatedBy") String updatedBy);
}