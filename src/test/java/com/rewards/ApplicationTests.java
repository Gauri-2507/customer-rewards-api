package com.rewards;

import com.rewards.dto.RewardSummary;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepository;
import com.rewards.service.RewardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationTests {

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

	// ─── Points Calculation Tests ───────────────────────────────────

	@Test
	void calculatePoints_whenAmountIsBelow50_shouldReturnZeroPoints() {
		assertEquals(0, rewardsService.calculatePoints(new BigDecimal("30.00")));
	}

	@Test
	void calculatePoints_whenAmountIsExactly50_shouldReturnZeroPoints() {
		assertEquals(0, rewardsService.calculatePoints(new BigDecimal("50.00")));
	}

	@Test
	void calculatePoints_whenAmountIsBetween50And100_shouldReturn1PointPerDollar() {
		assertEquals(25, rewardsService.calculatePoints(new BigDecimal("75.00")));
	}

	@Test
	void calculatePoints_whenAmountIsExactly100_shouldReturn50Points() {
		assertEquals(50, rewardsService.calculatePoints(new BigDecimal("100.00")));
	}

	@Test
	void calculatePoints_whenAmountIs120_shouldReturn90Points() {
		assertEquals(90, rewardsService.calculatePoints(new BigDecimal("120.00")));
	}

	@Test
	void calculatePoints_whenAmountIs200_shouldReturn250Points() {
		assertEquals(250, rewardsService.calculatePoints(new BigDecimal("200.00")));
	}

	// ─── Get All Transactions Tests ──────────────────────────────────

	@Test
	void getAllTransactions_shouldReturnTenTransactions() {
		when(transactionRepository.findAll()).thenReturn(sampleTransactions);

		List<Transaction> transactions = rewardsService.getAllTransactions();
		assertEquals(10, transactions.size());
	}

	@Test
	void getAllTransactions_shouldContainAllThreeCustomers() {
		when(transactionRepository.findAll()).thenReturn(sampleTransactions);

		List<Transaction> transactions = rewardsService.getAllTransactions();
		assertTrue(transactions.stream().anyMatch(t -> t.getCustomerId().equals("C001")));
		assertTrue(transactions.stream().anyMatch(t -> t.getCustomerId().equals("C002")));
		assertTrue(transactions.stream().anyMatch(t -> t.getCustomerId().equals("C003")));
	}

	// ─── Get All Customer Rewards Tests ──────────────────────────────

	@Test
	void getAllCustomerRewards_shouldReturnThreeCustomers() {
		when(transactionRepository.findAll()).thenReturn(sampleTransactions);

		List<RewardSummary> rewards = rewardsService.getAllCustomerRewards();
		assertEquals(3, rewards.size());
	}

	@Test
	void getAllCustomerRewards_shouldContainAliceJohnson() {
		when(transactionRepository.findAll()).thenReturn(sampleTransactions);

		List<RewardSummary> rewards = rewardsService.getAllCustomerRewards();
		assertTrue(rewards.stream().anyMatch(r -> r.customerName().equals("Alice Johnson")));
	}

	// ─── Get Rewards By Customer ID Tests ────────────────────────────

	@Test
	void getRewardsByCustomerId_whenValidCustomer_shouldReturnCorrectName() {
		when(transactionRepository.findByCustomerIdIgnoreCase("C001"))
				.thenReturn(sampleTransactions.subList(0, 4));

		RewardSummary summary = rewardsService.getRewardsByCustomerId("C001");
		assertEquals("Alice Johnson", summary.customerName());
	}

	@Test
	void getRewardsByCustomerId_whenValidCustomer_shouldReturnCorrectTotalPoints() {
		when(transactionRepository.findByCustomerIdIgnoreCase("C001"))
				.thenReturn(sampleTransactions.subList(0, 4));

		RewardSummary summary = rewardsService.getRewardsByCustomerId("C001");
		assertEquals(365, summary.totalPoints());
	}

	@Test
	void getRewardsByCustomerId_whenValidCustomer_shouldReturnThreeMonths() {
		when(transactionRepository.findByCustomerIdIgnoreCase("C001"))
				.thenReturn(sampleTransactions.subList(0, 4));

		RewardSummary summary = rewardsService.getRewardsByCustomerId("C001");
		assertEquals(3, summary.monthlyPoints().size());
	}

	@Test
	void getRewardsByCustomerId_whenInvalidCustomer_shouldThrowException() {
		when(transactionRepository.findByCustomerIdIgnoreCase("C999"))
				.thenReturn(List.of());

		assertThrows(CustomerNotFoundException.class,
				() -> rewardsService.getRewardsByCustomerId("C999"));
	}

	@Test
	void getRewardsByCustomerId_whenInvalidCustomer_shouldThrowCorrectMessage() {
		when(transactionRepository.findByCustomerIdIgnoreCase("C999"))
				.thenReturn(List.of());

		CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
				() -> rewardsService.getRewardsByCustomerId("C999"));
		assertTrue(ex.getMessage().contains("C999"));
	}
}
