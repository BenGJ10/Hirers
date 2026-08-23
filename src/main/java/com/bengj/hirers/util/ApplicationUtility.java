package com.bengj.hirers.util;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.dto.JobDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.entity.Job;
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
}
