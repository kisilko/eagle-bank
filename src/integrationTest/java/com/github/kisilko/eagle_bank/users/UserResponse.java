package com.github.kisilko.eagle_bank.users;

public record UserResponse(
        Long id,
        String name,
        String email
) { }
