package com.oop.web_project.Entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* This class keeps information about different categories of services. Each provider can provide one service. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceCategory {
    private long id;
    private String name;
}
