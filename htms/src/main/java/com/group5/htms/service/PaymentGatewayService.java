package com.group5.htms.service;

import com.group5.htms.dto.payment.request.PaymentGatewayCreatePaymentRequest;
import com.group5.htms.dto.payment.response.PaymentGatewayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.PaymentGatewayReturnResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentGatewayService {
    PaymentGatewayCreatePaymentResponse createMomoPayment(
            PaymentGatewayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    );

    PaymentGatewayReturnResponse handleMomoReturn(Map<String, String[]> parameterMap);

    Map<String, Object> handleMomoIpn(Map<String, Object> payload);

    PaymentGatewayCreatePaymentResponse createZalopayPayment(
            PaymentGatewayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    );

    PaymentGatewayReturnResponse handleZalopayReturn(Map<String, String[]> parameterMap);

    Map<String, Object> handleZalopayCallback(Map<String, Object> payload);
}
