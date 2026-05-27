package com.group5.htms.util;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class VnpayUtil {

    private VnpayUtil() {
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );

            hmac512.init(secretKey);

            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();

            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot generate HMAC SHA512", ex);
        }
    }

    public static String buildQueryString(Map<String, String> params) {
        return params.entrySet()
                .stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static String buildHashData(Map<String, String> params) {
        return buildQueryString(params);
    }

    public static Map<String, String> filterVnpayParams(Map<String, String[]> parameterMap) {
        Map<String, String> result = new TreeMap<>();

        parameterMap.forEach((key, values) -> {
            if (key != null
                    && key.startsWith("vnp_")
                    && !key.equals("vnp_SecureHash")
                    && !key.equals("vnp_SecureHashType")
                    && values != null
                    && values.length > 0
                    && values[0] != null
                    && !values[0].isBlank()) {
                result.put(key, values[0]);
            }
        });

        return result;
    }

    public static boolean verifySignature(
            Map<String, String[]> parameterMap,
            String hashSecret
    ) {
        String secureHash = getFirstValue(parameterMap, "vnp_SecureHash");

        if (secureHash == null || secureHash.isBlank()) {
            return false;
        }

        Map<String, String> filteredParams = filterVnpayParams(parameterMap);
        String hashData = buildHashData(filteredParams);
        String calculatedHash = hmacSHA512(hashSecret, hashData);

        return calculatedHash.equalsIgnoreCase(secureHash);
    }

    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");

        if (ipAddress != null && !ipAddress.isBlank()) {
            ipAddress = ipAddress.split(",")[0].trim();
        } else {
            ipAddress = request.getHeader("X-Real-IP");

            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            }
        }

        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        return ipAddress;
    }

    public static String getFirstValue(Map<String, String[]> params, String key) {
        String[] values = params.get(key);

        if (values == null || values.length == 0) {
            return null;
        }

        return values[0];
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}