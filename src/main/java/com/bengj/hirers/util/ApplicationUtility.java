package com.bengj.hirers.util;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.JobApplicationDto;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.dto.ProfileDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Job;
import com.bengj.hirers.entity.JobApplication;
import com.bengj.hirers.entity.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

import com.bengj.hirers.security.oauth2.CustomOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class ApplicationUtility {

    // Get the logged in user
    public static String getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()
        || Objects.equals(authentication.getPrincipal(), "anonymousUser")){
            return ApplicationConstants.SYSTEM;
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof HirersUser user){
            username = user.getEmail();
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            username = customOAuth2User.getEmail() != null ? customOAuth2User.getEmail() : customOAuth2User.getName();
        } else if (principal instanceof OAuth2User oAuth2User) {
            String email = (String) oAuth2User.getAttributes().get("email");
            username = email != null ? email : oAuth2User.getName();
        } else {
            username = authentication.getName();
        }

        if (username == null || username.isBlank()) {
            username = ApplicationConstants.SYSTEM;
        }

        return username;
    }

    // Utility method to transform Job entity to JobDto
    public static JobDto transformJobToDto(Job job) {
        int appCount = 0;
        if (job.getJobApplications() != null && !job.getJobApplications().isEmpty()) {
            appCount = job.getJobApplications().size();
        } else if (job.getApplicationsCount() != null) {
            appCount = job.getApplicationsCount();
        }

        return new JobDto(
                job.getId(),
                job.getTitle(),
                job.getCompany() != null ? job.getCompany().getId() : null,
                job.getCompany() != null ? job.getCompany().getName() : null,
                job.getCompany() != null ? job.getCompany().getLogo() : null,
                job.getLocation(),
                job.getWorkType(),
                job.getJobType(),
                job.getCategory(),
                job.getExperienceLevel(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                job.getSalaryPeriod(),
                job.getDescription(),
                job.getRequirements(),
                job.getBenefits(),
                job.getPostedDate(),
                job.getApplicationDeadline(),
                appCount,
                job.getFeatured(),
                job.getUrgent(),
                job.getRemote(),
                job.getStatus(),
                job.getCreatedBy()
        );
    }

    // Utility method to transform Job entity to JobDto
    public static JobApplicationDto mapToJobApplicationDto(JobApplication application) {
        // Map profile if exists
        ProfileDto profileDto = null;
        Profile profile = application.getUser().getProfile();
        if (profile != null) {
            profileDto = new ProfileDto(
                    profile.getId(),
                    profile.getUser().getId(),
                    profile.getJobTitle(),
                    profile.getLocation(),
                    profile.getExperienceLevel(),
                    profile.getProfessionalBio(),
                    profile.getPortfolioWebsite(),
                    null,
                    profile.getProfilePictureName(),
                    profile.getProfilePictureType(),
                    null,
                    profile.getResumeName(),
                    profile.getResumeType(),
                    profile.getCreatedAt(),
                    profile.getUpdatedAt()
            );
        }
        return new JobApplicationDto(
                application.getId(),
                application.getUser().getId(),
                application.getUser().getName(),
                application.getUser().getEmail(),
                application.getUser().getMobileNumber(),
                profileDto,
                ApplicationUtility.transformJobToDto(application.getJob()),
                application.getAppliedAt(),
                application.getStatus(),
                application.getCoverLetter(),
                application.getNotes()
        );
    }
}
