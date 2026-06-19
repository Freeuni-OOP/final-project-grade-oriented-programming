package com.oop.web_project.dto.requests;

import com.oop.web_project.entities.CardBrand;
import com.oop.web_project.entities.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardCreationRequest {
    private CardType cardType;
    private CardBrand cardBrand;
    private BigDecimal spendingLimit;
}
