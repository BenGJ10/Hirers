package com.bengj.hirers.job.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.JobApplicationDto;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.dto.UpdateJobApplicationDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Job;
import com.bengj.hirers.entity.JobApplication;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.JobApplicationRepository;
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
    private final JobApplicationRepository jobApplicationRepository;


    // Method to get jobs for a specific employer by their email
    @Override
    public List<JobDto> getEmployerJobs(String employerEmail){
        // Find the employer by email
        HirersUser employer = userRepository.findUserByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        // Check if the employer has a company assigned
        if(employer.getCompany() == null){
            throw new RuntimeException("Employer does not have a company assigned");
        }

        // Retrieve the jobs associated with the employer's company and transform them to DTOs
        List<Job> jobs = employer.getCompany().getJobs();
        return jobs.stream()
                .map(ApplicationUtility::transformJobToDto)
                .collect(Collectors.toList());
    }


    // Method to update the status of a job based on jobId, new status, and employer's email
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

        // Check if the employer has a company assigned
        if (employer.getCompany() == null) {
            throw new RuntimeException("Employer does not have a company assigned");
        }

        // Find the job by jobId within the employer's company jobs
        Job job = employer.getCompany().getJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        return ApplicationUtility.transformJobToDto(job);
    }


    // Method to create a new job for an employer based on the provided JobDto and employer's email
    @Override
    @Transactional
    public JobDto createJob(JobDto jobDto, String employerEmail){
        // Find the employer by email
        HirersUser employer = userRepository.findUserByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        // Check if the employer has a company assigned
        if(employer.getCompany() == null){
            throw new RuntimeException("Employer does not have a company assigned");
        }

        // Create a new Job entity from the provided JobDto, set its properties, and save it to the repository
        Job job = transformDtoToEntity(jobDto);
        job.setPostedDate(Instant.now());
        job.setApplicationsCount(0);
        job.setStatus(ApplicationConstants.JOB_STATUS_DRAFT);
        job.setCompany(employer.getCompany());

        // We have to save the job manually, as hibernate will not automatically persist the job when we set it to the company's jobs list
        Job savedJob = jobRepository.save(job);
        return ApplicationUtility.transformJobToDto(jobRepository.save(savedJob));
    }

    // Method to get all job applications for a specific job
    @Override
    public List<JobApplicationDto> getApplicationsByJobForEmployer(Long jobId) {
        List<JobApplication> applications = jobApplicationRepository.findByJobIdOrderByAppliedAtAsc(jobId);
        return applications.stream()
                .map(ApplicationUtility::mapToJobApplicationDto)
                .collect(Collectors.toList());
    }

    // Method to update the status and notes of a job application
    @Override
    @Transactional
    public boolean updateJobApplication(UpdateJobApplicationDto dto) {
        int updatedRows = jobApplicationRepository.updateStatusAndNotesById(
                dto.status().name(), dto.notes(), dto.applicationId(), ApplicationUtility.getLoggedInUser());
        return updatedRows > 0;
    }

    // Utility method to transform JobDto to Job entity
    private Job transformDtoToEntity(JobDto jobDto) {
        Job job = new Job();
        BeanUtils.copyProperties(jobDto, job);
        return job;
    }
}
