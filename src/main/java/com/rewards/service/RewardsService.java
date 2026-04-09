package com.rewards.service;

import com.rewards.dto.MonthlyPoints;
import com.rewards.dto.RewardSummary;
import com.rewards.dto.TransactionRequest;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.exception.TransactionNotFoundException;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardsService {

    private static final BigDecimal FIFTY   = new BigDecimal("50");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final TransactionRepository transactionRepository;

    /**
     * Calculates reward points for a single transaction amount.
     * - $0–$50:   0 points
     * - $50–$100: 1 point per dollar over $50
     * - Over $100: 50 points + 2 points per dollar over $100
     *
     * @param amount the transaction amount
     * @return points earned
     * @throws IllegalArgumentException if amount is null or negative
     */
    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction amount cannot be null or negative");
        }
        if (amount.compareTo(HUNDRED) > 0) {
            return 50 + amount.subtract(HUNDRED).setScale(0, RoundingMode.FLOOR).intValue() * 2;
        }
        if (amount.compareTo(FIFTY) > 0) {
            return amount.subtract(FIFTY).setScale(0, RoundingMode.FLOOR).intValue();
        }
        return 0;
    }

    /**
     * Returns all transactions in the database.
     *
     * @return list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Returns reward summaries for all customers, sorted by customer ID.
     *
     * @return list of reward summaries
     */
    public List<RewardSummary> getAllCustomerRewards() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId))
                .entrySet().stream()
                .map(e -> buildRewardSummary(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(RewardSummary::customerId))
                .toList();
    }

    /**
     * Returns the reward summary for a specific customer.
     *
     * @param customerId the customer ID (case-insensitive)
     * @return reward summary for the customer
     * @throws CustomerNotFoundException if no transactions exist for the given ID
     */
    public RewardSummary getRewardsByCustomerId(String customerId) {
        List<Transaction> transactions = transactionRepository.findByCustomerIdIgnoreCase(customerId);
        if (transactions.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }
        return buildRewardSummary(customerId, transactions);
    }

    /**
     * Persists a new transaction to the database.
     *
     * @param request the transaction details
     * @return the saved transaction
     */
    public Transaction createTransaction(TransactionRequest request) {
        return transactionRepository.save(new Transaction(
                request.transactionId(),
                request.customerId(),
                request.customerName(),
                request.amount(),
                request.transactionDate()
        ));
    }

    /**
     * Deletes a transaction by ID.
     *
     * @param transactionId the ID of the transaction to delete
     * @throws TransactionNotFoundException if no transaction exists with the given ID
     */
    public void deleteTransaction(String transactionId) {
        if (!transactionRepository.existsById(transactionId)) {
            throw new TransactionNotFoundException(transactionId);
        }
        transactionRepository.deleteById(transactionId);
    }

    /**
     * Builds a RewardSummary for a customer from their transactions.
     * Monthly points are sorted chronologically.
     */
    private RewardSummary buildRewardSummary(String customerId, List<Transaction> transactions) {
        String customerName = transactions.get(0).getCustomerName();

        List<MonthlyPoints> monthlyPoints = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().getMonth(),
                        Collectors.summingInt(t -> calculatePoints(t.getAmount()))
                ))
                .entrySet().stream()
                .map(e -> new MonthlyPoints(e.getKey().name(), e.getValue()))
                .sorted(Comparator.comparingInt(mp -> java.time.Month.valueOf(mp.month()).getValue()))
                .toList();

        int totalPoints = monthlyPoints.stream().mapToInt(MonthlyPoints::points).sum();

        return new RewardSummary(customerId, customerName, monthlyPoints, totalPoints);
    }
}
