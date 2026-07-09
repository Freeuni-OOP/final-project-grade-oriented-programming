package com.oop.web_project.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PageRequest {
    @NotNull
    private Integer page;
    @NotNull
    private Integer size;
    @NotNull
    @NotEmpty
    private String sortBy;
    @NotNull
    @NotEmpty
    private String sortDirection;
}
