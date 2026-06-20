package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.*;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.dto.responses.CardBalanceResponse;
import com.oop.web_project.dto.responses.CardResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.mapping.AccountSummaryApiMapper;
import com.oop.web_project.mapping.CardApiMapper;
import com.oop.web_project.mapping.CardBalanceApiMapper;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card")
@Tag(name = "Card", description = "Operations for managing cards")
@Validated
public class CardRestController {

    private final CardService cardService;
    private final AccountService accountService;
    private final CardApiMapper cardApiMapper;
    private final CardBalanceApiMapper cardBalanceApiMapper;
    private final AccountSummaryApiMapper accountSummaryApiMapper;

    public CardRestController(CardService cardService, AccountService accountService, CardApiMapper cardApiMapper,
                              CardBalanceApiMapper cardBalanceApiMapper, AccountSummaryApiMapper accountSummaryApiMapper) {
        this.cardService = cardService;
        this.accountService = accountService;
        this.cardApiMapper = cardApiMapper;
        this.cardBalanceApiMapper = cardBalanceApiMapper;
        this.accountSummaryApiMapper = accountSummaryApiMapper;
    }

    @Operation(summary = "Get card by ID", description = "Retrieves the details of a card by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @GetMapping("/{card-id}")
    public ResponseEntity<CardResponse> getCardById(@NotNull @Positive @PathVariable("card-id") Long cardId) {

        Card card = cardService.selectCardById(cardId);
        return ResponseEntity.ok(cardApiMapper.toCardResponse(card));
    }

    @Operation(summary = "Get account linked to card", description = "Retrieves the account associated with the given card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AccountSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card or account not found", content = @Content)
    })
    @GetMapping("/{card-id}/account")
    public ResponseEntity<AccountSummaryResponse> getCardAccount(@NotNull @Positive @PathVariable("card-id") Long cardId) {

        Account account = accountService.selectAccountByCardId(cardId);

        return ResponseEntity.ok(accountSummaryApiMapper.toAccountSummaryResponse(account));
    }

    @Operation(summary = "Get card balances", description = "Retrieves all currency balances associated with the given card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balances retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CardBalanceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @GetMapping("/{card-id}/balances")
    public ResponseEntity<List<CardBalanceResponse>> getCardBalances(@NotNull @Positive @PathVariable("card-id") Long cardId) {

        Card card = cardService.selectCardById(cardId);
        return ResponseEntity.ok(cardService.selectCardBalances(cardId)
                .stream().map(cardBalanceApiMapper::toCardBalanceResponse)
                .toList());
    }

    @Operation(summary = "Check card expiration", description = "Returns whether the card has expired or not")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expiration status retrieved successfully",
                    content = @Content(schema = @Schema(type = "boolean"))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @GetMapping("/{card-id}/expiration")
    public ResponseEntity<Boolean> getCardExpiration(@NotNull @Positive @PathVariable("card-id") Long cardId) {

        Boolean isExpired = cardService.checkCardExpiration(cardId);
        return ResponseEntity.ok(isExpired);
    }

    @Operation(summary = "Deposit money to card", description = "Deposits a specified amount in a given currency to the card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Money deposited successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID or request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @PostMapping("/{card-id}/deposit")
    public ResponseEntity<String> depositMoneyToCard(@NotNull @Positive @PathVariable("card-id") Long cardId,
                                                     @Valid @RequestBody CardDepositRequest cardDepositRequest) {

        cardService.depositMoney
                (cardId, cardDepositRequest.getAmountToDeposit(), cardDepositRequest.getCurrencyCode());

        return ResponseEntity.ok("Money has been successfully deposited to card!");
    }

    @Operation(summary = "Withdraw money from card", description = "Withdraws a specified amount in a given currency from the card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Money withdrawn successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID or request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @PostMapping("/{card-id}/withdraw")
    public ResponseEntity<String> withdrawMoneyFromCard(@NotNull @Positive @PathVariable("card-id") Long cardId,
                                                     @Valid @RequestBody CardWithdrawRequest cardWithdrawRequest) {

        cardService.withdrawMoney
                (cardId, cardWithdrawRequest.getAmountToWithdraw(), cardWithdrawRequest.getCurrencyCode());

        return ResponseEntity.ok("Money has been successfully withdrawn from card!");
    }

    @Operation(summary = "Transfer money between cards", description = "Transfers a specified amount in a given currency from one card to another")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Money transferred successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Sender or receiver card not found", content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@Valid @RequestBody CardTransferRequest cardTransferRequest) {

        cardService.transferMoney(cardTransferRequest.getSenderCardId(),
                cardTransferRequest.getReceiverCardId(),
                cardTransferRequest.getAmount(),
                cardTransferRequest.getCurrencyCode());
        return ResponseEntity.ok("Money have been successfully transferred!");
    }

    @Operation(summary = "Exchange currency on card", description = "Converts an amount from one currency to another on the specified card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Currency exchanged successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID or request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @PostMapping("/{card-id}/exchange-currency")
    public ResponseEntity<String> exchangeCurrency(@NotNull @Positive @PathVariable("card-id") Long cardId,
            @Valid @RequestBody CurrencyExchangeRequest currencyExchangeRequest) {

        cardService.changeCurrency(cardId, currencyExchangeRequest.getAmount(),
                currencyExchangeRequest.getFromCurrencyCode(), currencyExchangeRequest.getToCurrencyCode());
        return ResponseEntity.ok("Currency transfer was successful!");
    }

    @Operation(summary = "Add currency to card", description = "Adds a new supported currency to the specified card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Currency added successfully",
                    content = @Content(schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID or currency code", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @PatchMapping("/{card-id}/currencies/{currency-code}")
    public ResponseEntity<CardResponse> addCurrencyToCard(@NotNull @Positive @PathVariable("card-id") Long cardId,
                                                          @NotBlank @PathVariable("currency-code") String currencyCode) {

        cardService.addCurrencyToCard(cardId, currencyCode);
        return ResponseEntity.ok(cardApiMapper.toCardResponse(cardService.selectCardById(cardId)));
    }

    @Operation(summary = "Activate a card", description = "Sets the card status to active")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card activated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @PatchMapping("{card-id}/activate")
    public ResponseEntity<String> activateCard(@NotNull @Positive @PathVariable("card-id") Long cardId) {

        cardService.activateCard(cardId);
        return ResponseEntity.ok("Card has been successfully activated!");
    }

    @Operation(summary = "Deactivate a card", description = "Sets the card status to inactive")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card deactivated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @PatchMapping("{card-id}/deactivate")
    public ResponseEntity<String> deactivateCard(@NotNull @Positive @PathVariable("card-id") Long cardId) {

        cardService.deactivateCard(cardId);
        return ResponseEntity.ok("Card has been successfully deactivated!");
    }

    @Operation(summary = "Delete a card", description = "Permanently removes a card from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card deleted successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid card ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    @DeleteMapping("/{card-id}")
    public ResponseEntity<String> deleteCard(@NotNull @Positive @PathVariable("card-id") Long cardId) {

        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Card has been successfully deleted!");
    }
}