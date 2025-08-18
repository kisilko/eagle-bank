package com.github.kisilko.eagle_bank.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BankAccountCreateRequest {

        @NotNull(message = "Missing required field: userId")
        Long userId;

        @NotBlank(message = "Missing required field: accountType")
        String accountType;

        @NotBlank(message = "Missing required field: currency")
        String currency;
}
