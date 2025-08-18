package com.github.kisilko.eagle_bank.user;

public record UserResponse(
        Long id,
        String name,
        String email
) { }
