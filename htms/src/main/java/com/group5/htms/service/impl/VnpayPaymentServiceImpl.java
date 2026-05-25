package com.group5.htms.service.impl;

import com.group5.htms.dto.payment.VnpayCreatePaymentRequest;
import com.group5.htms.dto.payment.response.VnpayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.VnpayReturnResponse;
import com.group5.htms.service.VnpayPaymentService;
import com.group5.htms.util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class VnpayPaymentServiceImpl implements VnpayPaymentService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    /*
     * Tạo payment URL cho VNPay.
     *
     * Step hiện tại:
     * - Chỉ tạo URL và redirect payment.
     * - Chưa update Wallet DB ở đây.
     *
     * Step sau:
     * - Trước khi tạo URL, tạo WalletTransaction status = pending.
     * - txnRef nên map với tx_id hoặc mã transaction nội bộ.
     */
    @Override
    public VnpayCreatePaymentResponse createPaymentUrl(
            VnpayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        String txnRef = generateTxnRef();
        String clientIp = VnpayUtil.getClientIp(httpServletRequest);

        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        LocalDateTime expire = now.plusMinutes(15);

        BigDecimal amount = request.getAmount();

        /*
         * VNPay yêu cầu amount nhân 100.
         * Ví dụ 10,000 VND gửi sang là 1,000,000.
         */
        String vnpAmount = amount
                .multiply(BigDecimal.valueOf(100))
                .toBigInteger()
                .toString();

        Map<String, String> params = new TreeMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", vnpAmount);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Topup wallet for user " + username + " txn " + txnRef);
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_Locale", normalizeLocale(request.getLocale()));
        params.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", expire.format(VNPAY_DATE_FORMAT));

        if (request.getBankCode() != null && !request.getBankCode().isBlank()) {
            params.put("vnp_BankCode", request.getBankCode().trim());
        }

        String hashData = VnpayUtil.buildHashData(params);
        String secureHash = VnpayUtil.hmacSHA512(hashSecret, hashData);

        String paymentUrl = payUrl
                + "?"
                + VnpayUtil.buildQueryString(params)
                + "&vnp_SecureHash="
                + secureHash;

        return VnpayCreatePaymentResponse.builder()
                .txnRef(txnRef)
                .paymentUrl(paymentUrl)
                .build();
    }

    /*
    Return URL:
    Đây là URL browser của user bị redirect về sau thanh toán.
    Chỉ dùng để hiển thị kết quả.
    Không nên cộng tiền ví ở đây.
     */
    @Override
    public VnpayReturnResponse handleReturn(Map<String, String[]> parameterMap) {
        boolean validSignature = VnpayUtil.verifySignature(parameterMap, hashSecret);

        String responseCode = VnpayUtil.getFirstValue(parameterMap, "vnp_ResponseCode");
        String transactionStatus = VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionStatus");

        boolean success = validSignature
                && "00".equals(responseCode)
                && "00".equals(transactionStatus);

        return VnpayReturnResponse.builder()
                .validSignature(validSignature)
                .success(success)
                .txnRef(VnpayUtil.getFirstValue(parameterMap, "vnp_TxnRef"))
                .amount(VnpayUtil.getFirstValue(parameterMap, "vnp_Amount"))
                .responseCode(responseCode)
                .transactionStatus(transactionStatus)
                .transactionNo(VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionNo"))
                .bankCode(VnpayUtil.getFirstValue(parameterMap, "vnp_BankCode"))
                .payDate(VnpayUtil.getFirstValue(parameterMap, "vnp_PayDate"))
                .message(success ? "Payment success" : "Payment failed or invalid signature")
                .build();
    }

    /*
     * IPN URL:
     * - Đây là callback server-to-server từ VNPay.
     * - Sau này update WalletTransaction + Wallet ở đây.
     *
     * VNPay yêu cầu response JSON có RspCode + Message.
     */
    @Override
    public Map<String, String> handleIpn(Map<String, String[]> parameterMap) {
        boolean validSignature = VnpayUtil.verifySignature(parameterMap, hashSecret);

        if (!validSignature) {
            return Map.of(
                    "RspCode", "97",
                    "Message", "Invalid signature"
            );
        }

        String txnRef = VnpayUtil.getFirstValue(parameterMap, "vnp_TxnRef");
        String responseCode = VnpayUtil.getFirstValue(parameterMap, "vnp_ResponseCode");
        String transactionStatus = VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionStatus");

        /*
          1. Find WalletTransaction by txnRef.
             Nếu không thấy:
               return RspCode = 01, Message = Order not found

          2. Check amount:
             vnp_Amount / 100 phải bằng cash_amount trong WalletTransaction.
             Nếu sai:
               return RspCode = 04, Message = Invalid Amount

          3. Check transaction status:
             Nếu đã completed/failed rồi:
               return RspCode = 02, Message = Order already confirmed

          4. Nếu responseCode = 00 và transactionStatus = 00:
               update WalletTransaction status = completed
               cộng point vào Wallet

          5. Nếu thất bại:
               update WalletTransaction status = failed
         */

        if (txnRef == null || txnRef.isBlank()) {
            return Map.of(
                    "RspCode", "01",
                    "Message", "Order not found"
            );
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            return Map.of(
                    "RspCode", "00",
                    "Message", "Confirm Success"
            );
        }

        return Map.of(
                "RspCode", "00",
                "Message", "Confirm Success"
        );
    }

    private String generateTxnRef() {
        return "TOPUP" + System.currentTimeMillis();
    }

    private String normalizeLocale(String locale) {
        if ("en".equalsIgnoreCase(locale)) {
            return "en";
        }

        return "vn";
    }
}