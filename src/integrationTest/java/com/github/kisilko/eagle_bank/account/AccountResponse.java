package com.github.kisilko.eagle_bank.account;

public record AccountResponse(
        Long id,
        Long userId,
        String accountType,
        String currency
) { }
