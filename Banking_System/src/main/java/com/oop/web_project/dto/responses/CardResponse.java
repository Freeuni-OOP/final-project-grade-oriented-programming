package com.oop.web_project.dto.responses;

import com.oop.web_project.entities.CardBrand;
import com.oop.web_project.entities.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardResponse {

    private CardType type;
    private CardBrand brand;
    private BigDecimal spendingLimit;
    private LocalDate expirationDate;
    private String panMasked;
    private String panToken;
    private boolean isActive;
    private List<CardBalanceResponse> cardBalances;
}
