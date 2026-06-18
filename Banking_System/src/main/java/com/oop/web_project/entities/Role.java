package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/* This class keeps information about different roles users can have like manager, admin, etc. Each customer may only have one role. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Role_id")
    private long id;

    @Column(name = "Role_name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    @OneToMany(mappedBy = "role")
    private List<Customer> customers;

    @ManyToMany
    @JoinTable(name = "Role_permission", joinColumns = @JoinColumn(name = "Role_id"), inverseJoinColumns = @JoinColumn(name = "Permission_id"))
    private List<Permission> permissions;
}
