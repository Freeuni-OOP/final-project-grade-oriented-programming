package com.oop.web_project.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/* This class keeps information about different categories of services. Each provider can provide one service. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Service_categories")
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Service_category_id")
    private long id;

    @Column(name = "Service_category_name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "serviceCategory")
    private List<ServiceProvider> providers;
}
