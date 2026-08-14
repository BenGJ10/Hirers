package com.bengj.hirers.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
    WebConfig class is responsible for configuring the web application settings.
    It implements the WebMvcConfigurer interface to customize the default Spring MVC configuration.

    This class provides two main configurations:
    1. API Versioning: It configures the API versioning strategy using media type parameters.
        The supported versions are "1.0", "2.0", and "3.0", with "1.0" set as the default version.

    2. Path Matching: It adds a path prefix "/api" to all request mappings, ensuring that all API endpoints
        are accessible under the "/api" path.
*/
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer){
        configurer.useMediaTypeParameter(MediaType.parseMediaType("application/vnd.bengj+json"), "v")
                .addSupportedVersions("1.0", "2.0", "3.0").setDefaultVersion("1.0");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer){
        configurer.addPathPrefix("/api", _ -> true); // use _ as a wildcard, as it is a lambda expression parameter that is not used in the method body
    }
}
