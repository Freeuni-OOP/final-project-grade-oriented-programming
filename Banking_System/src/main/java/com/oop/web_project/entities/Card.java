package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/* This class keeps information about cards. Each card is attached to an account. An account may have several cards. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Cards")
public class Card {

    @Column(name = "Card_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "Card_type", nullable = false)
    private CardType type;

    @ManyToOne()
    @JoinColumn(name = "Brand_id", nullable = false)
    private CardBrand brand;

    @ManyToOne()
    @JoinColumn(name = "Account_id", nullable = false)
    private Account account;

    @Column(name = "Spending_limit")
    private int spendingLimit;

    @Column(name = "Expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "Pan_masked", nullable = false, unique = true)
    private String panMasked;

    @Column(name = "Pan_token", nullable = false, unique = true)
    private String panToken;

    @Column(name = "Is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "card")
    private List<CardBalance> balances;
}
