package com.github.kisilko.eagle_bank.security;

public record LoginRequest(
        String email,
        String password
) { }
