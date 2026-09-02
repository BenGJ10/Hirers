package com.bengj.hirers.job.controller;

import com.bengj.hirers.dto.JobApplicationDto;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.dto.ProfileDto;
import com.bengj.hirers.dto.UpdateJobApplicationDto;
import com.bengj.hirers.job.service.IJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final IJobService jobService;

    // Retrieve all jobs for the authenticated employer
    @GetMapping(path = "/employer", version = "1.0")
    public ResponseEntity<List<JobDto>> getEmployerJobs(Authentication authentication) {
        String employerEmail = authentication.getName();
        List<JobDto> jobs = jobService.getEmployerJobs(employerEmail);
        return ResponseEntity.ok(jobs);
    }

    // Create a new job for the authenticated employer
    @PostMapping(path = "/employer", version = "1.0")
    public ResponseEntity<JobDto> createJob(@RequestBody @Valid JobDto jobDto, Authentication authentication) {
        String employerEmail = authentication.getName();
        JobDto createdJob = jobService.createJob(jobDto, employerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
    }

    // Update full job details for the authenticated employer
    @PutMapping(path = "/{jobId}/employer", version = "1.0")
    public ResponseEntity<JobDto> updateJob(
            @PathVariable Long jobId,
            @RequestBody @Valid JobDto jobDto,
            Authentication authentication) {
        String employerEmail = authentication.getName();
        JobDto updatedJob = jobService.updateJob(jobId, jobDto, employerEmail);
        return ResponseEntity.ok(updatedJob);
    }

    // Update the status of a job for the authenticated employer
    @PatchMapping(path = "/{jobId}/status/employer", version = "1.0")
    public ResponseEntity<?> updateJobStatus(
            @PathVariable Long jobId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication){

        // Get the authenticated employer's email and the new status from the request body
        String employerEmail = authentication.getName();
        String status = requestBody.get("status");

        // Validate that the status is provided in the request body
        if(status == null || status.trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Status is required"));
        }

        JobDto updatedJob = jobService.updateJobStatus(jobId, status.toUpperCase(), employerEmail);
        return ResponseEntity.ok(updatedJob);
    }

    // Retrieve all applications for a specific job
    @GetMapping(path = "/applications/{jobId}/employer")
    public ResponseEntity<List<JobApplicationDto>> getApplicationsByJobForEmployer(@PathVariable Long jobId) {
        List<JobApplicationDto> applications = jobService.getApplicationsByJobForEmployer(jobId);
        return ResponseEntity.ok(applications);
    }

    // Get candidate resume for an application (Employer)
    @GetMapping(path = "/applications/{applicationId}/resume/employer")
    public ResponseEntity<byte[]> getApplicationResume(@PathVariable Long applicationId) {
        ProfileDto profileDto = jobService.getApplicationResume(applicationId);
        if (profileDto == null || profileDto.resume() == null || profileDto.resume().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        byte[] resume = profileDto.resume();

        HttpHeaders headers = new HttpHeaders();
        if (profileDto.resumeType() != null && !profileDto.resumeType().isBlank()) {
            headers.setContentType(MediaType.parseMediaType(profileDto.resumeType()));
        } else {
            headers.setContentType(MediaType.APPLICATION_PDF);
        }
        headers.setContentLength(resume.length);
        String fileName = profileDto.resumeName() != null ? profileDto.resumeName() : "resume.pdf";
        headers.setContentDispositionFormData("inline", fileName);
        return new ResponseEntity<>(resume, headers, HttpStatus.OK);
    }

    // Get candidate profile picture for an application (Employer)
    @GetMapping(path = "/applications/{applicationId}/picture/employer")
    public ResponseEntity<byte[]> getApplicationProfilePicture(@PathVariable Long applicationId) {
        ProfileDto profileDto = jobService.getApplicationProfilePicture(applicationId);
        if (profileDto == null || profileDto.profilePicture() == null || profileDto.profilePicture().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        byte[] picture = profileDto.profilePicture();
        HttpHeaders headers = new HttpHeaders();
        if (profileDto.profilePictureType() != null && !profileDto.profilePictureType().isBlank()) {
            headers.setContentType(MediaType.parseMediaType(profileDto.profilePictureType()));
        } else {
            headers.setContentType(MediaType.IMAGE_JPEG);
        }
        headers.setContentLength(picture.length);
        return new ResponseEntity<>(picture, headers, HttpStatus.OK);
    }

    // Update the status of a job application
    @PatchMapping(path = "/applications/employer", version = "1.0")
    public ResponseEntity<String> updateJobApplication(
            @RequestBody @Valid UpdateJobApplicationDto updateJobApplicationDto) {
        boolean isUpdated = jobService.updateJobApplication(updateJobApplicationDto);

        if (isUpdated) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Job application updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update job application");
        }
    }
}
