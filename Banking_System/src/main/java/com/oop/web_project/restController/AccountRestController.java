package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.AccountCreationRequest;
import com.oop.web_project.dto.requests.CardCreationRequest;
import com.oop.web_project.dto.responses.AccountProfileResponse;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.mapping.CardApiMapper;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/account")
@Validated
public class AccountRestController {
    private final AccountService accountService;
    private final AccountApiMapper accountMapper;
    private final CardService cardService;
    private final CardApiMapper cardApiMapper;

    public AccountRestController(AccountService accountService, AccountApiMapper accountMapper,
                                 CardService cardService, CardApiMapper cardApiMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.cardService = cardService;
        this.cardApiMapper = cardApiMapper;
    }

    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody @Valid AccountCreationRequest request) {
        Account account = accountMapper.toAccount(request);
        accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body("Account has been successfully created.");
    }

    @PostMapping("/{account-id}/cards")
    public ResponseEntity<String> createCard(
            @NotNull @Positive @PathVariable("account-id") Long accountId,
            @Valid @RequestBody CardCreationRequest cardCreationRequest) {

        cardService.createCard(cardApiMapper.toCardOnCardCreation(cardCreationRequest),
                accountId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Card has been successfully created!");
    }


    @PatchMapping("/{account-id}/activate")
    public ResponseEntity<String> ActivateAccount(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        accountService.activateAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully activated.");
    }

    @PatchMapping("/{account-id}/deactivate")
    public ResponseEntity<String> DeactivateAccount(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        accountService.deactivateAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully deactivated.");
    }

    @GetMapping("/{account-id}")
    public ResponseEntity<AccountProfileResponse> getAccountWithId(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        Account account = accountService.selectAccountById(accountId);
        AccountProfileResponse response = accountMapper.toProfileResponse(account);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{account-id}")
    public ResponseEntity<String> deleteAccount(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully deleted.");
    }

    @GetMapping
    public ResponseEntity<List<AccountSummaryResponse>> getAccountsByEmail(@NotBlank @Email @RequestParam("customerEmail") String customerEmail) {
        List<Account> accounts = accountService.selectAccountsByCustomerEmail(customerEmail);
        List<AccountSummaryResponse> summaryResponses = new ArrayList<>();
        for(Account account : accounts) {
            summaryResponses.add(accountMapper.toAccountSummaryResponse(account));
        }
        return ResponseEntity.status(HttpStatus.OK).body(summaryResponses);
    }

    @GetMapping("/customer/{customer-id}")
    public ResponseEntity<List<AccountProfileResponse>> getAccountWithCustomerId(@NotNull @Positive @PathVariable("customer-id") Long customerId) {
        List<Account> accounts = accountService.selectAccountsByCustomerId(customerId);
        List<AccountProfileResponse> profileResponses = new ArrayList<>();
        for(Account account : accounts) {
            profileResponses.add(accountMapper.toProfileResponse(account));
        }
        return ResponseEntity.status(HttpStatus.OK).body(profileResponses);
    }

    @PutMapping("/{account-id}")
    public ResponseEntity<String> updateAccount(@NotNull @Positive @PathVariable("account-id") Long accountId, @NotBlank @RequestBody String accountName) {
        accountService.updateAccount(accountId, accountName);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully updated!");
    }

    @PutMapping("/{account-id}/customers/{customer-id}")
    public ResponseEntity<String> registerCustomerToAccount(@NotNull @Positive @PathVariable("account-id") Long accountId, @NotNull @Positive @PathVariable("customer-id") Long customerId) {
        accountService.registerCustomerToAccount(accountId, customerId);
        return ResponseEntity.ok("Account to the Customer has been registered successfully!");
    }

    @GetMapping("/{account-id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalanceByCurrency(@NotNull @Positive @PathVariable("account-id") Long accountId, @NotBlank @RequestParam("currencyCode") String currencyCode) {
        BigDecimal balance = accountService.getAccountBalanceByCurrency(accountId, currencyCode);
        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }
}
