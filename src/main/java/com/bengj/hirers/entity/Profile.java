package com.bengj.hirers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private HirersUser user;

    @Size(max = 255)
    @NotNull
    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Size(max = 255)
    @NotNull
    @Column(name = "location", nullable = false)
    private String location;

    @Size(max = 50)
    @NotNull
    @Column(name = "experience_level", nullable = false, length = 50)
    private String experienceLevel;

    @NotNull
    @Lob
    @Column(name = "professional_bio", nullable = false)
    private String professionalBio;

    @Size(max = 500)
    @Column(name = "portfolio_website", length = 500)
    private String portfolioWebsite;

    @Size(max = 500)
    @Column(name = "profile_picture_key", length = 500)
    private String profilePictureKey;

    @Size(max = 255)
    @Column(name = "profile_picture_name")
    private String profilePictureName;

    @Size(max = 100)
    @Column(name = "profile_picture_type", length = 100)
    private String profilePictureType;

    @Size(max = 500)
    @Column(name = "resume_key", length = 500)
    private String resumeKey;

    @Size(max = 255)
    @Column(name = "resume_name")
    private String resumeName;

    @Size(max = 100)
    @Column(name = "resume_type", length = 100)
    private String resumeType;

}