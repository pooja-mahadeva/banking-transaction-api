package com.pooja.bankingapi.controller;

import com.pooja.bankingapi.dto.AmountRequest;
import com.pooja.bankingapi.dto.CreateAccountRequest;
import com.pooja.bankingapi.dto.TransferRequest;
import com.pooja.bankingapi.model.Account;
import com.pooja.bankingapi.model.Transaction;
import com.pooja.bankingapi.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.getOwnerName(), request.getInitialBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(Map.of("accountNumber", accountNumber, "balance", balance));
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable String accountNumber,
                                            @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, request.getAmount()));
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable String accountNumber,
                                             @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.withdraw(accountNumber, request.getAmount()));
    }

    @PostMapping("/{accountNumber}/transfer")
    public ResponseEntity<Map<String, String>> transfer(@PathVariable String accountNumber,
                                                          @Valid @RequestBody TransferRequest request) {
        accountService.transfer(accountNumber, request.getToAccountNumber(), request.getAmount());
        return ResponseEntity.ok(Map.of(
                "message", "Transfer completed successfully",
                "from", accountNumber,
                "to", request.getToAccountNumber()
        ));
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getTransactionHistory(accountNumber));
    }
}
