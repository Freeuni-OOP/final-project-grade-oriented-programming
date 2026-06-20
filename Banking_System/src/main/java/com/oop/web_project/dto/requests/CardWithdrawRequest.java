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
public class CardWithdrawRequest {

    @NotNull(message = "There should be an amount to withdraw.")
    @Positive(message = "Withdraw amount should be positive.")
    private BigDecimal amountToWithdraw;

    @NotBlank(message = "Currency code should not be blank.")
    private String currencyCode;

}
