package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/* This class keeps information about accounts. Each customer may have several accounts. Several customers can also have a shared account. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {
    private long id;
    private String name;
    private AccountCategory category;
    private BigDecimal balance;
    private Currency currency;
    private LocalDate dateOpened;
    private boolean isActive;
}
