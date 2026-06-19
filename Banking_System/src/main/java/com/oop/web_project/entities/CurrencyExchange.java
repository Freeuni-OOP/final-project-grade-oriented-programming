package com.oop.web_project.entities;

import jakarta.persistence.*;
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
@Entity
@Table(name = "Currency_exchanges")
public class CurrencyExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Currency_exchange_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "From_currency_id", nullable = false)
    private Currency from;

    @ManyToOne
    @JoinColumn(name = "To_currency_id", nullable = false)
    private Currency to;

    @Column(name = "Exchange_rate", nullable = false)
    private BigDecimal rate;

    @Column(name = "Exchange_time_stamp", nullable = false)
    private LocalDateTime timeStamp;
}
