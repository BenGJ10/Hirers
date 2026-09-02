package com.bengj.hirers.entity;

import com.bengj.hirers.enums.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class HirersUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 500)
    @Column(name = "password_hash", length = 500)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Size(max = 20)
    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @ManyToMany
    @JoinTable(name = "saved_jobs", joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = {@JoinColumn(name = "job_id")})
    private Set<Job> savedJobs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<JobApplication> jobApplications = new LinkedHashSet<>();
}