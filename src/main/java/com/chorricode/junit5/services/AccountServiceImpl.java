package com.chorricode.junit5.services;

import com.chorricode.junit5.models.Account;
import com.chorricode.junit5.models.Bank;
import com.chorricode.junit5.repositories.AccountRepository;
import com.chorricode.junit5.repositories.BankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private AccountRepository accountRepository;
    private BankRepository bankRepository;

    public AccountServiceImpl(AccountRepository accountRepository, BankRepository bankRepository) {
        this.accountRepository = accountRepository;
        this.bankRepository = bankRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Account findById(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public int TotalTransferCheck(Long bankId) {
        Bank bank = bankRepository.findById(bankId).orElseThrow();
        return bank.getTotalTransfers();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal balanceCheck(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        return account.getBalance();
    }

    @Override
    @Transactional
    public void transfer(Long sourceAccountId, Long targetAccountId, BigDecimal transferAmount, Long bankId) {
        Account sourceAccount = accountRepository.findById(sourceAccountId).orElseThrow();
        sourceAccount.debit(transferAmount);
        accountRepository.save(sourceAccount);
        Account targetAccount = accountRepository.findById(targetAccountId).orElseThrow();
        targetAccount.credit(transferAmount);
        accountRepository.save(targetAccount);

        Bank bank = bankRepository.findById(bankId).orElseThrow();
        int  totalTransfer = bank.getTotalTransfers();
        bank.setTotalTransfers(++totalTransfer);
        bankRepository.save(bank);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }
}
