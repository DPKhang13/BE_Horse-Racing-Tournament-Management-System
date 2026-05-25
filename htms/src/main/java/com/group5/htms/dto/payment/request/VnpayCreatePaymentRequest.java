package com.group5.htms.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VnpayCreatePaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Minimum top-up amount is 10,000 VND")
    private BigDecimal amount;

    /*
     * Optional.
     Null/blank thì VNPay sẽ cho user tự chọn phương thức thanh toán.
     Một số bankCode hay dùng:
     VNPAYQR = thanh toán QR
     VNBANK  = thẻ ATM/tài khoản nội địa
     INTCARD = thẻ quốc tế
     */
    private String bankCode;

    /*
     Optional.
     vn hoặc en.
     */
    private String locale;
}