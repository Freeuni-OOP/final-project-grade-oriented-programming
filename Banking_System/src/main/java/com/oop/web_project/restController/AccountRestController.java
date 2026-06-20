package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.AccountCreationRequest;
import com.oop.web_project.dto.requests.CardCreationRequest;
import com.oop.web_project.dto.responses.AccountProfileResponse;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.mapping.CardApiMapper;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.html.HTMLHtmlElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/account")
@Tag(name = "Account", description = "Operations for managing accounts")
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

    @Operation(summary = "Create a new account", description = "Creates a new bank account from the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<String> createAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account creation details", required = true,
                    content = @Content(schema = @Schema(implementation = AccountCreationRequest.class)))
            @RequestBody AccountCreationRequest request) {
        Account account = accountMapper.toAccount(request);
        accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body("Account has been successfully created.");
    }

    @Operation(summary = "Create a card for an account", description = "Creates a new card and links it to the specified account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Card created successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID or request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping("/{account-id}/cards")
    public ResponseEntity<String> createCard(
            @Parameter(description = "ID of the account to link the card to", required = true, example = "1")
            @Valid @PathVariable("account-id") Long accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card creation details", required = true,
                    content = @Content(schema = @Schema(implementation = CardCreationRequest.class)))
            @Valid @RequestBody CardCreationRequest cardCreationRequest) {
        cardService.createCard(cardApiMapper.toCardOnCardCreation(cardCreationRequest), accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Card has been successfully created!");
    }

    @Operation(summary = "Activate an account", description = "Sets the account status to active")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account activated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PatchMapping("/{account-id}/activate")
    public ResponseEntity<String> ActivateAccount(
            @Parameter(description = "ID of the account to activate", required = true, example = "1")
            @PathVariable("account-id") Long accountId) {
        accountService.activateAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body("Account has been successfully activated.");
    }

    @Operation(summary = "Deactivate an account", description = "Sets the account status to inactive")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deactivated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid account ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PatchMapping("/{account-id}/deactivate")
    public ResponseEntity<String> DeactivateAccount(
            @Parameter(description = "ID of the account to deactivate", required = true, example = "1")
            @PathVariable("account-id") Long accountId) {
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
    public ResponseEntity<AccountProfileResponse> getAccountWithId(
            @Parameter(description = "ID of the account to retrieve", required = true, example = "1")
            @PathVariable("account-id") Long accountId) {
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
    public ResponseEntity<String> deleteAccount(
            @Parameter(description = "ID of the account to delete", required = true, example = "1")
            @PathVariable("account-id") Long accountId) {
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
    public ResponseEntity<List<AccountSummaryResponse>> getAccountsByEmail(
            @Parameter(description = "Email of the customer whose accounts to retrieve", required = true, example = "jane@example.com")
            @RequestParam("customerEmail") String customerEmail) {
        List<Account> accounts = accountService.selectAccountsByCustomerEmail(customerEmail);
        List<AccountSummaryResponse> summaryResponses = new ArrayList<>();
        for (Account account : accounts) {
            summaryResponses.add(accountMapper.toAccountSummaryResponse(account));
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
    public ResponseEntity<List<AccountProfileResponse>> getAccountWithCustomerId(
            @Parameter(description = "ID of the customer whose accounts to retrieve", required = true, example = "1")
            @PathVariable("customer-id") Long customerId) {
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
    public ResponseEntity<String> updateAccount(
            @Parameter(description = "ID of the account to update", required = true, example = "1")
            @PathVariable("account-id") Long accountID,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New account name", required = true,
                    content = @Content(schema = @Schema(type = "string")))
            @RequestBody String accountName) {
        accountService.updateAccount(accountID, accountName);
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
    public ResponseEntity<String> registerCustomerToAccount(
            @Parameter(description = "ID of the account", required = true, example = "1")
            @PathVariable("account-id") Long accountId,
            @Parameter(description = "ID of the customer to link", required = true, example = "1")
            @PathVariable("customer-id") Long customerId) {
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
    public ResponseEntity<BigDecimal> getAccountBalanceByCurrency(
            @Parameter(description = "ID of the account", required = true, example = "1")
            @PathVariable("account-id") long accountID,
            @Parameter(description = "Currency code to convert balance to", required = true, example = "USD")
            @RequestParam("currencyCode") String currencyCode) {
        BigDecimal balance = accountService.getAccountBalanceByCurrency(accountID, currencyCode);
        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }
}