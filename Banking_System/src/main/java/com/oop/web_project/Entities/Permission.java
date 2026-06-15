package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* This class keeps information about all the permissions. Each role can have several permissions and vice versa. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Permission {
    private long id;
    private String name;
}
