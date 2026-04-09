package com.rewards.exception;

import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends RewardsException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId, HttpStatus.NOT_FOUND);
    }
}
