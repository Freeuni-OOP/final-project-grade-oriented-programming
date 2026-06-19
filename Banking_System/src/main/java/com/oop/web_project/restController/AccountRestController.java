package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.AccountCreationRequest;
import com.oop.web_project.dto.responses.AccountProfileResponse;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.services.AccountService;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.html.HTMLHtmlElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/account")
public class AccountRestController {
    private final AccountService accountService;
    private final AccountApiMapper accountMapper;

    public AccountRestController(AccountService accountService, AccountApiMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody AccountCreationRequest request) {
        Account account = accountMapper.toAccount(request);
        accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body("successfully created");
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> ActivateAccount(@PathVariable long id) {
        accountService.activateAccount(id);
        return ResponseEntity.status(HttpStatus.OK).body("successfully activated");
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> DeactivateAccount(@PathVariable long id) {
        accountService.deactivateAccount(id);
        return ResponseEntity.status(HttpStatus.OK).body("successfully deactivated");
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountProfileResponse> getAccountWithId(@PathVariable long id) {
        Account account = accountService.selectAccountById(id);
        AccountProfileResponse response = accountMapper.toProfileResponse(account);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.status(HttpStatus.OK).body("successfully deleted");
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<AccountProfileResponse>> getAccountWithCustomerId(@PathVariable long id) {
        List<Account> accounts = accountService.selectAccountsByCustomerId(id);
        List<AccountProfileResponse> profileResponses = new ArrayList<>();
        for(Account account : accounts) {
            profileResponses.add(accountMapper.toProfileResponse(account));
        }
        return ResponseEntity.status(HttpStatus.OK).body(profileResponses);
    }

    @PutMapping("/{id}/{name}")
    public ResponseEntity<String> updateAccount(@PathVariable long id, @PathVariable String name) {
        accountService.updateAccount(id, name);
        return ResponseEntity.status(HttpStatus.OK).body("successfully updated");
    }

    @PutMapping("/{accountId}/customers/{customerId}")
    public ResponseEntity<String> registerCustomerToAccount(@PathVariable("accountId") long accountId, @PathVariable("customerId") long customerId) {
        accountService.registerCustomerToAccount(accountId, customerId);
        return ResponseEntity.ok("Successfully registered");
    }

    @GetMapping("/{accountId}/currency/{currencyName}")
    public ResponseEntity<BigDecimal> getAccountBalanceByCurrency(@PathVariable long accountID, @PathVariable String currencyName) {
        BigDecimal balance = accountService.getAccountBalanceByCurrency(accountID, currencyName);
        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }
}
