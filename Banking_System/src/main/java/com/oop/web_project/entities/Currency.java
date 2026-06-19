package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/* This class keeps information about different currencies. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Currencies")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Currency_id")
    private Long id;

    @Column(name = "Currency_code", nullable = false, unique = true)
    private String code;

    @Column(name = "Currency_name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "currency")
    private List<CardBalance> cardBalances;

    @OneToMany(mappedBy = "from")
    private List<CurrencyExchange> exchangesFrom;

    @OneToMany(mappedBy = "to")
    private List<CurrencyExchange> exchangesTo;

    @OneToMany(mappedBy = "currency")
    private List<Transaction> transactions;
}
