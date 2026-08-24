package com.bengj.hirers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @Size(max = 255)
        @NotNull
        @Column(name = "title", nullable = false)
        private String title;


        /*
         * The @NotNull annotation is a validation constraint that ensures the company field cannot be null.
           This means that every job must be associated with a company, and if an attempt is made to save a job without a company, a validation error will occur.

         * The @ManyToOne annotation indicates that many jobs can belong to one company. The fetch type is set to LAZY, which means that the company data will only be loaded when it is explicitly accessed,
           rather than being loaded immediately with the job data. The optional attribute is set to false, indicating that a job must always be associated with a company.

         * The @OnDelete annotation specifies a database constraint that when a company is deleted, all associated jobs should also be deleted.
           This is database-level enforcement of the cascading delete behavior, ensuring that there are no orphaned job records in the database when a company is removed.

         * The @JoinColumn annotation specifies the foreign key column in the jobs table that references the companies table. The name attribute defines the name of the foreign key column,
           and nullable = false indicates that this column cannot be null, enforcing the requirement that every job must be associated with a company. This ensures referential integrity between the jobs and companies tables in the database.
         */
        @NotNull
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "company_id", nullable = false)
        private Company company;

        @Size(max = 255)
        @NotNull
        @Column(name = "location", nullable = false)
        private String location;

        @Size(max = 50)
        @NotNull
        @Column(name = "work_type", nullable = false, length = 50)
        private String workType;

        @Size(max = 50)
        @NotNull
        @Column(name = "job_type", nullable = false, length = 50)
        private String jobType;

        @Size(max = 100)
        @NotNull
        @Column(name = "category", nullable = false, length = 100)
        private String category;

        @Size(max = 50)
        @NotNull
        @Column(name = "experience_level", nullable = false, length = 50)
        private String experienceLevel;

        @NotNull
        @Column(name = "salary_min", nullable = false, precision = 12, scale = 2)
        private BigDecimal salaryMin;

        @NotNull
        @Column(name = "salary_max", nullable = false, precision = 12, scale = 2)
        private BigDecimal salaryMax;

        @Size(max = 10)
        @NotNull
        @ColumnDefault("'USD'")
        @Column(name = "salary_currency", nullable = false, length = 10)
        private String salaryCurrency;

        @Size(max = 20)
        @NotNull
        @ColumnDefault("'year'")
        @Column(name = "salary_period", nullable = false, length = 20)
        private String salaryPeriod;

        @NotNull
        @Lob
        @Column(name = "description", nullable = false, columnDefinition = "TEXT"       )
        private String description;

        @Lob
        @Column(name = "requirements", columnDefinition = "TEXT")
        private String requirements;

        @Lob
        @Column(name = "benefits", columnDefinition = "TEXT")
        private String benefits;

        @NotNull
        @ColumnDefault("CURRENT_TIMESTAMP")
        @Column(name = "posted_date", nullable = false)
        private Instant postedDate;

        @Column(name = "application_deadline")
        private Instant applicationDeadline;

        @ColumnDefault("0")
        @Column(name = "applications_count")
        private Integer applicationsCount;

        @ColumnDefault("0")
        @Column(name = "featured")
        private Boolean featured;

        @ColumnDefault("0")
        @Column(name = "urgent")
        private Boolean urgent;

        @ColumnDefault("0")
        @Column(name = "remote")
        private Boolean remote;

        @Size(max = 20)
        @NotNull
        @ColumnDefault("'ACTIVE'")
        @Column(name = "status", nullable = false, length = 20)
        private String status;

        @ManyToMany(mappedBy = "savedJobs")
        private Set<HirersUser> savedByUsers = new LinkedHashSet<>();

        @OneToMany(mappedBy = "job")
        private Set<JobApplication> jobApplications = new LinkedHashSet<>();
}