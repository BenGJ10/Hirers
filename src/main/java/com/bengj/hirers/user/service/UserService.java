package com.bengj.hirers.user.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.*;
import com.bengj.hirers.entity.*;
import com.bengj.hirers.repository.*;
import com.bengj.hirers.s3.IS3StorageService;
import com.bengj.hirers.util.ApplicationUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// read-only transactions by default, can be overridden for specific methods.
// It helps in optimizing performance for read operations by avoiding unnecessary locking and flushing of the persistence context.
@Transactional(readOnly = true) 
public class UserService implements IUserService{

    private final HirersUserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final IS3StorageService s3StorageService;

    // Method to retrieve all users with pagination and sorting
    @Override
    public Page<UserDto> getAllUsers(int pageNumber, int pageSize, String sortBy, String sortDir) {
        // Create a Sort object based on the provided sort parameters
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Create a pageable object with the specified page number and page size
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        return userRepository.findAll(pageable).map(this::mapToUserDto);
    }

    // Method to search for a user by email and return an Optional<UserDto>
    @Override
    public Optional<UserDto> searchUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .map(this::mapToUserDto);
    }

    // Method to elevate a user to employer role based on the provided userId
    @Override
    @Transactional
    public UserDto elevateToEmployer(Long userId) {
        HirersUser user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId));

        // Check if the user is already having the employer role
        if (user.getRole().getName().equals(ApplicationConstants.ROLE_EMPLOYER)) {
            return mapToUserDto(user);
        }

        // Check if the user is already an admin
        if (user.getRole().getName().equals(ApplicationConstants.ROLE_ADMIN)) {
            throw new RuntimeException("Cannot elevate admin to employer");
        }

        // Elevate the user to employer role
        Role employerRole = roleRepository.findRoleByName(ApplicationConstants.ROLE_EMPLOYER).orElseThrow(
                () -> new RuntimeException("Role not found: " + ApplicationConstants.ROLE_EMPLOYER));
        user.setRole(employerRole);
        return mapToUserDto(user);
    }   

    // Method to assign a company to an employer based on the provided userId and companyId
    @Override
    @Transactional
    public UserDto assignCompanyToEmployer(Long userId, Long companyId) {
        HirersUser user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId));

        // Verify that the user is an employer
        if (!user.getRole().getName().equals(ApplicationConstants.ROLE_EMPLOYER)) {
            throw new RuntimeException("User is not an employer");
        }

        // Verify that the company exists
        Company company = companyRepository.findById(companyId).orElseThrow(
                () -> new RuntimeException("Company not found with id: " + companyId));

        user.setCompany(company);
        return mapToUserDto(user);
    }

    // Method to create or update a user's profile based on the provided userEmail, profileJson, profilePicture, and resume
    @Override
    @Transactional
    public ProfileDto createOrUpdateProfile(String userEmail, String profileJson,
                                            MultipartFile profilePicture, MultipartFile resume) throws JsonProcessingException{
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Check if the user already has a profile, if not create a new one                                        
        Profile profile = user.getProfile();
        if(profile == null){
            profile = new Profile();
            profile.setUser(user);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        // Deserialize the profile JSON into a ProfileDto object
        ProfileDto profileDto = objectMapper.readValue(profileJson, ProfileDto.class);

        // Update text fields
        profile.setJobTitle(profileDto.jobTitle());
        profile.setLocation(profileDto.location());
        profile.setExperienceLevel(profileDto.experienceLevel());
        profile.setProfessionalBio(profileDto.professionalBio());
        profile.setPortfolioWebsite(profileDto.portfolioWebsite());

        String oldPictureKey = profile.getProfilePictureKey();
        String oldResumeKey = profile.getResumeKey();

        // Handle profile picture upload to S3
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String newPictureKey = s3StorageService.uploadProfilePicture(user.getId(), profilePicture);
            profile.setProfilePictureKey(newPictureKey);
            profile.setProfilePictureName(profilePicture.getOriginalFilename());
            profile.setProfilePictureType(profilePicture.getContentType());
        }

        // Handle resume upload to S3
        if (resume != null && !resume.isEmpty()) {
            String newResumeKey = s3StorageService.uploadResume(user.getId(), resume);
            profile.setResumeKey(newResumeKey);
            profile.setResumeName(resume.getOriginalFilename());
            profile.setResumeType(resume.getContentType());
        }

        Profile savedProfile = profileRepository.save(profile);

        // Delete old S3 objects if new ones were successfully uploaded and saved
        if (profilePicture != null && !profilePicture.isEmpty() && oldPictureKey != null && !oldPictureKey.equals(savedProfile.getProfilePictureKey())) {
            s3StorageService.deleteObject(oldPictureKey);
        }
        if (resume != null && !resume.isEmpty() && oldResumeKey != null && !oldResumeKey.equals(savedProfile.getResumeKey())) {
            s3StorageService.deleteObject(oldResumeKey);
        }

        return mapToProfileDto(savedProfile, null, null);
    }

    // Method to retrieve a user's profile based on the provided userEmail
    @Override
    public ProfileDto getProfile(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (user.getProfile() == null) {
            return null;
        }
        return mapToProfileDto(user.getProfile(), null, null);
    }   

    // Method to retrieve a user's profile picture based on the provided userEmail
    @Override
    public ProfileDto getProfilePicture(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        Profile profile = user.getProfile();
        if (profile == null || profile.getProfilePictureKey() == null) {
            return null;
        }
        byte[] pictureBytes = s3StorageService.downloadObject(profile.getProfilePictureKey());
        return mapToProfileDto(profile, pictureBytes, null);
    }

    // Method to retrieve a user's resume based on the provided userEmail
    @Override
    public ProfileDto getResume(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        Profile profile = user.getProfile();
        if (profile == null || profile.getResumeKey() == null) {
            return null;
        }
        byte[] resumeBytes = s3StorageService.downloadObject(profile.getResumeKey());
        return mapToProfileDto(profile, null, resumeBytes);
    }

    // Method to save a job to the user's saved jobs list'
    @Override
    @Transactional
    public JobDto saveJob(String userEmail, Long jobId){
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        user.getSavedJobs().add(job);
        return ApplicationUtility.transformJobToDto(job);
    }

    // Method to unsave a job from the user's saved jobs list'
    @Override
    @Transactional
    public void unsaveJob(String userEmail, Long jobId){
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        user.getSavedJobs().remove(job);
    }

    // Method to retrieve a user's saved jobs list
    @Override
    public List<JobDto> getSavedJobs(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        return user.getSavedJobs().stream()
                .map(ApplicationUtility::transformJobToDto)
                .collect(Collectors.toList());
    }

    // Method to apply for a job based on the provided userEmail and ApplyJobRequestDto
    @Override
    @Transactional
    public JobApplicationDto applyForJob(String userEmail, ApplyJobRequestDto request) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Check if the user has already applied for the job
        Long jobId = request.jobId();
        if(jobApplicationRepository.existsByUserIdAndJobId(user.getId(), jobId)){
            throw new RuntimeException("You have already applied for this job");
        }

        // Validate the jobId
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // Create a new JobApplication entity and set its properties
        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJob(job);
        application.setAppliedAt(Instant.now());
        application.setStatus(ApplicationConstants.PENDING);
        application.setCoverLetter(request.coverLetter());
        JobApplication saved = jobApplicationRepository.save(application);

        // Increment the applications count for the job
        job.setApplicationsCount(job.getApplicationsCount() != null ? job.getApplicationsCount() + 1 : 1);
        jobRepository.save(job);
        return ApplicationUtility.mapToJobApplicationDto(saved);
    }

    // Method to withdraw a job application based on the provided userEmail and jobId
    @Override
    @Transactional
    public void withdrawApplication(String userEmail, Long jobId) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        if(!jobApplicationRepository.existsByUserIdAndJobId(user.getId(), jobId)){
            throw new RuntimeException("You have not applied for this job");
        }

        jobApplicationRepository.deleteByUserIdAndJobId(user.getId(), jobId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        if(job.getApplicationsCount() != null && job.getApplicationsCount() > 0){
            job.setApplicationsCount(job.getApplicationsCount() - 1);
        }
    }

    // Method to retrieve all job applications made by a user based on the provided userEmail
    @Override
    public List<JobApplicationDto> getJobSeekerApplications(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        return user.getJobApplications().stream()
                .map(ApplicationUtility::mapToJobApplicationDto)
                .collect(Collectors.toList());
    }


    // Utility method to transform Profile entity to ProfileDTO
    private ProfileDto mapToProfileDto(Profile profile, byte[] profilePictureBytes, byte[] resumeBytes) {
        return new ProfileDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getJobTitle(),
                profile.getLocation(),
                profile.getExperienceLevel(),
                profile.getProfessionalBio(),
                profile.getPortfolioWebsite(),
                profilePictureBytes,
                profile.getProfilePictureName(),
                profile.getProfilePictureType(),
                resumeBytes,
                profile.getResumeName(),
                profile.getResumeType(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    // Utility method to transform User entity to UserDTO
    private UserDto mapToUserDto(HirersUser user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        dto.setUserId(user.getId());
        dto.setRole(user.getRole() != null ? user.getRole().getName() : null);
        dto.setCompanyId(user.getCompany() != null ? user.getCompany().getId() : null);
        dto.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : null);
        return dto;
    }
}
