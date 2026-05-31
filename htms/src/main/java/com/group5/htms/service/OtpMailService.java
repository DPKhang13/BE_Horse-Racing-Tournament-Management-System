package com.group5.htms.service;
import com.group5.htms.enums.OtpValidationStatus;

public interface OtpMailService {
    void generateAndSendOtp(String email);

    OtpValidationStatus validateOtp(String email, String inputOtp);

    void clearOtp(String email);

    void resendOtp(String email);
}