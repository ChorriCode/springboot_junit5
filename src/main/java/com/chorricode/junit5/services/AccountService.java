package com.chorricode.junit5.services;

import com.chorricode.junit5.models.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account findById(Long accountId);
    int TotalTransferCheck(Long bankId);
    BigDecimal balanceCheck(Long accountId);
    void transfer(Long sourceBankId, Long targetBankId, BigDecimal transferAmount, Long bankId);

    List<Account> findAll();
    Account save(Account account);
    void deleteById(Long accountId);
}
