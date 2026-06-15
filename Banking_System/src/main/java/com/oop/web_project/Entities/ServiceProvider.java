package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
/* This class keeps information about different service providers. Each transaction can have up to one service provider. */
public class ServiceProvider {
    private long id;
    private ServiceCategory serviceCategory;
    private String name;
    private String apiEndpoint;
    private boolean isActive;

}
