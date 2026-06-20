package com.oop.web_project.restController;


import com.oop.web_project.dto.requests.CardCreationRequest;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.dto.responses.CardBalanceResponse;
import com.oop.web_project.dto.responses.CardResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.mapping.CardApiMapper;
import com.oop.web_project.mapping.CardBalanceApiMapper;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/card")
public class CardRestController {

    private final CardService cardService;
    private final AccountService accountService;
    private final CardApiMapper cardApiMapper;
    private final AccountApiMapper accountApiMapper;
    private final CardBalanceApiMapper cardBalanceApiMapper;

    public CardRestController(CardService cardService, AccountService accountService, CardApiMapper cardApiMapper,
                              AccountApiMapper accountApiMapper, CardBalanceApiMapper cardBalanceApiMapper) {
        this.cardService = cardService;
        this.accountService = accountService;
        this.cardApiMapper = cardApiMapper;
        this.accountApiMapper = accountApiMapper;
        this.cardBalanceApiMapper = cardBalanceApiMapper;
    }


    @GetMapping("/{card-id}")
    public ResponseEntity<CardResponse> getCardById(@NotNull @PathVariable("card-id") Long cardId) {

        Card card = cardService.selectCardById(cardId);

        return ResponseEntity.ok(cardApiMapper.toCardResponse(card));
    }

    @GetMapping("/{card-id}/account")
    public ResponseEntity<AccountSummaryResponse> getCardAccount(@NotNull @PathVariable("card-id") Long cardId) {

        Account account = accountService.selectAccountByCardId(cardId);

        return ResponseEntity.ok(accountApiMapper.toAccountSummaryResponse(account));
    }

    @GetMapping("/{card-id}/balances")
    public ResponseEntity<List<CardBalanceResponse>> getCardBalances(@NotNull @PathVariable("card-id") Long cardId) {

        Card card = cardService.selectCardById(cardId);

        return ResponseEntity.ok(cardService.selectCardBalances(cardId)
                .stream().map(cardBalanceApiMapper::toCardBalanceResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<String> createCard(@Valid @RequestBody CardCreationRequest cardCreationRequest) {

        cardService.createCard(cardApiMapper.toCardOnCardCreation(cardCreationRequest));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Card has been successfully created!");
    }

    @PatchMapping("/{card-id}/currencies/{currency-code}")
    public ResponseEntity<CardResponse> addCurrencyToCard(@Valid @PathVariable("card-id") Long cardId,
                                                          @Valid @PathVariable("currency-code") String currencyCode) {

        cardService.addCurrencyToCard(cardId, currencyCode);

        return ResponseEntity.ok(cardApiMapper.toCardResponse(cardService.selectCardById(cardId)));
    }



    @PatchMapping("{card-id}/activate")
    public ResponseEntity<String> activateCard(@NotNull @PathVariable("card-id") Long cardId) {

        cardService.activateCard(cardId);

        return ResponseEntity.ok("Card has been successfully activated!");
    }

    @PatchMapping("{card-id}/deactivate")
    public ResponseEntity<String> deactivateCard(@NotNull @PathVariable("card-id") Long cardId) {

        cardService.deactivateCard(cardId);

        return ResponseEntity.ok("Card has been successfully deactivated!");
    }


}
