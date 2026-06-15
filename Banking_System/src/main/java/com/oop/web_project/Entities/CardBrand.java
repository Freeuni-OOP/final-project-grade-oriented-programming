package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* This class keeps information about all the different brands of cards. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardBrand {
    private long id;
    private String name;
}
