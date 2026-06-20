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
public class CardTransferRequest {

    @NotNull(message = "There should be a sender id.")
    @Positive(message = "Sender id should be positive.")
    private Long senderCardId;

    @NotNull(message = "There should be a receiver id.")
    @Positive(message = "Receiver id should be positive.")
    private Long receiverCardId;

    @NotNull(message = "There should be an amount to transfer.")
    @Positive(message = "Transfer amount should be positive.")
    private BigDecimal amount;

    @NotBlank(message = "Currency code should not be blank.")
    private String currencyCode;
}
