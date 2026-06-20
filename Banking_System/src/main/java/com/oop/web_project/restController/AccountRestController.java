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
        return ResponseEntity.status(HttpStatus.CREATED).body("Account has been successfully created.");
    }

    @PatchMapping("/{account-id}/activate")
    public ResponseEntity<String> ActivateAccount(@PathVariable("account-id") Long accountId) {
        accountService.activateAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully activated.");
    }

    @PatchMapping("/{account-id}/deactivate")
    public ResponseEntity<String> DeactivateAccount(@PathVariable("account-id") Long accountId) {
        accountService.deactivateAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully deactivated.");
    }

    @GetMapping("/{account-id}")
    public ResponseEntity<AccountProfileResponse> getAccountWithId(@PathVariable("account-id") Long accountId) {
        Account account = accountService.selectAccountById(accountId);
        AccountProfileResponse response = accountMapper.toProfileResponse(account);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{account-id}")
    public ResponseEntity<String> deleteAccount(@PathVariable("account-id") Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully deleted.");
    }

    @GetMapping
    public ResponseEntity<List<AccountSummaryResponse>> getAccountsByEmail(@RequestParam("customerEmail") String customerEmail) {
        List<Account> accounts = accountService.selectAccountsByCustomerEmail(customerEmail);
        List<AccountSummaryResponse> summaryResponses = new ArrayList<>();
        for(Account account : accounts) {
            summaryResponses.add(accountMapper.toAccountSummaryResponse(account));
        }
        return ResponseEntity.status(HttpStatus.OK).body(summaryResponses);
    }

    @GetMapping("/customer/{customer-id}")
    public ResponseEntity<List<AccountProfileResponse>> getAccountWithCustomerId(@PathVariable("customer-id") Long customerId) {
        List<Account> accounts = accountService.selectAccountsByCustomerId(customerId);
        List<AccountProfileResponse> profileResponses = new ArrayList<>();
        for(Account account : accounts) {
            profileResponses.add(accountMapper.toProfileResponse(account));
        }
        return ResponseEntity.status(HttpStatus.OK).body(profileResponses);
    }

    @PutMapping("/{account-id}")
    public ResponseEntity<String> updateAccount(@PathVariable("account-id") Long accountID, @RequestBody String accountName) {
        accountService.updateAccount(accountID, accountName);
        return ResponseEntity.status(HttpStatus.OK).body("successfully updated");
    }

    @PutMapping("/{account-id}/customers/{customer-id}")
    public ResponseEntity<String> registerCustomerToAccount(@PathVariable("account-id") Long accountId, @PathVariable("customer-id") Long customerId) {
        accountService.registerCustomerToAccount(accountId, customerId);
        return ResponseEntity.ok("Successfully registered");
    }

    @GetMapping("/{account-id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalanceByCurrency(@PathVariable("account-id") long accountID, @RequestParam("currencyCode") String currencyCode) {
        BigDecimal balance = accountService.getAccountBalanceByCurrency(accountID, currencyCode);
        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }
}
