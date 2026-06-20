package com.oop.web_project.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyExchangeRequest {
    private BigDecimal amount;
    private String fromCurrencyCode;
    private String toCurrencyCode;
}
