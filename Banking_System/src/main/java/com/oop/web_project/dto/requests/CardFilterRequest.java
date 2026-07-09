package com.oop.web_project.dto.requests;

import com.oop.web_project.entities.CardBrand;
import com.oop.web_project.entities.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardFilterRequest {
    private CardType type;
    private CardBrand brand;
    private BigDecimal spendingLimit;
    private LocalDate expirationDate;
}
