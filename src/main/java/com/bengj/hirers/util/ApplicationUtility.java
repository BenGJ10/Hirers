package com.bengj.hirers.util;

import com.bengj.hirers.constant.ApplicationConstants;
import com.bengj.hirers.entity.HirersUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class ApplicationUtility {

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
}
