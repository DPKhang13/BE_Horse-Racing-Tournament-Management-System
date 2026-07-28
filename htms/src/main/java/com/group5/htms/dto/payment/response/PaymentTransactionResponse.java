package com.group5.htms.dto.payment.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentTransactionResponse {

    private Integer txId;

    private Integer walletId;

    private Integer userId;

    private String txType;

    private BigDecimal cashAmount;

    private BigDecimal pointsAmount;

    private BigDecimal exchangeRate;

    private BigDecimal pointsBefore;

    private BigDecimal pointsAfter;

    private String status;

    private String refType;

    private Integer refId;

    private Integer createdBy;

    private Instant createdAt;

    private Instant updatedAt;
}
