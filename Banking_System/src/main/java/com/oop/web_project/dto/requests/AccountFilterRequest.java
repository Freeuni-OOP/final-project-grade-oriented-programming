package com.oop.web_project.dto.requests;

import com.oop.web_project.entities.AccountCategory;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountFilterRequest {
    private String name;
    private AccountCategory category;
    private LocalDate dateOpened;
    private Boolean isActive;
}
