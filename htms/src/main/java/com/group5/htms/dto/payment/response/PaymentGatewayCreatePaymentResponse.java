package com.group5.htms.dto.payment.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayCreatePaymentResponse {
    private String gateway;
    private String txnRef;
    private String transactionRef;
    private String paymentUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String gatewayOrderId;
    private String gatewayMessage;
    private Integer gatewayResultCode;
    private PaymentTransactionResponse transaction;
}
