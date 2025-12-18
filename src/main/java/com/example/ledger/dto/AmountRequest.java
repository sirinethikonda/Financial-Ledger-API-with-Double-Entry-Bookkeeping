package com.example.ledger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AmountRequest {

    @NotNull(message = "Account ID is mandatory")
    private Long accountId;

    @NotNull(message = "Amount is mandatory")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;
}