package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/* This class keeps information about all the different brands of cards. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Card_brands")
public class CardBrand {

    @Column(name = "Card_brand_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "Card_brand_name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "brand")
    private List<Card> cards;
}
