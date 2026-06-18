package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/* This class keeps information about different currency balances of a card. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Card_balances")
public class CardBalance {

    @Column(name = "Card_balance_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "Card_balance_amount", nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "Card_id", nullable = false)
    private Card card;

    @ManyToOne
    @JoinColumn(name = "Currency_id", nullable = false)
    private Currency currency;
}
