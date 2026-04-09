package com.rewards.controller;

import com.rewards.dto.RewardSummary;
import com.rewards.dto.TransactionRequest;
import com.rewards.model.Transaction;
import com.rewards.service.RewardsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * GET /api/rewards
     * Returns reward summaries for all customers.
     */
    @GetMapping("/rewards")
    public ResponseEntity<List<RewardSummary>> getAllCustomerRewards() {
        return ResponseEntity.ok(rewardsService.getAllCustomerRewards());
    }

    /**
     * GET /api/rewards/{customerId}
     * Returns the reward summary for a specific customer.
     * Returns 404 if the customer ID is not found.
     */
    @GetMapping("/rewards/{customerId}")
    public ResponseEntity<RewardSummary> getRewardsByCustomerId(
            @PathVariable @NotBlank(message = "Customer ID must not be blank") String customerId) {
        return ResponseEntity.ok(rewardsService.getRewardsByCustomerId(customerId));
    }

    /**
     * GET /api/transactions
     * Returns all transactions across all customers.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(rewardsService.getAllTransactions());
    }

    /**
     * POST /api/transactions
     * Creates a new transaction. Returns 201 Created with a Location header.
     */
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Transaction created = rewardsService.createTransaction(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getTransactionId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * DELETE /api/transactions/{transactionId}
     * Deletes a transaction by ID. Returns 204 No Content on success.
     * Returns 404 if the transaction ID is not found.
     */
    @DeleteMapping("/transactions/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String transactionId) {
        rewardsService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }
}
