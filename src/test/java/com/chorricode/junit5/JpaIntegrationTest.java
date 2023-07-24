package com.chorricode.junit5;

import com.chorricode.junit5.models.Account;
import com.chorricode.junit5.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class JpaIntegrationTest {
    @Autowired
    AccountRepository accountRepository;

    @Test
    void testAccountFindById() {
         Optional<Account> accountOptional = accountRepository.findById(1L);
         assertTrue(accountOptional.isPresent());
         assertEquals("Andrés", accountOptional.orElseThrow().getPersonName());
    }

    @Test
    void testAccountFindByPersonName() {
         Optional<Account> accountOptional = accountRepository.findByPersonName("Andrés");
         assertTrue(accountOptional.isPresent());
         assertEquals("Andrés", accountOptional.orElseThrow().getPersonName());
         assertEquals("1000.00", accountOptional.orElseThrow().getBalance().toPlainString());
    }
    @Test
    void testAccountFindByPersonNameNotFound() {
        Optional<Account> accountOptional = accountRepository.findByPersonName("DontExist");
        assertThrows(NoSuchElementException.class, accountOptional::orElseThrow);
        assertFalse(accountOptional.isPresent());
    }

    @Test
    void testAccountFindAll() {
        List<Account> accountList = accountRepository.findAll();
        assertFalse(accountList.isEmpty());
        assertEquals(2, accountList.size());
    }

    @Test
    void testAccountSave() {
        // GIVEN
        Account account = new Account(null, "Pepe", new BigDecimal("3000"));
        // WHEN
        Account accountSaved = accountRepository.save(account);
        // THEN
        assertEquals(account.getPersonName(), accountSaved.getPersonName());
        assertEquals(account.getBalance(), accountSaved.getBalance());
    }

    @Test
    void testAccountUpdate() {
        // GIVEN
        Account account = new Account(null, "Pepe", new BigDecimal("3000"));
        Account accountSaved = accountRepository.save(account);
        // WHEN
        accountSaved.setBalance(new BigDecimal("3800"));
        Account accountUpdated = accountRepository.save(accountSaved);
        // THEN
        assertEquals(accountSaved.getPersonName(), accountUpdated.getPersonName());
        assertEquals(accountSaved.getBalance(), accountUpdated.getBalance());
    }

    @Test
    void testAccountDelete() {
        Account account = accountRepository.findById(2L).orElseThrow();
        assertEquals("John",  account.getPersonName());
        accountRepository.delete(account);

        assertThrows(NoSuchElementException.class, () -> {
            accountRepository.findByPersonName("John").orElseThrow();
        });
    }
}
