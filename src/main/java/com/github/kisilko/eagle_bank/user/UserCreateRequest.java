package com.github.kisilko.eagle_bank.user;

import jakarta.validation.constraints.NotBlank;

record UserCreateRequest(
        @NotBlank(message = "Missing required field: name")
        String name,
        @NotBlank(message = "Missing required field: email")
        String email,
        @NotBlank(message = "Missing required field: password")
        String password
) { }
