package com.oop.web_project.restController;


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
import jakarta.validation.constraints.NotNull;
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


    @GetMapping("/{card_id}")
    public ResponseEntity<CardResponse> getCardById(@NotNull @PathVariable Long card_id) {

        Card card = cardService.selectCardById(card_id);

        return ResponseEntity.ok(cardApiMapper.toCardResponse(card));
    }

    @GetMapping("/{card_id}/account")
    public ResponseEntity<AccountSummaryResponse> getCardAccount(@NotNull @PathVariable Long card_id) {

        Account account = accountService.selectAccountByCardId(card_id);

        return ResponseEntity.ok(accountApiMapper.toAccountSummaryResponse(account));
    }

    @GetMapping("{card_id}/balances")
    public ResponseEntity<List<CardBalanceResponse>> getCardBalances(@NotNull @PathVariable Long card_id) {

        Card card = cardService.selectCardById(card_id);

        return ResponseEntity.ok(cardService.selectCardBalances(card_id)
                .stream().map(cardBalanceApiMapper::toCardBalanceResponse)
                .toList());
    }


}
