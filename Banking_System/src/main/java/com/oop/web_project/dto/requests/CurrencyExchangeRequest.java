package com.oop.web_project.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "There should be an amount to exchange.")
    @Positive(message = "Exchange amount should be positive.")
    private BigDecimal amount;

    @NotBlank(message = "From currency code should not be blank.")
    private String fromCurrencyCode;

    @NotBlank(message = "To currency code should not be blank.")
    private String toCurrencyCode;
}
