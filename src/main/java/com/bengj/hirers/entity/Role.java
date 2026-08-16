package com.bengj.hirers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;


//    As we don't need to fetch all the users for a role, we can remove the relationship mapping to avoid unnecessary data retrieval and potential performance issues.
//    If needed, we can always query users by role using a separate repository method. 
//    @OneToMany(mappedBy = "role", orphanRemoval = true)
//    private List<HirersUser> hirersUsers = new ArrayList<>();

}