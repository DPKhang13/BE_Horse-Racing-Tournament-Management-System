package com.group5.htms.service.impl;

import com.group5.htms.dto.dashboard.response.AdminDashboardSummaryResponse;
import com.group5.htms.entity.PrizeAwards;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Withdrawals;
import com.group5.htms.enums.PrizeAwardStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.enums.WithdrawalStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.PrizeAwardsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WithdrawalsRepository;
import com.group5.htms.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
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
    private final PrizeAwardsRepository prizeAwardsRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary(
            LocalDate from,
            LocalDate to,
            Integer tournamentId,
            String period
    ) {
        validateDateRange(from, to);
        PeriodBucket periodBucket = normalizePeriod(period);

        List<WalletTransactions> completedTopUps = walletTransactionsRepository.findAll()
                .stream()
                .filter(this::isCompletedTopUp)
                .filter(tx -> isInRange(txInstant(tx), from, to))
                .toList();

        List<Withdrawals> paidWithdrawals = withdrawalsRepository
                .findByStatusIgnoreCaseOrderByCreatedAtDesc(WithdrawalStatus.PAID.getValue())
                .stream()
                .filter(withdrawal -> isInRange(withdrawalInstant(withdrawal), from, to))
                .toList();

        List<PrizeAwards> awardedPrizes = prizeAwardsRepository.findAll()
                .stream()
                .filter(this::isAwardedPrize)
                .filter(prize -> tournamentId == null
                        || prize.getTournaments() != null
                        && tournamentId.equals(prize.getTournaments().getId()))
                .filter(prize -> isInRange(prize.getAwardedAt(), from, to))
                .toList();

        BigDecimal totalDeposit = completedTopUps.stream()
                .map(WalletTransactions::getCashAmount)
                .reduce(BigDecimal.ZERO, this::addMoney);

        BigDecimal totalWithdrawal = paidWithdrawals.stream()
                .map(Withdrawals::getNetCashAmount)
                .reduce(BigDecimal.ZERO, this::addMoney);

        BigDecimal totalPrizeAward = awardedPrizes.stream()
                .map(PrizeAwards::getAmount)
                .reduce(BigDecimal.ZERO, this::addMoney);

        List<Races> races = racesRepository.findAll()
                .stream()
                .filter(race -> tournamentId == null
                        || race.getSchedule() != null
                        && race.getSchedule().getTournaments() != null
                        && tournamentId.equals(race.getSchedule().getTournaments().getId()))
                .toList();

        long activeRaces = races.stream()
                .filter(this::isActiveRace)
                .count();

        long totalBets = betsRepository.findAll()
                .stream()
                .filter(bet -> tournamentId == null
                        || bet.getOption() != null
                        && bet.getOption().getRaces() != null
                        && bet.getOption().getRaces().getSchedule() != null
                        && bet.getOption().getRaces().getSchedule().getTournaments() != null
                        && tournamentId.equals(bet.getOption().getRaces().getSchedule().getTournaments().getId()))
                .count();

        return AdminDashboardSummaryResponse.builder()
                .totalUsers(usersRepository.count())
                .totalRaces(races.size())
                .activeRaces(activeRaces)
                .totalBets(totalBets)
                .totalSuccessfulDepositAmount(totalDeposit)
                .totalCompletedWithdrawalAmount(totalWithdrawal)
                .totalAwardedPrizeAmount(totalPrizeAward)
                .pendingWithdrawalCount(countPendingWithdrawals())
                .netCashFlow(totalDeposit.subtract(totalWithdrawal).subtract(totalPrizeAward))
                .cashFlowByDay(buildCashFlowByDay(completedTopUps, paidWithdrawals, awardedPrizes, periodBucket))
                .recentTransactions(getRecentTransactions(from, to))
                .build();
    }

    private long countPendingWithdrawals() {
        return withdrawalsRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc(WithdrawalStatus.PENDING.getValue()).size();
    }

    private List<AdminDashboardSummaryResponse.RecentTransaction> getRecentTransactions(LocalDate from, LocalDate to) {
        return walletTransactionsRepository.findAll()
                .stream()
                .filter(tx -> isInRange(tx.getCreatedAt(), from, to))
                .sorted(Comparator.comparing(WalletTransactions::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(RECENT_TRANSACTION_LIMIT)
                .map(this::toRecentTransaction)
                .toList();
    }

    private List<AdminDashboardSummaryResponse.CashFlowByDay> buildCashFlowByDay(
            List<WalletTransactions> completedTopUps,
            List<Withdrawals> paidWithdrawals,
            List<PrizeAwards> awardedPrizes,
            PeriodBucket periodBucket
    ) {
        Map<LocalDate, CashFlowAccumulator> byDate = new TreeMap<>(Comparator.reverseOrder());

        completedTopUps.forEach(tx -> byDate
                .computeIfAbsent(toBucketDate(txInstant(tx), periodBucket), ignored -> new CashFlowAccumulator())
                .addDeposit(tx.getCashAmount()));

        paidWithdrawals.forEach(withdrawal -> byDate
                .computeIfAbsent(toBucketDate(withdrawalInstant(withdrawal), periodBucket), ignored -> new CashFlowAccumulator())
                .addWithdrawal(withdrawal.getNetCashAmount()));

        awardedPrizes.forEach(prize -> byDate
                .computeIfAbsent(toBucketDate(prize.getAwardedAt(), periodBucket), ignored -> new CashFlowAccumulator())
                .addPrizeAward(prize.getAmount()));

        return byDate.entrySet()
                .stream()
                .map(entry -> AdminDashboardSummaryResponse.CashFlowByDay.builder()
                        .date(entry.getKey())
                        .depositAmount(entry.getValue().depositAmount)
                        .withdrawalAmount(entry.getValue().withdrawalAmount)
                        .prizeAwardAmount(entry.getValue().prizeAwardAmount)
                        .netCashFlow(entry.getValue().depositAmount
                                .subtract(entry.getValue().withdrawalAmount)
                                .subtract(entry.getValue().prizeAwardAmount))
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

    private boolean isAwardedPrize(PrizeAwards prize) {
        return prize != null
                && PrizeAwardStatus.AWARDED.getValue().equalsIgnoreCase(prize.getStatus());
    }

    private boolean isActiveRace(Races race) {
        return race != null
                && !RaceStatus.COMPLETED.equalsValue(race.getStatus())
                && !RaceStatus.CANCELLED.equalsValue(race.getStatus());
    }

    private Instant txInstant(WalletTransactions tx) {
        return tx.getUpdatedAt() == null ? tx.getCreatedAt() : tx.getUpdatedAt();
    }

    private Instant withdrawalInstant(Withdrawals withdrawal) {
        return withdrawal.getPaidAt() == null ? withdrawal.getCreatedAt() : withdrawal.getPaidAt();
    }

    private boolean isInRange(Instant instant, LocalDate from, LocalDate to) {
        if (instant == null) {
            return false;
        }
        LocalDate date = toDate(instant);
        return (from == null || !date.isBefore(from))
                && (to == null || !date.isAfter(to));
    }

    private LocalDate toBucketDate(Instant instant, PeriodBucket periodBucket) {
        LocalDate date = toDate(instant == null ? Instant.now() : instant);
        return switch (periodBucket) {
            case MONTH -> date.withDayOfMonth(1);
            case YEAR -> LocalDate.of(date.getYear(), 1, 1);
            default -> date;
        };
    }

    private LocalDate toDate(Instant instant) {
        Instant value = instant == null ? Instant.now() : instant;
        return value.atZone(VIETNAM_ZONE).toLocalDate();
    }

    private PeriodBucket normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return PeriodBucket.DAY;
        }

        return switch (period.trim().toLowerCase()) {
            case "day" -> PeriodBucket.DAY;
            case "month" -> PeriodBucket.MONTH;
            case "year" -> PeriodBucket.YEAR;
            default -> throw new BadRequestException("Invalid period. Allowed values: day, month, year");
        };
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("From date must be before or equal to to date");
        }
    }

    private BigDecimal addMoney(BigDecimal left, BigDecimal right) {
        return safeMoney(left).add(safeMoney(right));
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private enum PeriodBucket {
        DAY,
        MONTH,
        YEAR
    }

    private class CashFlowAccumulator {
        private BigDecimal depositAmount = BigDecimal.ZERO;
        private BigDecimal withdrawalAmount = BigDecimal.ZERO;
        private BigDecimal prizeAwardAmount = BigDecimal.ZERO;

        private void addDeposit(BigDecimal amount) {
            this.depositAmount = addMoney(this.depositAmount, amount);
        }

        private void addWithdrawal(BigDecimal amount) {
            this.withdrawalAmount = addMoney(this.withdrawalAmount, amount);
        }

        private void addPrizeAward(BigDecimal amount) {
            this.prizeAwardAmount = addMoney(this.prizeAwardAmount, amount);
        }
    }
}
