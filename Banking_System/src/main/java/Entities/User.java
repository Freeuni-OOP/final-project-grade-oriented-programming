package Entities;


import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class User {


    public User(long id, String firstName, String lastName, BigDecimal accountBalance) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountBalance = accountBalance;
    }

    private long id;

    private String firstName;

    private String lastName;

    private BigDecimal accountBalance;
}
