package com.bengj.hirers.user.controller;

import com.bengj.hirers.dto.*;
import com.bengj.hirers.user.service.IUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    // Get all users for admin purposes
    @GetMapping(path = "/page/admin", version = "1.0")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<UserDto> userDtoPage = userService.
                getAllUsers(pageNumber, pageSize, sortBy, sortDir);
        return ResponseEntity.status(HttpStatus.OK).body(userDtoPage);
    }

    // Search user by email for admin purposes
    @GetMapping(path = "/search/admin", version = "1.0")
    public ResponseEntity<?> searchUserByEmail(@RequestParam String email) {
        Optional<UserDto> userOptional = userService.searchUserByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found with email: " + email));
        }
        return ResponseEntity.ok(userOptional.get());
    }

    // Elevate a user to employer role for admin purposes
    @PatchMapping(path = "/{userId}/role/employer/admin", version = "1.0")
    public ResponseEntity<?> elevateToEmployer(@PathVariable Long userId) {
        UserDto updatedUser = userService.elevateToEmployer(userId);
        return ResponseEntity.ok(updatedUser);
    }

    // Assign a company to an employer for admin purposes
    @PatchMapping(path = "/{userId}/company/{companyId}/admin", version = "1.0")
    public ResponseEntity<?> assignCompanyToEmployer(
            @PathVariable Long userId, @PathVariable Long companyId) {
        UserDto updatedUser = userService.assignCompanyToEmployer(userId, companyId);
        return ResponseEntity.ok(updatedUser);
    }

    // Create or update the profile for a jobseeker
    @PutMapping(path = "/profile/jobseeker", version = "1.0",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDto> createOrUpdateProfile(
            @RequestPart("profile") String profileJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            Authentication authentication) throws JsonProcessingException {

        // Extract user email from authentication
        String userEmail = authentication.getName();

        ProfileDto savedProfile = userService.createOrUpdateProfile(
                userEmail, profileJson, profilePicture, resume);
        return ResponseEntity.ok(savedProfile);
    }

    // Get the profile of the authenticated jobseeker
    @GetMapping(path = "/profile/jobseeker", version = "1.0")
    public ResponseEntity<ProfileDto> getProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        ProfileDto profile = userService.getProfile(userEmail);
        return ResponseEntity.ok(profile);
    }

    // Get the profile picture of the authenticated jobseeker
    @GetMapping(path = "/profile/picture/jobseeker", version = "1.0")
    public ResponseEntity<byte[]> getProfilePicture(Authentication authentication) {
        String userEmail = authentication.getName();
        ProfileDto profile = userService.getProfilePicture(userEmail);
        
        // Check if the profile and picture exist
        if (profile == null || profile.profilePicture() == null || profile.profilePicture().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Set the appropriate headers for the response, including content type and length
        HttpHeaders headers = new HttpHeaders();
        if (profile.profilePictureType() != null && !profile.profilePictureType().isBlank()) {
            headers.setContentType(MediaType.parseMediaType(profile.profilePictureType()));
        } else {
            headers.setContentType(MediaType.IMAGE_JPEG);
        }
        headers.setContentLength(profile.profilePicture().length);
        return new ResponseEntity<>(profile.profilePicture(), headers, HttpStatus.OK);
    }

    // Get the resume of the authenticated jobseeker
    @GetMapping(path = "/profile/resume/jobseeker", version = "1.0")
    public ResponseEntity<byte[]> getResume(Authentication authentication) {
        String userEmail = authentication.getName();
        ProfileDto profileDto = userService.getResume(userEmail);
        
        // Check if the profile and resume exist
        if (profileDto == null || profileDto.resume() == null || profileDto.resume().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        byte[] resume = profileDto.resume();

        // Set the appropriate headers for the response, including content type, length, and content disposition for file download
        HttpHeaders headers = new HttpHeaders();
        if (profileDto.resumeType() != null && !profileDto.resumeType().isBlank()) {
            headers.setContentType(MediaType.parseMediaType(profileDto.resumeType()));
        } else {
            headers.setContentType(MediaType.APPLICATION_PDF);
        }
        headers.setContentLength(resume.length);
        String fileName = profileDto.resumeName() != null ? profileDto.resumeName() : "resume.pdf";
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(resume, headers, HttpStatus.OK);
    }

    // Save a job for the authenticated jobseeker
    @PostMapping(path = "/saved-jobs/{jobId}/jobseeker", version = "1.0")
    public ResponseEntity<JobDto> saveJob(@PathVariable Long jobId,
                                          Authentication authentication) {
        String userEmail = authentication.getName();
        JobDto savedJob = userService.saveJob(userEmail, jobId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJob);
    }

    // Unsave a job for the authenticated jobseeker
    @DeleteMapping(path = "/saved-jobs/{jobId}/jobseeker", version = "1.0")
    public ResponseEntity<String> unsaveJob(@PathVariable Long jobId,
                                            Authentication authentication) {
        String userEmail = authentication.getName();
        userService.unsaveJob(userEmail, jobId);
        return ResponseEntity.status(HttpStatus.OK).body("Job unsaved successfully");
    }

    // Get all saved jobs for the authenticated jobseeker
    @GetMapping(path = "/saved-jobs/jobseeker", version = "1.0")
    public ResponseEntity<List<JobDto>> getSavedJobs(Authentication authentication) {
        String userEmail = authentication.getName();
        List<JobDto> savedJobDtos = userService.getSavedJobs(userEmail);
        return ResponseEntity.ok(savedJobDtos);
    }

    // Apply for a job as the authenticated jobseeker
    @PostMapping(path = "/job-applications/jobseeker", version = "1.0")
    public ResponseEntity<JobApplicationDto> applyForJob(
            @RequestBody @Valid ApplyJobRequestDto applyJobRequestDto,
            Authentication authentication) {
        String userEmail = authentication.getName();
        JobApplicationDto jobApplicationDto = userService.applyForJob(
                userEmail, applyJobRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobApplicationDto);
    }

    // Withdraw a job application as the authenticated jobseeker
    @DeleteMapping(path = "/job-applications/{jobId}/jobseeker", version = "1.0")
    public ResponseEntity<String> withdrawApplication(
            @PathVariable Long jobId, Authentication authentication) {
        String userEmail = authentication.getName();
        userService.withdrawApplication(userEmail, jobId);
        return ResponseEntity.status(HttpStatus.OK).body("Application withdrawn successfully");
    }

    // Get all job applications for the authenticated jobseeker
    @GetMapping(path = "/job-applications/jobseeker", version = "1.0")
    public ResponseEntity<List<JobApplicationDto>> getJobApplications(Authentication authentication) {
        String userEmail = authentication.getName();
        List<JobApplicationDto> applications = userService.getJobSeekerApplications(userEmail);
        return ResponseEntity.ok(applications);
    }
}
