package com.group5.htms.controller;

import com.group5.htms.dto.payment.request.PaymentGatewayCreatePaymentRequest;
import com.group5.htms.dto.payment.request.VnpayCreatePaymentRequest;
import com.group5.htms.dto.payment.response.PaymentGatewayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.PaymentGatewayReturnResponse;
import com.group5.htms.dto.payment.response.PaymentTransactionResponse;
import com.group5.htms.dto.payment.response.VnpayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.VnpayReturnResponse;
import com.group5.htms.service.PaymentGatewayService;
import com.group5.htms.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;

    @Value("${app.frontend.payment-result-url:http://localhost:5173/payment-result}")
    private String paymentResultUrl;

    @PostMapping("/vnpay/create-payment")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<VnpayCreatePaymentResponse> createPayment(
            @Valid @RequestBody VnpayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(
                paymentService.createPaymentUrl(request, httpServletRequest)
        );
    }

    @PostMapping("/momo/create-payment")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<PaymentGatewayCreatePaymentResponse> createMomoPayment(
            @Valid @RequestBody PaymentGatewayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(paymentGatewayService.createMomoPayment(request, httpServletRequest));
    }

    @PostMapping("/zalopay/create-payment")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<PaymentGatewayCreatePaymentResponse> createZalopayPayment(
            @Valid @RequestBody PaymentGatewayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(paymentGatewayService.createZalopayPayment(request, httpServletRequest));
    }

    @GetMapping({"/topup-history", "/vnpay/topup-history"})
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<List<PaymentTransactionResponse>> getTopUpHistory() {
        return ResponseEntity.ok(paymentService.getTopUpHistory());
    }

    @GetMapping({"/transactions/{txId}", "/vnpay/transactions/{txId}"})
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<PaymentTransactionResponse> getTransactionDetail(@PathVariable Integer txId) {
        return ResponseEntity.ok(paymentService.getTransactionDetail(txId));
    }

    @GetMapping("/vnpay/handle-payment-return")
    public ResponseEntity<Void> handleReturn(HttpServletRequest request) {
        VnpayReturnResponse result = paymentService.handleReturn(request.getParameterMap());
        return redirectPaymentResult(
                result.isSuccess(),
                result.getTxnRef(),
                result.getResponseCode(),
                result.getTransactionStatus(),
                result.getMessage(),
                result.getTransaction()
        );
    }

    @GetMapping("/vnpay/handle-payment-ipn")
    public ResponseEntity<Map<String, String>> handleIpn(HttpServletRequest request) {
        return ResponseEntity.ok(paymentService.handleIpn(request.getParameterMap()));
    }

    @GetMapping("/momo/handle-payment-return")
    public ResponseEntity<Void> handleMomoReturn(HttpServletRequest request) {
        PaymentGatewayReturnResponse result = paymentGatewayService.handleMomoReturn(request.getParameterMap());
        return redirectPaymentResult(
                result.isSuccess(),
                result.getTxnRef(),
                result.getResponseCode(),
                result.getTransactionStatus(),
                result.getMessage(),
                result.getTransaction()
        );
    }

    @PostMapping("/momo/handle-payment-ipn")
    public ResponseEntity<Map<String, Object>> handleMomoIpn(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(paymentGatewayService.handleMomoIpn(payload));
    }

    @GetMapping("/zalopay/handle-payment-return")
    public ResponseEntity<Void> handleZalopayReturn(HttpServletRequest request) {
        PaymentGatewayReturnResponse result = paymentGatewayService.handleZalopayReturn(request.getParameterMap());
        return redirectPaymentResult(
                result.isSuccess(),
                result.getTxnRef(),
                result.getResponseCode(),
                result.getTransactionStatus(),
                result.getMessage(),
                result.getTransaction()
        );
    }

    @PostMapping("/zalopay/handle-payment-callback")
    public ResponseEntity<Map<String, Object>> handleZalopayCallback(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(paymentGatewayService.handleZalopayCallback(payload));
    }

    private ResponseEntity<Void> redirectPaymentResult(
            boolean success,
            String txnRef,
            String responseCode,
            String gatewayTransactionStatus,
            String message,
            PaymentTransactionResponse transaction
    ) {
        URI redirectUri = UriComponentsBuilder
                .fromUriString(paymentResultUrl)
                .queryParam("success", success)
                .queryParam("txnRef", txnRef)
                .queryParam("responseCode", responseCode)
                .queryParam("gatewayTransactionStatus", gatewayTransactionStatus)
                .queryParam("transactionStatus", transaction == null ? null : transaction.getStatus())
                .queryParam("pointsAdded", transaction == null ? null : transaction.getPointsAmount())
                .queryParam("message", message)
                .build()
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .build();
    }
}

