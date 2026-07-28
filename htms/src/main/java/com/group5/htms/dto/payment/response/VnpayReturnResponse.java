package com.group5.htms.dto.payment.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VnpayReturnResponse {

    private boolean validSignature;
    private boolean success;

    private String txnRef;
    private String amount;
    private String responseCode;
    private String transactionStatus;
    private String transactionNo;
    private String bankCode;
    private String payDate;

    private String message;

    private String transactionRef;
    private PaymentTransactionResponse transaction;
}
