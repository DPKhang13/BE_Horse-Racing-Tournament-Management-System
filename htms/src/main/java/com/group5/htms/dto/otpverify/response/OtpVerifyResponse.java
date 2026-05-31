package com.group5.htms.dto.otpverify.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpVerifyResponse {

    private String message;
    private String email;
    private boolean verified;
}