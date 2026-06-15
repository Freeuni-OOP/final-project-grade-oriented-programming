package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/* This class keeps information about different transaction types. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionType {
    private long id;
    private String name;
}
