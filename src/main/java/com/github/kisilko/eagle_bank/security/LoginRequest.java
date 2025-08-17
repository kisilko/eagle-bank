package com.github.kisilko.eagle_bank.security;

record LoginRequest(
        String email,
        String password
) { }
