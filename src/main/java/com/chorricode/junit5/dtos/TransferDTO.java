package com.chorricode.junit5.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferDTO {
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private Long bankId;
}
