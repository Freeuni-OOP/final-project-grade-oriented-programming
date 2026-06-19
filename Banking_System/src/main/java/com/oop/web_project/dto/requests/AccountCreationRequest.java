package com.oop.web_project.dto.requests;

import com.oop.web_project.entities.AccountCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreationRequest {
    private String accountName;
    private AccountCategory category;
}
