package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/* This class keeps information about all the permissions. Each role can have several permissions and vice versa. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Permission_id")
    private Long id;

    @Column(name = "Permission_name", nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles;
}
