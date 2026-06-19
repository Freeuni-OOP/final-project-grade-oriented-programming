package com.oop.web_project.dto.responses;


import com.oop.web_project.entities.AccountCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountProfileResponse {

    private String name;
    private AccountCategory category;
    private LocalDate dateOpened;
    private boolean isActive;
}
