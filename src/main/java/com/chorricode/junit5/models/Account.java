package com.chorricode.junit5.models;


import com.chorricode.junit5.exceptions.AmountNotEnoughException;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "person_name")
    private String personName;
    @Column(name = "balance")
    private BigDecimal balance;

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        BigDecimal newAmmount = this.balance.subtract(amount);
        if (newAmmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AmountNotEnoughException("your balance is not enough");
        }
        this.balance = newAmmount;
    }
}
