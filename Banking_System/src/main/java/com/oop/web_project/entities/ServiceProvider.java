package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/* This class keeps information about different service providers. Each transaction can have up to one service provider. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Service_providers")
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Service_provider_id")
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "Service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    @Column(name = "Service_provider_name", nullable = false, unique = true)
    private String name;

    @Column(name = "Api_endpoint", nullable = false, unique = true)
    private String apiEndpoint;

    @Column(name = "Is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "serviceProvider")
    private List<Transaction> transactions;

}
