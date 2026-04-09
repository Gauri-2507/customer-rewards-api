package com.rewards.service;

import com.rewards.dto.RewardSummary;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardsService rewardsService;

    private List<Transaction> sampleTransactions;

    @BeforeEach
    void setUp() {
        sampleTransactions = List.of(
                new Transaction("TXN001", "C001", "Alice Johnson", new BigDecimal("120.00"), LocalDate.of(2026, 1, 5)),
                new Transaction("TXN002", "C001", "Alice Johnson", new BigDecimal("75.00"),  LocalDate.of(2026, 1, 20)),
                new Transaction("TXN003", "C001", "Alice Johnson", new BigDecimal("200.00"), LocalDate.of(2026, 2, 14)),
                new Transaction("TXN004", "C001", "Alice Johnson", new BigDecimal("30.00"),  LocalDate.of(2026, 3, 10)),
                new Transaction("TXN005", "C002", "Michael Chen",  new BigDecimal("110.00"), LocalDate.of(2026, 1, 8)),
                new Transaction("TXN006", "C002", "Michael Chen",  new BigDecimal("60.00"),  LocalDate.of(2026, 2, 22)),
                new Transaction("TXN007", "C002", "Michael Chen",  new BigDecimal("130.00"), LocalDate.of(2026, 3, 15)),
                new Transaction("TXN008", "C003", "Sarah Williams",new BigDecimal("45.00"),  LocalDate.of(2026, 1, 12)),
                new Transaction("TXN009", "C003", "Sarah Williams",new BigDecimal("150.00"), LocalDate.of(2026, 2, 18)),
                new Transaction("TXN010", "C003", "Sarah Williams",new BigDecimal("95.00"),  LocalDate.of(2026, 3, 25))
        );
    }

    @Nested
    class CalculatePoints {

        @Test
        void zeroAmount_shouldReturnZeroPoints() {
            assertEquals(0, rewardsService.calculatePoints(BigDecimal.ZERO));
        }

        @Test
        void nullAmount_shouldThrowIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> rewardsService.calculatePoints(null));
        }

        @Test
        void negativeAmount_shouldThrowIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> rewardsService.calculatePoints(new BigDecimal("-10.00")));
        }

        @Test
        void amountExactly50_shouldReturnZeroPoints() {
            assertEquals(0, rewardsService.calculatePoints(new BigDecimal("50.00")));
        }

        @Test
        void amountOf51_shouldReturnOnePoint() {
            assertEquals(1, rewardsService.calculatePoints(new BigDecimal("51.00")));
        }

        @Test
        void amountOf75_shouldReturn25Points() {
            assertEquals(25, rewardsService.calculatePoints(new BigDecimal("75.00")));
        }

        @Test
        void amountExactly100_shouldReturn50Points() {
            assertEquals(50, rewardsService.calculatePoints(new BigDecimal("100.00")));
        }

        @Test
        void amountOf120_shouldReturn90Points() {
            assertEquals(90, rewardsService.calculatePoints(new BigDecimal("120.00")));
        }

        @Test
        void amountOf200_shouldReturn250Points() {
            assertEquals(250, rewardsService.calculatePoints(new BigDecimal("200.00")));
        }

        @Test
        void amountWithFractionalCents_shouldFloorCorrectly() {
            assertEquals(90, rewardsService.calculatePoints(new BigDecimal("120.99")));
        }
    }

    @Nested
    class GetRewardsByCustomerId {

        @Test
        void validCustomer_shouldReturnCorrectName() {
            when(transactionRepository.findByCustomerIdIgnoreCase("C001"))
                    .thenReturn(sampleTransactions.subList(0, 4));

            RewardSummary summary = rewardsService.getRewardsByCustomerId("C001");
            assertEquals("Alice Johnson", summary.customerName());
        }

        @Test
        void validCustomer_shouldReturnCorrectTotalPoints() {
            when(transactionRepository.findByCustomerIdIgnoreCase("C001"))
                    .thenReturn(sampleTransactions.subList(0, 4));

            RewardSummary summary = rewardsService.getRewardsByCustomerId("C001");
            assertEquals(365, summary.totalPoints());
        }

        @Test
        void monthlyPoints_shouldBeSortedChronologically() {
            when(transactionRepository.findByCustomerIdIgnoreCase("C001"))
                    .thenReturn(sampleTransactions.subList(0, 4));

            RewardSummary summary = rewardsService.getRewardsByCustomerId("C001");
            var months = summary.monthlyPoints();
            assertEquals("JANUARY",  months.get(0).month());
            assertEquals("FEBRUARY", months.get(1).month());
            assertEquals("MARCH",    months.get(2).month());
        }

        @Test
        void caseInsensitiveLookup_shouldWork() {
            when(transactionRepository.findByCustomerIdIgnoreCase("c001"))
                    .thenReturn(sampleTransactions.subList(0, 4));

            RewardSummary summary = rewardsService.getRewardsByCustomerId("c001");
            assertEquals("Alice Johnson", summary.customerName());
        }

        @Test
        void nonExistentCustomer_shouldThrowCustomerNotFoundException() {
            when(transactionRepository.findByCustomerIdIgnoreCase("NOPE"))
                    .thenReturn(List.of());

            var ex = assertThrows(CustomerNotFoundException.class,
                    () -> rewardsService.getRewardsByCustomerId("NOPE"));
            assertTrue(ex.getMessage().contains("NOPE"));
        }
    }

    @Nested
    class GetAllCustomerRewards {

        @Test
        void shouldReturnOneEntryPerCustomer() {
            when(transactionRepository.findAll()).thenReturn(sampleTransactions);

            var rewards = rewardsService.getAllCustomerRewards();
            assertEquals(3, rewards.size());
        }

        @Test
        void resultsShouldBeSortedByCustomerId() {
            when(transactionRepository.findAll()).thenReturn(sampleTransactions);

            var rewards = rewardsService.getAllCustomerRewards();
            assertEquals("C001", rewards.get(0).customerId());
            assertEquals("C002", rewards.get(1).customerId());
            assertEquals("C003", rewards.get(2).customerId());
        }

        @Test
        void eachCustomer_shouldHaveNonNegativeTotalPoints() {
            when(transactionRepository.findAll()).thenReturn(sampleTransactions);

            rewardsService.getAllCustomerRewards().forEach(r ->
                    assertTrue(r.totalPoints() >= 0,
                            "Total points should be non-negative for " + r.customerId()));
        }
    }

    @Nested
    class GetAllTransactions {

        @Test
        void shouldReturnAllTransactions() {
            when(transactionRepository.findAll()).thenReturn(sampleTransactions);

            assertEquals(10, rewardsService.getAllTransactions().size());
        }
    }
}
