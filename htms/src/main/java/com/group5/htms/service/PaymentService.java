package com.group5.htms.service;

import com.group5.htms.dto.payment.request.VnpayCreatePaymentRequest;
import com.group5.htms.dto.payment.response.VnpayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.VnpayReturnResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {

    VnpayCreatePaymentResponse createPaymentUrl(
            VnpayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    );

    VnpayReturnResponse handleReturn(Map<String, String[]> parameterMap);

    Map<String, String> handleIpn(Map<String, String[]> parameterMap);
}