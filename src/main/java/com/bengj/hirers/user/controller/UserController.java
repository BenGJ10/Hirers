package com.bengj.hirers.user.controller;

import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.dto.ProfileDto;
import com.bengj.hirers.dto.UserDto;
import com.bengj.hirers.user.service.IUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
            @RequestParam(defaultValue = "asc") String sortDir){

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
        
        // Check if the profile picture exists
        byte[] picture = profile.profilePicture();
        if (picture == null || picture.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Set the appropriate headers for the response, including content type and length
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(profile.profilePictureType()));
        headers.setContentLength(picture.length);
        return new ResponseEntity<>(picture, headers, HttpStatus.OK);
    }

    // Get the resume of the authenticated jobseeker
    @GetMapping(path = "/profile/resume/jobseeker", version = "1.0")
    public ResponseEntity<byte[]> getResume(Authentication authentication) {
        String userEmail = authentication.getName();
        ProfileDto profileDto = userService.getResume(userEmail);
        
        // Check if the resume exists
        byte[] resume = profileDto.resume();
        if (resume == null || resume.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Set the appropriate headers for the response, including content type, length, and content disposition for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(profileDto.resumeType()));
        headers.setContentLength(resume.length);
        headers.setContentDispositionFormData("attachment", profileDto.resumeName());
        return new ResponseEntity<>(resume, headers, HttpStatus.OK);
    }


    @PostMapping(path = "/saved-jobs/{jobId}/jobseeker", version = "1.0")
    public ResponseEntity<JobDto> saveJob(@PathVariable Long jobId,
                                          Authentication authentication) {
        String userEmail = authentication.getName();
        JobDto savedJob = userService.saveJob(userEmail, jobId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJob);
    }

    @DeleteMapping(path = "/saved-jobs/{jobId}/jobseeker", version = "1.0")
    public ResponseEntity<String> unsaveJob(@PathVariable Long jobId,
                                            Authentication authentication) {
        String userEmail = authentication.getName();
        userService.unsaveJob(userEmail, jobId);
        return ResponseEntity.status(HttpStatus.OK).body("Job unsaved successfully");
    }

    @GetMapping(path = "/saved-jobs/jobseeker", version = "1.0")
    public ResponseEntity<List<JobDto>> getSavedJobs(Authentication authentication) {
        String userEmail = authentication.getName();
        List<JobDto> savedJobDtos = userService.getSavedJobs(userEmail);
        return ResponseEntity.ok(savedJobDtos);
    }

}
