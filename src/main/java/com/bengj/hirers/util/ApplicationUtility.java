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

        if(principal instanceof HirersUser user){
            username = user.getEmail();
        }
        else{
            username = principal.toString();
        }
        return username;
    }

    // Utility method to transform Job entity to JobDto
    public static JobDto transformJobToDto(Job job) {
        return new JobDto(
                job.getId(),
                job.getTitle(),
                job.getCompany().getId(),
                job.getCompany().getName(),
                job.getCompany().getLogo(),
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
                job.getApplicationsCount(),
                job.getFeatured(),
                job.getUrgent(),
                job.getRemote(),
                job.getStatus()
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
                    profile.getProfilePicture(),
                    profile.getProfilePictureName(),
                    profile.getProfilePictureType(),
                    profile.getResume(),
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
