package com.rewards.dto;

import java.util.List;

/**
 * Response object containing a customer's reward points breakdown.
 *
 * @param customerId    unique customer identifier
 * @param customerName  full name of the customer
 * @param monthlyPoints reward points earned per month, sorted chronologically
 * @param totalPoints   total points earned across all months
 */
public record RewardSummary(
        String customerId,
        String customerName,
        List<MonthlyPoints> monthlyPoints,
        int totalPoints) {
}
