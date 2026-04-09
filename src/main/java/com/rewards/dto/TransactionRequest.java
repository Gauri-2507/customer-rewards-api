package com.rewards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for creating a new transaction.
 * All fields are required.
 *
 * @param transactionId   unique identifier for the transaction
 * @param customerId      identifier of the customer making the purchase
 * @param customerName    full name of the customer
 * @param amount          purchase amount (must be greater than 0)
 * @param transactionDate date the transaction occurred
 */
public record TransactionRequest(
        @NotBlank(message = "Transaction ID must not be blank")
        String transactionId,

        @NotBlank(message = "Customer ID must not be blank")
        String customerId,

        @NotBlank(message = "Customer name must not be blank")
        String customerName,

        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Transaction date must not be null")
        LocalDate transactionDate
) {}
