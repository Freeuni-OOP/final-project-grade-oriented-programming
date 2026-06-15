package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* This class keeps information about different roles users can have like manager, admin, etc. Each customer may only have one role. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role {
    private long id;
    private RoleName name;
}
