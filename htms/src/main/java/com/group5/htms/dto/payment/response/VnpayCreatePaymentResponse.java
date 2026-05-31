package com.group5.htms.dto.payment.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VnpayCreatePaymentResponse {

    private String txnRef;
    private String paymentUrl;
}