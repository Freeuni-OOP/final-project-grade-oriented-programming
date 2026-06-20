package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.entities.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountSummaryApiMapper {
    public AccountSummaryResponse toAccountSummaryResponse(Account account){
        AccountSummaryResponse response = new AccountSummaryResponse();
        response.setName(account.getName());
        response.setCategory(account.getCategory());
        response.setDateOpened(account.getDateOpened());
        response.setActive(account.isActive());
        return response;
    }
}