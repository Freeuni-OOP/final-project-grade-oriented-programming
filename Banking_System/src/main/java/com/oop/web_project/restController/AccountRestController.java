package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.AccountCreationRequest;
import com.oop.web_project.dto.requests.CardCreationRequest;
import com.oop.web_project.dto.responses.AccountProfileResponse;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.mapping.AccountSummaryApiMapper;
import com.oop.web_project.mapping.CardApiMapper;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Account", description = "Operations for managing accounts")
@Validated
public class AccountRestController {

    private final AccountService accountService;
    private final AccountApiMapper accountMapper;
    private final CardService cardService;
    private final CardApiMapper cardApiMapper;
    private final AccountSummaryApiMapper accountSummaryApiMapper;

    public AccountRestController(AccountService accountService, AccountApiMapper accountMapper,
                                 CardService cardService, CardApiMapper cardApiMapper, AccountSummaryApiMapper accountSummaryApiMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.cardService = cardService;
        this.cardApiMapper = cardApiMapper;
        this.accountSummaryApiMapper = accountSummaryApiMapper;
    }

    @Operation(summary = "Create a new account", description = "Creates a new bank account from the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody @Valid AccountCreationRequest request) {
        Account account = accountMapper.toAccount(request);
        accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body("Account has been successfully created.");
    }

    @Operation(summary = "Create a card for an account", description = "Creates a new card and links it to the specified account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Card created successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID or request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "A card with the same PAN already exists", content = @Content)
    })
    @PostMapping("/{account-id}/cards")
    public ResponseEntity<String> createCard(
            @NotNull @Positive @PathVariable("account-id") Long accountId,
            @Valid @RequestBody CardCreationRequest cardCreationRequest) {
        cardService.createCard(cardApiMapper.toCardOnCardCreation(cardCreationRequest), accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Card has been successfully created!");
    }

    @Operation(summary = "Activate an account", description = "Sets the account status to active")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account activated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "406", description = "Account is already active", content = @Content)
    })
    @PatchMapping("/{account-id}/activate")
    public ResponseEntity<String> ActivateAccount(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        accountService.activateAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully activated.");
    }

    @Operation(summary = "Deactivate an account", description = "Sets the account status to inactive")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deactivated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "406", description = "Account is already inactive", content = @Content)
    })
    @PatchMapping("/{account-id}/deactivate")
    public ResponseEntity<String> DeactivateAccount(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        accountService.deactivateAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully deactivated.");
    }

    @Operation(summary = "Get account by ID", description = "Retrieves the full profile of an account by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AccountProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/{account-id}")
    public ResponseEntity<AccountProfileResponse> getAccountWithId(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        Account account = accountService.selectAccountById(accountId);
        AccountProfileResponse response = accountMapper.toProfileResponse(account);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Delete an account", description = "Permanently removes an account from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deleted successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @DeleteMapping("/{account-id}")
    public ResponseEntity<String> deleteAccount(@NotNull @Positive @PathVariable("account-id") Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully deleted.");
    }

    @Operation(summary = "Get accounts by customer email", description = "Returns a list of account summaries associated with the given customer email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AccountSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email parameter", content = @Content),
            @ApiResponse(responseCode = "404", description = "No accounts found for this email", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<AccountSummaryResponse>> getAccountsByEmail(@NotBlank @Email @RequestParam("customerEmail") String customerEmail) {
        List<Account> accounts = accountService.selectAccountsByCustomerEmail(customerEmail);
        List<AccountSummaryResponse> summaryResponses = new ArrayList<>();
        for(Account account : accounts) {
            summaryResponses.add(accountSummaryApiMapper.toAccountSummaryResponse(account));
        }
        return ResponseEntity.status(HttpStatus.OK).body(summaryResponses);
    }

    @Operation(summary = "Get accounts by customer ID", description = "Returns a list of full account profiles linked to the given customer ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AccountProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "No accounts found for this customer", content = @Content)
    })
    @GetMapping("/customer/{customer-id}")
    public ResponseEntity<List<AccountProfileResponse>> getAccountWithCustomerId(@NotNull @Positive @PathVariable("customer-id") Long customerId) {
        List<Account> accounts = accountService.selectAccountsByCustomerId(customerId);
        List<AccountProfileResponse> profileResponses = new ArrayList<>();
        for (Account account : accounts) {
            profileResponses.add(accountMapper.toProfileResponse(account));
        }
        return ResponseEntity.status(HttpStatus.OK).body(profileResponses);
    }

    @Operation(summary = "Update account name", description = "Updates the name of an existing account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID or request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PutMapping("/{account-id}")
    public ResponseEntity<String> updateAccount(@NotNull @Positive @PathVariable("account-id") Long accountId, @NotBlank @RequestBody String accountName) {
        accountService.updateAccount(accountId, accountName);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully updated!");
    }

    @Operation(summary = "Register customer to account", description = "Links an existing customer to an existing account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer registered to account successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID or customer ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account or customer not found", content = @Content)
    })
    @PutMapping("/{account-id}/customers/{customer-id}")
    public ResponseEntity<String> registerCustomerToAccount(@NotNull @Positive @PathVariable("account-id") Long accountId, @NotNull @Positive @PathVariable("customer-id") Long customerId) {
        accountService.registerCustomerToAccount(accountId, customerId);
        return ResponseEntity.ok("Account to the Customer has been registered successfully!");
    }

    @Operation(summary = "Get account balance by currency", description = "Returns the account balance converted to the specified currency")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                    content = @Content(schema = @Schema(type = "number"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID or currency code", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/{account-id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalanceByCurrency(@NotNull @Positive @PathVariable("account-id") Long accountId, @NotBlank @RequestParam("currencyCode") String currencyCode) {
        BigDecimal balance = accountService.getAccountBalanceByCurrency(accountId, currencyCode);
        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }
}