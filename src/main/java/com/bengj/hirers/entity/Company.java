package com.bengj.hirers.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter @Setter
@NamedQueries({
        @NamedQuery(name = "Company.fetchCompaniesWithJobsByStatus",
                query = "SELECT DISTINCT c FROM Company c JOIN FETCH c.jobs j WHERE j.status = :status"),

        @NamedQuery(name = "Company.updateCompany",
                query =
                        """
                                UPDATE Company c SET
                                                            c.name = :name,
                                                            c.logo = :logo,
                                                            c.industry = :industry,
                                                            c.size = :size,
                                                            c.rating = :rating,
                                                            c.locations = :locations,
                                                            c.founded = :founded,
                                                            c.description = :description,
                                                            c.employees = :employees,
                                                            c.website = :website
                                                        WHERE c.id = :id
                        """)
})
@NamedNativeQueries({
        @NamedNativeQuery(name = "Company.fetchCompaniesWithJobsByStatusNative",
                query = "SELECT DISTINCT c.* FROM companies c JOIN jobs j ON c.id = j.company_id WHERE j.status = ?",
                resultClass = Company.class)})
public class Company extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Column(name = "LOGO", length = 500)
    private String logo;

    @Column(name = "INDUSTRY", nullable = false, length = 100)
    private String industry;

    @Column(name = "SIZE", nullable = false, length = 50)
    private String size;

    @Column(name = "RATING", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "LOCATIONS", length = 1000)
    private String locations;

    @Column(name = "FOUNDED", nullable = false)
    private Integer founded;

    @Lob // Large Object
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EMPLOYEES")
    private Integer employees;

    @Column(name = "WEBSITE", length = 500)
    private String website;

    /**
     * A company can have many jobs
     * CascadeType.ALL means that if a company is deleted, all its jobs will also be deleted. This is a hibernate feature that allows for automatic propagation of operations from parent to child entities.
     * orphanRemoval = true means that if a job is removed from the company's job list, it will also be deleted from the database. This is useful for maintaining data integrity and avoiding orphaned records.
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Job> jobs = new ArrayList<>();

}

