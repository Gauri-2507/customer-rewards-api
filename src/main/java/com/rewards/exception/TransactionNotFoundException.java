package com.rewards.exception;

import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends RewardsException {

    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found: " + transactionId, HttpStatus.NOT_FOUND);
    }
}
