package com.oop.web_project.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerFilterRequest {
    private String firstName;
    private String lastName;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
