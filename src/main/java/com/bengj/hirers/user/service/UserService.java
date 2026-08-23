package com.bengj.hirers.user.service;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.ProfileDto;
import com.bengj.hirers.dto.UserDto;
import com.bengj.hirers.entity.Company;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Profile;
import com.bengj.hirers.entity.Role;
import com.bengj.hirers.repository.CompanyRepository;
import com.bengj.hirers.repository.HirersUserRepository;
import com.bengj.hirers.repository.ProfileRepository;
import com.bengj.hirers.repository.RoleRepository;
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

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements IUserService{

    private final HirersUserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;

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
        HirersUser user = userRepository.findUserByEmail(userEmail).
                orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Check if the user already has a profile, if not create a new one                                        
        Profile profile = user.getProfile();
        if(profile == null){
            profile = new Profile();
            profile.setUser(user);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        // Deserialize the profile JSON into a ProfileDto object
        ProfileDto profileDto = objectMapper.readValue(profileJson, ProfileDto.class);
        Profile savedProfile = profileRepository.save(mapToProfile(profile, profileDto, profilePicture, resume));
        return mapToProfileDto(savedProfile, false);
    }

    // Method to retrieve a user's profile based on the provided userEmail
    @Override
    public ProfileDto getProfile(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (user.getProfile() == null) {
            return null;
        }
        return mapToProfileDto(user.getProfile(), false);
    }   

    // Method to retrieve a user's profile picture based on the provided userEmail
    @Override
    public ProfileDto getProfilePicture(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (user.getProfile() == null) {
            return null;
        }
        return mapToProfileDto(user.getProfile(), true);
    }

    // Method to retrieve a user's resume based on the provided userEmail
    @Override
    public ProfileDto getResume(String userEmail) {
        HirersUser user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (user.getProfile() == null) {
            return null;
        }
        return mapToProfileDto(user.getProfile(), true);
    }


    // Utility method to transform ProfileDto to Profile entity and handle file uploads
    private Profile mapToProfile(Profile profile, ProfileDto profileDto, MultipartFile profilePicture, MultipartFile resume) {
        // Update text fields
        profile.setJobTitle(profileDto.jobTitle());
        profile.setLocation(profileDto.location());
        profile.setExperienceLevel(profileDto.experienceLevel());
        profile.setProfessionalBio(profileDto.professionalBio());
        profile.setPortfolioWebsite(profileDto.portfolioWebsite());

        // Handle profile picture upload
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                profile.setProfilePicture(profilePicture.getBytes());
                profile.setProfilePictureName(profilePicture.getOriginalFilename());
                profile.setProfilePictureType(profilePicture.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile picture", e);
            }
        }

        // Handle resume upload
        if (resume != null && !resume.isEmpty()) {
            try {
                profile.setResume(resume.getBytes());
                profile.setResumeName(resume.getOriginalFilename());
                profile.setResumeType(resume.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload resume", e);
            }
        }
        return profile;
    }

    // Utility method to transform Profile entity to ProfileDTO
    private ProfileDto mapToProfileDto(Profile profile, boolean includeBinaryData) {
        ProfileDto dto;
        if (includeBinaryData) {
            dto = new ProfileDto(profile.getId(), profile.getUser().getId(),
                    profile.getJobTitle(), profile.getLocation(), profile.getExperienceLevel(),
                    profile.getProfessionalBio(), profile.getPortfolioWebsite(), profile.getProfilePicture(),
                    profile.getProfilePictureName(), profile.getProfilePictureType(), profile.getResume(),
                    profile.getResumeName(), profile.getResumeType(), profile.getCreatedAt(), profile.getUpdatedAt()
            );
        } else {
            dto = new ProfileDto(profile.getId(), profile.getUser().getId(),
                    profile.getJobTitle(), profile.getLocation(), profile.getExperienceLevel(),
                    profile.getProfessionalBio(), profile.getPortfolioWebsite(), null,
                    profile.getProfilePictureName(), profile.getProfilePictureType(), null,
                    profile.getResumeName(), profile.getResumeType(), profile.getCreatedAt(), profile.getUpdatedAt());
        }
        return dto;
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
