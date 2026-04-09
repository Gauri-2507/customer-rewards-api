package com.rewards.repository;

import com.rewards.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Transaction} entities.
 * Extends JpaRepository to provide standard CRUD operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Finds all transactions for a given customer ID, case-insensitively.
     *
     * @param customerId the customer ID to search for
     * @return list of matching transactions, or empty list if none found
     */
    List<Transaction> findByCustomerIdIgnoreCase(String customerId);
}
