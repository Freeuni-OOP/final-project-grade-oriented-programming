package com.oop.web_project.dto.requests;

import com.oop.web_project.entities.CardBrand;
import com.oop.web_project.entities.CardType;
import jakarta.validation.constraints.*;
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
    @NotNull(message = "Card type should be picked.")
    private CardType cardType;

   @NotNull(message = "Card brand should be picked.")
    private CardBrand cardBrand;

    @NotNull(message = "There should be a spending limit.")
    @Positive(message = "Spending limit should be positive.")
    @Min(value = 100, message = "Spending limit should be at least 100.")
    @Max(value = 100000, message = "Spending limit should be at most 100000.")
    private BigDecimal spendingLimit;

    @NotBlank(message = "Pan number should not be blank.")
    @Size(min = 12, max = 19, message = "Pan number should be between 12 and 19 digits.")
    @Pattern(regexp = "\\d+", message = "Pan number must contain only digits.")
    private String pan;
}
