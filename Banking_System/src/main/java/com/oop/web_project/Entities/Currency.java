package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* This class keeps information about different currencies. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Currency {
    private long id;
    private String code;
    private String name;
}
