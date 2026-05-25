package com.group5.htms.controller;

import com.group5.htms.dto.payment.VnpayCreatePaymentRequest;
import com.group5.htms.dto.payment.response.VnpayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.VnpayReturnResponse;
import com.group5.htms.service.VnpayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/vnpay")
public class VnpayPaymentController {

    private final VnpayPaymentService vnpayPaymentService;

    /*
     Authenticated API.
     User đã login mới được tạo payment URL để nạp tiền.
     */
    @PostMapping("/create-payment")
    public ResponseEntity<VnpayCreatePaymentResponse> createPayment(
            @Valid @RequestBody VnpayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(
                vnpayPaymentService.createPaymentUrl(request, httpServletRequest)
        );
    }

    /*
     Return URL từ VNPay redirect browser về.
     Tạm thời trả JSON để dễ test.
     Sau này có thể redirect về frontend page:
     http://localhost:5173/payment-result?status=success
     */
    @GetMapping("/return")
    public ResponseEntity<VnpayReturnResponse> handleReturn(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                vnpayPaymentService.handleReturn(request.getParameterMap())
        );
    }

    /*
     IPN URL server-to-server.
     Sau này update WalletTransaction + Wallet tại đây.
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleIpn(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                vnpayPaymentService.handleIpn(request.getParameterMap())
        );
    }
}