package com.pooja.bankingapi.service;

import com.pooja.bankingapi.exception.AccountNotFoundException;
import com.pooja.bankingapi.exception.InsufficientBalanceException;
import com.pooja.bankingapi.model.Account;
import com.pooja.bankingapi.model.Transaction;
import com.pooja.bankingapi.repository.AccountRepository;
import com.pooja.bankingapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, transactionRepository);
    }

    @Test
    void createAccount_savesAccountWithGeneratedNumber() {
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.createAccount("Pooja Mahadeva", new BigDecimal("100.00"));

        assertNotNull(result.getAccountNumber());
        assertTrue(result.getAccountNumber().startsWith("ACC-"));
        assertEquals("Pooja Mahadeva", result.getOwnerName());
        assertEquals(new BigDecimal("100.00"), result.getBalance());
    }

    @Test
    void getAccount_throwsWhenAccountDoesNotExist() {
        when(accountRepository.findByAccountNumber("ACC-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount("ACC-UNKNOWN"));
    }

    @Test
    void deposit_increasesBalanceAndRecordsTransaction() {
        Account account = new Account("ACC-12345678", "Pooja Mahadeva", new BigDecimal("50.00"));
        when(accountRepository.findByAccountNumber("ACC-12345678")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.deposit("ACC-12345678", new BigDecimal("25.00"));

        assertEquals(new BigDecimal("75.00"), result.getBalance());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(Transaction.Type.DEPOSIT, captor.getValue().getType());
        assertEquals(new BigDecimal("25.00"), captor.getValue().getAmount());
    }

    @Test
    void withdraw_decreasesBalanceWhenSufficientFunds() {
        Account account = new Account("ACC-12345678", "Pooja Mahadeva", new BigDecimal("100.00"));
        when(accountRepository.findByAccountNumber("ACC-12345678")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.withdraw("ACC-12345678", new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), result.getBalance());
    }

    @Test
    void withdraw_throwsInsufficientBalanceException_whenAmountExceedsBalance() {
        Account account = new Account("ACC-12345678", "Pooja Mahadeva", new BigDecimal("30.00"));
        when(accountRepository.findByAccountNumber("ACC-12345678")).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class,
                () -> accountService.withdraw("ACC-12345678", new BigDecimal("50.00")));

        // Balance must remain unchanged after a failed withdrawal
        assertEquals(new BigDecimal("30.00"), account.getBalance());
    }

    @Test
    void transfer_movesFundsBetweenTwoAccounts() {
        Account from = new Account("ACC-FROM001", "Pooja Mahadeva", new BigDecimal("200.00"));
        Account to = new Account("ACC-TO00002", "John Doe", new BigDecimal("50.00"));

        when(accountRepository.findByAccountNumber("ACC-FROM001")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("ACC-TO00002")).thenReturn(Optional.of(to));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        accountService.transfer("ACC-FROM001", "ACC-TO00002", new BigDecimal("75.00"));

        assertEquals(new BigDecimal("125.00"), from.getBalance());
        assertEquals(new BigDecimal("125.00"), to.getBalance());

        // Two transaction records should be created: one TRANSFER_OUT, one TRANSFER_IN
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_throwsInsufficientBalanceException_andDoesNotMoveFunds() {
        Account from = new Account("ACC-FROM001", "Pooja Mahadeva", new BigDecimal("10.00"));
        Account to = new Account("ACC-TO00002", "John Doe", new BigDecimal("50.00"));

        when(accountRepository.findByAccountNumber("ACC-FROM001")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("ACC-TO00002")).thenReturn(Optional.of(to));

        assertThrows(InsufficientBalanceException.class,
                () -> accountService.transfer("ACC-FROM001", "ACC-TO00002", new BigDecimal("100.00")));

        assertEquals(new BigDecimal("10.00"), from.getBalance());
        assertEquals(new BigDecimal("50.00"), to.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
