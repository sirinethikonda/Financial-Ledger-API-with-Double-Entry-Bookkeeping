package com.example.ledger.dto;

import com.example.ledger.model.Account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull(message = "User ID is mandatory")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Account type is mandatory")
    private AccountType type;

    @NotBlank(message = "Currency is mandatory")
    private String currency;
}