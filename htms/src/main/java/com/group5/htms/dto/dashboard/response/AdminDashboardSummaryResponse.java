package com.group5.htms.dto.dashboard.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class AdminDashboardSummaryResponse {
    private long totalUsers;
    private long totalRaces;
    private long activeRaces;
    private long totalBets;
    private BigDecimal totalSuccessfulDepositAmount;
    private BigDecimal totalCompletedWithdrawalAmount;
    private long pendingWithdrawalCount;
    private BigDecimal netCashFlow;
    private List<CashFlowByDay> cashFlowByDay;
    private List<RecentTransaction> recentTransactions;

    @Builder
    @Getter
    public static class CashFlowByDay {
        private LocalDate date;
        private BigDecimal depositAmount;
        private BigDecimal withdrawalAmount;
        private BigDecimal netCashFlow;
    }

    @Builder
    @Getter
    public static class RecentTransaction {
        private Integer txId;
        private Integer userId;
        private String username;
        private String userFullName;
        private String txType;
        private BigDecimal cashAmount;
        private BigDecimal pointsAmount;
        private String status;
        private String refType;
        private Integer refId;
        private Instant createdAt;
        private Instant updatedAt;
    }
}