package com.github.kisilko.eagle_bank.account;

class BankAccountNotFoundException extends RuntimeException {
    public BankAccountNotFoundException(Long accountId) {
        super("Account %s not found".formatted(accountId));
    }
}
