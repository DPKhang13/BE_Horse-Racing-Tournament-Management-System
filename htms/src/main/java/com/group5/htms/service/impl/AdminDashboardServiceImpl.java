package com.group5.htms.service.impl;

import com.group5.htms.dto.dashboard.response.AdminDashboardSummaryResponse;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Withdrawals;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.enums.WithdrawalStatus;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WithdrawalsRepository;
import com.group5.htms.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int RECENT_TRANSACTION_LIMIT = 10;

    private final UsersRepository usersRepository;
    private final RacesRepository racesRepository;
    private final BetsRepository betsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final WithdrawalsRepository withdrawalsRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        List<WalletTransactions> transactions = walletTransactionsRepository.findAll();
        List<Withdrawals> paidWithdrawals = withdrawalsRepository
                .findByStatusIgnoreCaseOrderByCreatedAtDesc(WithdrawalStatus.PAID.getValue());

        BigDecimal totalDeposit = transactions.stream()
                .filter(this::isCompletedTopUp)
                .map(WalletTransactions::getCashAmount)
                .reduce(BigDecimal.ZERO, this::addMoney);

        BigDecimal totalWithdrawal = paidWithdrawals.stream()
                .map(Withdrawals::getNetCashAmount)
                .reduce(BigDecimal.ZERO, this::addMoney);

        long activeRaces = racesRepository.findAll()
                .stream()
                .filter(this::isActiveRace)
                .count();

        return AdminDashboardSummaryResponse.builder()
                .totalUsers(usersRepository.count())
                .totalRaces(racesRepository.count())
                .activeRaces(activeRaces)
                .totalBets(betsRepository.count())
                .totalSuccessfulDepositAmount(totalDeposit)
                .totalCompletedWithdrawalAmount(totalWithdrawal)
                .pendingWithdrawalCount(countPendingWithdrawals())
                .netCashFlow(totalDeposit.subtract(totalWithdrawal))
                .cashFlowByDay(buildCashFlowByDay(transactions, paidWithdrawals))
                .recentTransactions(getRecentTransactions())
                .build();
    }

    private long countPendingWithdrawals() {
        return withdrawalsRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc(WithdrawalStatus.PENDING.getValue()).size();
    }

    private List<AdminDashboardSummaryResponse.RecentTransaction> getRecentTransactions() {
        return walletTransactionsRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_TRANSACTION_LIMIT))
                .stream()
                .map(this::toRecentTransaction)
                .toList();
    }

    private List<AdminDashboardSummaryResponse.CashFlowByDay> buildCashFlowByDay(
            List<WalletTransactions> transactions,
            List<Withdrawals> paidWithdrawals
    ) {
        Map<LocalDate, CashFlowAccumulator> byDate = new TreeMap<>(Comparator.reverseOrder());

        transactions.stream()
                .filter(this::isCompletedTopUp)
                .forEach(tx -> byDate
                        .computeIfAbsent(toDate(tx.getUpdatedAt() == null ? tx.getCreatedAt() : tx.getUpdatedAt()), ignored -> new CashFlowAccumulator())
                        .addDeposit(tx.getCashAmount()));

        paidWithdrawals.forEach(withdrawal -> byDate
                .computeIfAbsent(toDate(withdrawal.getPaidAt() == null ? withdrawal.getCreatedAt() : withdrawal.getPaidAt()), ignored -> new CashFlowAccumulator())
                .addWithdrawal(withdrawal.getNetCashAmount()));

        return byDate.entrySet()
                .stream()
                .map(entry -> AdminDashboardSummaryResponse.CashFlowByDay.builder()
                        .date(entry.getKey())
                        .depositAmount(entry.getValue().depositAmount)
                        .withdrawalAmount(entry.getValue().withdrawalAmount)
                        .netCashFlow(entry.getValue().depositAmount.subtract(entry.getValue().withdrawalAmount))
                        .build())
                .toList();
    }

    private AdminDashboardSummaryResponse.RecentTransaction toRecentTransaction(WalletTransactions tx) {
        Users user = tx.getUsers();
        return AdminDashboardSummaryResponse.RecentTransaction.builder()
                .txId(tx.getId())
                .userId(user == null ? null : user.getId())
                .username(user == null ? null : user.getUsername())
                .userFullName(user == null ? null : user.getFullName())
                .txType(tx.getTxType())
                .cashAmount(tx.getCashAmount())
                .pointsAmount(tx.getPointsAmount())
                .status(tx.getStatus())
                .refType(tx.getRefType())
                .refId(tx.getRefId())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }

    private boolean isCompletedTopUp(WalletTransactions tx) {
        return tx != null
                && WalletTransactionType.TOPUP.getValue().equalsIgnoreCase(tx.getTxType())
                && WalletTransactionStatus.COMPLETED.getValue().equalsIgnoreCase(tx.getStatus());
    }

    private boolean isActiveRace(Races race) {
        return race != null
                && !RaceStatus.COMPLETED.equalsValue(race.getStatus())
                && !RaceStatus.CANCELLED.equalsValue(race.getStatus());
    }

    private LocalDate toDate(Instant instant) {
        Instant value = instant == null ? Instant.now() : instant;
        return value.atZone(VIETNAM_ZONE).toLocalDate();
    }

    private BigDecimal addMoney(BigDecimal left, BigDecimal right) {
        return safeMoney(left).add(safeMoney(right));
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private class CashFlowAccumulator {
        private BigDecimal depositAmount = BigDecimal.ZERO;
        private BigDecimal withdrawalAmount = BigDecimal.ZERO;

        private void addDeposit(BigDecimal amount) {
            this.depositAmount = addMoney(this.depositAmount, amount);
        }

        private void addWithdrawal(BigDecimal amount) {
            this.withdrawalAmount = addMoney(this.withdrawalAmount, amount);
        }
    }
}