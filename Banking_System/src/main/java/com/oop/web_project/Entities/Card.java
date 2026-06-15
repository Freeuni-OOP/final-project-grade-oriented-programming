package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/* This class keeps information about cards. Each card is attached to an account. An account may have several cards. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Card {
    private long id;
    private CardType type;
    private CardBrand brand;
    private Account account;
    private int spendingLimit;
    private LocalDate expirationDate;
    private String panMasked;
    private String panToken;
    private boolean isActive;
}
