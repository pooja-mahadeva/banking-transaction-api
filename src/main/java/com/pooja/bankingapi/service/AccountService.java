package com.pooja.bankingapi.service;

import com.pooja.bankingapi.exception.AccountNotFoundException;
import com.pooja.bankingapi.exception.InsufficientBalanceException;
import com.pooja.bankingapi.model.Account;
import com.pooja.bankingapi.model.Transaction;
import com.pooja.bankingapi.repository.AccountRepository;
import com.pooja.bankingapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account createAccount(String ownerName, BigDecimal initialBalance) {
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, ownerName, initialBalance);
        return accountRepository.save(account);
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public BigDecimal getBalance(String accountNumber) {
        return getAccount(accountNumber).getBalance();
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        transactionRepository.save(new Transaction(
                accountNumber, Transaction.Type.DEPOSIT, amount, account.getBalance(), null));

        return account;
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(accountNumber);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        transactionRepository.save(new Transaction(
                accountNumber, Transaction.Type.WITHDRAWAL, amount, account.getBalance(), null));

        return account;
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        Account fromAccount = getAccount(fromAccountNumber);
        Account toAccount = getAccount(toAccountNumber);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(fromAccountNumber);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionRepository.save(new Transaction(
                fromAccountNumber, Transaction.Type.TRANSFER_OUT, amount, fromAccount.getBalance(), toAccountNumber));
        transactionRepository.save(new Transaction(
                toAccountNumber, Transaction.Type.TRANSFER_IN, amount, toAccount.getBalance(), fromAccountNumber));
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        // Ensure the account exists before returning (possibly empty) history
        getAccount(accountNumber);
        return transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber);
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }
}
