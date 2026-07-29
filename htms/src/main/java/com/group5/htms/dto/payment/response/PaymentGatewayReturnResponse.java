package com.group5.htms.dto.payment.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayReturnResponse {
    private String gateway;
    private boolean validSignature;
    private boolean success;
    private String txnRef;
    private String transactionRef;
    private String amount;
    private String responseCode;
    private String transactionStatus;
    private String transactionNo;
    private String message;
    private PaymentTransactionResponse transaction;
}
