package com.bengj.hirers.job.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.JobApplicationDto;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.dto.ProfileDto;
import com.bengj.hirers.dto.UpdateJobApplicationDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Job;
import com.bengj.hirers.entity.JobApplication;
import com.bengj.hirers.entity.Profile;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.JobApplicationRepository;
import com.bengj.hirers.repository.JobRepository;
import com.bengj.hirers.s3.IS3StorageService;
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
    private final IS3StorageService s3StorageService;


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
            throw new RuntimeException("Invalid job status");
        }

        // Find the job by ID
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));

        // Find the employer by email
        HirersUser employer = userRepository.findUserByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        // Check if the job belongs to the employer's company
        if (!job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new RuntimeException("Employer is not authorized to update this job");
        }

        // Update the status of the job and save it
        job.setStatus(status);
        Job updatedJob = jobRepository.save(job);
        return ApplicationUtility.transformJobToDto(updatedJob);
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
        job.setStatus(ApplicationConstants.JOB_STATUS_DRAFT);
        job.setCompany(employer.getCompany());

        // We have to save the job manually, as hibernate will not automatically persist the job when we set it to the company's jobs list
        Job savedJob = jobRepository.save(job);
        return ApplicationUtility.transformJobToDto(jobRepository.save(savedJob));
    }

    // Method to update full details of a job by its creator employer
    @Override
    @Transactional
    public JobDto updateJob(Long jobId, JobDto jobDto, String employerEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));

        HirersUser employer = userRepository.findUserByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        if (employer.getCompany() == null || !job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new RuntimeException("Employer is not authorized to update this job");
        }

        job.setTitle(jobDto.title());
        job.setLocation(jobDto.location());
        job.setWorkType(jobDto.workType());
        job.setJobType(jobDto.jobType());
        job.setCategory(jobDto.category());
        job.setExperienceLevel(jobDto.experienceLevel());
        job.setSalaryMin(jobDto.salaryMin());
        job.setSalaryMax(jobDto.salaryMax());
        job.setSalaryCurrency(jobDto.salaryCurrency());
        job.setSalaryPeriod(jobDto.salaryPeriod());
        job.setDescription(jobDto.description());
        job.setRequirements(jobDto.requirements());
        job.setBenefits(jobDto.benefits());
        job.setApplicationDeadline(jobDto.applicationDeadline());
        if (jobDto.featured() != null) job.setFeatured(jobDto.featured());
        if (jobDto.urgent() != null) job.setUrgent(jobDto.urgent());
        if (jobDto.remote() != null) job.setRemote(jobDto.remote());
        if (jobDto.status() != null && !jobDto.status().isBlank()) {
            job.setStatus(jobDto.status().toUpperCase());
        }

        Job updatedJob = jobRepository.save(job);
        return ApplicationUtility.transformJobToDto(updatedJob);
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

    // Method to get candidate resume for a specific application from S3
    @Override
    public ProfileDto getApplicationResume(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Job application not found with ID: " + applicationId));

        Profile profile = application.getUser().getProfile();
        if (profile == null || profile.getResumeKey() == null) {
            return null;
        }

        byte[] resumeBytes = s3StorageService.downloadObject(profile.getResumeKey());
        return new ProfileDto(
                profile.getId(),
                application.getUser().getId(),
                profile.getJobTitle(),
                profile.getLocation(),
                profile.getExperienceLevel(),
                profile.getProfessionalBio(),
                profile.getPortfolioWebsite(),
                null,
                null,
                null,
                resumeBytes,
                profile.getResumeName(),
                profile.getResumeType(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    // Method to get a candidate profile picture for a specific application from S3
    @Override
    public ProfileDto getApplicationProfilePicture(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Job application not found with ID: " + applicationId));

        Profile profile = application.getUser().getProfile();
        if (profile == null || profile.getProfilePictureKey() == null) {
            return null;
        }

        byte[] pictureBytes = s3StorageService.downloadObject(profile.getProfilePictureKey());
        return new ProfileDto(
                profile.getId(),
                application.getUser().getId(),
                profile.getJobTitle(),
                profile.getLocation(),
                profile.getExperienceLevel(),
                profile.getProfessionalBio(),
                profile.getPortfolioWebsite(),
                pictureBytes,
                profile.getProfilePictureName(),
                profile.getProfilePictureType(),
                null,
                null,
                null,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    // Utility method to transform JobDto to Job entity
    private Job transformDtoToEntity(JobDto jobDto) {
        Job job = new Job();
        BeanUtils.copyProperties(jobDto, job);
        return job;
    }
}
