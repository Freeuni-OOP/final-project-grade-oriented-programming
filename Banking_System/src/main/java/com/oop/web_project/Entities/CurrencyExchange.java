package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* This class keeps information about currency exchange rates. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CurrencyExchange {
    private long id;
    private Currency from;
    private Currency to;
    private BigDecimal rate;
    private LocalDateTime timeStamp;
}
