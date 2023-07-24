package com.chorricode.junit5;

import com.chorricode.junit5.exceptions.AmountNotEnoughException;
import com.chorricode.junit5.models.Account;
import com.chorricode.junit5.models.Bank;
import com.chorricode.junit5.repositories.AccountRepository;
import com.chorricode.junit5.repositories.BankRepository;
import com.chorricode.junit5.services.AccountService;
import com.chorricode.junit5.services.AccountServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class Junit5ApplicationTests {

    /* next are Mockito annotations
    @Mock
    AccountRepository accountRepository;
    @Mock
    BankRepository bankRepository;
    @InjectMocks
    AccountServiceImpl accountService;
*/

    // next are Springboot Mockito annotations
    @MockBean
    AccountRepository accountRepository;
    @MockBean
    BankRepository bankRepository;
    @Autowired
    AccountService accountService;

    @Test
    void testTransferBetweenAccounts2() {
		when(accountRepository.findById(1L)).thenReturn(Data.createAccount001());
		when(accountRepository.findById(2L)).thenReturn(Data.createAccount002());
		when(bankRepository.findById(1L)).thenReturn(Data.createBank());

		BigDecimal sourceBalance = accountService.balanceCheck(1L);
		BigDecimal targetBalance = accountService.balanceCheck(2L);
		assertEquals("1000", sourceBalance.toPlainString());
		assertEquals("2000", targetBalance.toPlainString());

        accountService.transfer(1L, 2L, new BigDecimal("100"),1L);

        sourceBalance = accountService.balanceCheck(1L);
        targetBalance = accountService.balanceCheck(2L);

        assertEquals("900", sourceBalance.toPlainString());
        assertEquals("2100", targetBalance.toPlainString());
        int totalTransfers = accountService.TotalTransferCheck(1L);
        assertEquals(1, totalTransfers);

        // now we check how many times the next methods are called
        verify(accountRepository, times(3)).findById(1L);
        verify(accountRepository, times(3)).findById(2L);
        verify(accountRepository, times(2)).save(any(Account.class));

        verify(bankRepository, times(2)).findById(1L);
        verify(bankRepository).save(any(Bank.class));

        verify(accountRepository, never()).findAll();

    }

    @Test
    void testTransferBetweenAccountsBalanceNotEnough() {
        when(accountRepository.findById(1L)).thenReturn(Data.createAccount001());
        when(accountRepository.findById(2L)).thenReturn(Data.createAccount002());
        when(bankRepository.findById(1L)).thenReturn(Data.createBank());

        BigDecimal sourceBalance = accountService.balanceCheck(1L);
        BigDecimal targetBalance = accountService.balanceCheck(2L);
        assertEquals("1000", sourceBalance.toPlainString());
        assertEquals("2000", targetBalance.toPlainString());

        assertThrows(AmountNotEnoughException.class, () -> {
            accountService.transfer(1L, 2L, new BigDecimal("10001"),1L);
        });

        sourceBalance = accountService.balanceCheck(1L);
        targetBalance = accountService.balanceCheck(2L);

        assertEquals("1000", sourceBalance.toPlainString());
        assertEquals("2000", targetBalance.toPlainString());
        int totalTransfers = accountService.TotalTransferCheck(1L);
        assertEquals(0, totalTransfers);

        // now we check how many times the next methods are called
        verify(accountRepository, times(3)).findById(1L);
        verify(accountRepository, times(2)).findById(2L);
        verify(accountRepository, never()).save(any(Account.class));

        verify(bankRepository, times(1)).findById(1L);
        verify(bankRepository, never()).save(any(Bank.class));

        verify(accountRepository, never()).findAll();
    }

    @Test
    void testCompareTwoSameAccounts() {
        when(accountRepository.findById(1L)).thenReturn(Data.createAccount001());
        Account account001 = accountService.findById(1L);
        Account accountOther001 = accountService.findById(1L);
        assertSame(account001, accountOther001);
        assertTrue(account001 == accountOther001);
        assertEquals("Andrés", account001.getPersonName());
        assertEquals("Andrés", accountOther001.getPersonName());
        verify(accountRepository, times(2)).findById(1L);
    }

    @Test
    void testAccountFindAll() {
        // GIVEN
        List<Account> accountListResponseInTest = Arrays.asList(
                Data.createAccount001().orElseThrow(),
                Data.createAccount002().orElseThrow()
        );
        when(accountRepository.findAll()).thenReturn(accountListResponseInTest);

        // WHEN
        List<Account> accountList = accountService.findAll();

        // THEN
        assertFalse(accountList.isEmpty());
        assertEquals(2, accountList.size());
        assertTrue(accountList.contains(Data.createAccount002().orElseThrow()));

        verify(accountRepository).findAll();
    }

    @Test
    void testAccountSave() {
        // GIVEN
        Account account = new Account(null, "Pepe", new BigDecimal("3000"));
        when(accountRepository.save(any())).then(invocationOnMock -> {
            Account accountMocked = invocationOnMock.getArgument(0);
            accountMocked.setId(3L);
            return accountMocked;
        });

        // WHEN
        Account accountSaved = accountService.save(account);

        // THEN
        assertEquals("Pepe", accountSaved.getPersonName());
        assertEquals(3, accountSaved.getId());
        assertEquals("3000", accountSaved.getBalance().toPlainString());

        verify(accountRepository).save(any());
    }
}
