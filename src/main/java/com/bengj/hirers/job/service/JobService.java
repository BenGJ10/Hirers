package com.bengj.hirers.job.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Job;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.JobRepository;
import com.bengj.hirers.util.ApplicationUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService implements IJobService{

    private final JobRepository jobRepository;
    private final HirersUserRepository userRepository;

    @Override
    public List<JobDto> getEmployerJobs(String employerEmail){
        HirersUser employer = userRepository.findUserByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        if(employer.getCompany() == null){
            throw new RuntimeException("Employer does not have a company assigned");
        }

        List<Job> jobs = employer.getCompany().getJobs();
        return jobs.stream()
                .map(ApplicationUtility::transformJobToDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public JobDto updateJobStatus(Long jobId, String status, String employerEmail) {
        // Validate the status
        if (!status.equals(ApplicationConstants.JOB_STATUS_ACTIVE)
                && !status.equals(ApplicationConstants.JOB_STATUS_CLOSED)
                && !status.equals(ApplicationConstants.JOB_STATUS_DRAFT)) {
            throw new RuntimeException("Invalid status. Must be ACTIVE, CLOSED, or DRAFT");
        }

        // Find the user by email
        HirersUser employer = userRepository.findUserByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        if (employer.getCompany() == null) {
            throw new RuntimeException("Employer does not have a company assigned");
        }

        Job job = employer.getCompany().getJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        return ApplicationUtility.transformJobToDto(job);
    }

    @Override
    @Transactional
    public JobDto createJob(JobDto jobDto, String employerEmail){
        HirersUser employer = userRepository.findUserByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        if(employer.getCompany() == null){
            throw new RuntimeException("Employer does not have a company assigned");
        }

        Job job = transformDtoToEntity(jobDto);
        job.setPostedDate(Instant.now());
        job.setApplicationsCount(0);
        job.setStatus(ApplicationConstants.JOB_STATUS_DRAFT);
        job.setCompany(employer.getCompany());

        Job savedJob = jobRepository.save(job);
        return ApplicationUtility.transformJobToDto(jobRepository.save(savedJob));
    }


    private Job transformDtoToEntity(JobDto jobDto) {
        Job job = new Job();
        BeanUtils.copyProperties(jobDto, job);
        return job;
    }
}
