package com.bengj.hirers.constant;

public class ApplicationConstants {

    private ApplicationConstants() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    public static final String JWT_SECRET_KEY="JWT_SECRET_KEY";
    public static final String JWT_SECRET_DEFAULT_VALUE="ndaijsoandaioian3128609hb@9jsiai9iklaksmxj8jqwh";
    public static final String JWT_HEADER="Authorization";
    public static final String JWT_PREFIX="Bearer ";

    public static final String ROLE_JOB_SEEKER="ROLE_JOB_SEEKER";
    public static final String JOB_STATUS_ACTIVE="ACTIVE";

    public static final String NEW_MESSAGE="NEW";
    public static final String READ_MESSAGE="READ";
    public static final String CLOSED_MESSAGE="CLOSED";

    public static final String SYSTEM="SYSTEM";

}
