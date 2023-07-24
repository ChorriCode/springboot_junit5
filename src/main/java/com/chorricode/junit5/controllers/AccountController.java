package com.chorricode.junit5.controllers;

import com.chorricode.junit5.dtos.TransferDTO;
import com.chorricode.junit5.models.Account;
import com.chorricode.junit5.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    AccountService accountService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Account> getAccountAll() {
        return accountService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account save(@RequestBody Account account) {
        return accountService.save(account);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable(name = "accountId") Long accountId) {
        Account account = null;
        try {
            account = accountService.findById(accountId);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> accountTransfer(@RequestBody TransferDTO transferDTO) {

        accountService.transfer(transferDTO.getSourceAccountId(), transferDTO.getTargetAccountId(), transferDTO.getAmount(), transferDTO.getBankId());
        return ResponseEntity.ok(
                Map.of(
                "date", LocalDate.now().toString(),
                "status", "ok",
                "message", "Transfer ok",
                "transfer", transferDTO
                )
        );
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long accountId) {
        accountService.deleteById(accountId);
    }
}
