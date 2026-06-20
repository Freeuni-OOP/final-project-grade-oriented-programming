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
public class CardWithdrawRequest {

    private BigDecimal amountToWithdraw;
    private String currencyCode;

}
