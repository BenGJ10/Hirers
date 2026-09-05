package com.bengj.hirers.constant;

public class ApplicationConstants {

    private ApplicationConstants() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    public static final String JWT_SECRET_KEY="JWT_SECRET_KEY";
    public static final String JWT_HEADER="Authorization";
    public static final String JWT_PREFIX="Bearer ";

    public static final String ROLE_JOB_SEEKER="ROLE_JOB_SEEKER";
    public static final String JOB_STATUS_ACTIVE="ACTIVE";
    public static final String JOB_STATUS_CLOSED="CLOSED";
    public static final String JOB_STATUS_DRAFT="DRAFT";

    public static final String NEW_MESSAGE="NEW";
    public static final String CLOSED_MESSAGE="CLOSED";

    public static final String SYSTEM="SYSTEM";

    public static final String ROLE_ADMIN="ROLE_ADMIN";
    public static final String ROLE_EMPLOYER="ROLE_EMPLOYER";

    public static final String PENDING = "PENDING";

    public static final int OTP_VALIDITY_MINUTES = 15;

    public static final String PROFILE_PICTURES_PREFIX = "profile-pictures/";
    public static final String RESUMES_PREFIX = "resumes/";

}
