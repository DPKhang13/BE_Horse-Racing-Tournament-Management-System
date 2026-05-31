package com.group5.htms.service.impl;

import com.group5.htms.enums.OtpValidationStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.EmailSendException;
import com.group5.htms.service.OtpMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpMailServiceImpl implements OtpMailService {

    private static final long OTP_EXPIRATION_MS = 5 * 60 * 1000;
    private static final long RESEND_COOLDOWN_MS = 60 * 1000;
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();

    @Override
    public void generateAndSendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);

        String otp = generateOtp();
        Instant now = Instant.now();
        Instant expiredAt = now.plusMillis(OTP_EXPIRATION_MS);

        OtpRecord record = new OtpRecord(
                passwordEncoder.encode(otp),
                expiredAt,
                0,
                now
        );

        otpStore.put(normalizedEmail, record);

        sendOtpEmail(normalizedEmail, otp);
    }

    @Override
    public OtpValidationStatus validateOtp(String email, String inputOtp) {
        String normalizedEmail = normalizeEmail(email);

        if (inputOtp == null || inputOtp.isBlank()) {
            return OtpValidationStatus.INVALID;
        }

        OtpRecord record = otpStore.get(normalizedEmail);

        if (record == null) {
            return OtpValidationStatus.NOT_FOUND;
        }

        if (Instant.now().isAfter(record.getExpiredAt())) {
            otpStore.remove(normalizedEmail);
            return OtpValidationStatus.EXPIRED;
        }

        if (record.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            otpStore.remove(normalizedEmail);
            return OtpValidationStatus.MAX_ATTEMPTS_EXCEEDED;
        }

        boolean matched = passwordEncoder.matches(inputOtp, record.getOtpHash());

        if (!matched) {
            record.setFailedAttempts(record.getFailedAttempts() + 1);

            if (record.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                otpStore.remove(normalizedEmail);
                return OtpValidationStatus.MAX_ATTEMPTS_EXCEEDED;
            }

            otpStore.put(normalizedEmail, record);
            return OtpValidationStatus.INVALID;
        }

        return OtpValidationStatus.VALID;
    }

    @Override
    public void clearOtp(String email) {
        otpStore.remove(normalizeEmail(email));
    }

    @Override
    public void resendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);

        OtpRecord existingRecord = otpStore.get(normalizedEmail);

        if (existingRecord != null) {
            long millisSinceLastSent =
                    Instant.now().toEpochMilli() - existingRecord.getLastSentAt().toEpochMilli();

            if (millisSinceLastSent < RESEND_COOLDOWN_MS) {
                throw new BadRequestException("Please wait before requesting another OTP");
            }
        }

        generateAndSendOtp(normalizedEmail);
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(email);
            message.setSubject("HTMS Email Verification OTP");
            message.setText("""
                    Your HTMS verification code is: %s
                    
                    This code is valid for 5 minutes.
                    If you did not request this, please ignore this email.
                    """.formatted(otp));

            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailSendException("Failed to send OTP email", ex);
        }
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        return email.trim().toLowerCase();
    }

    private static class OtpRecord {

        private final String otpHash;

        private final Instant expiredAt;

        private int failedAttempts;

        private final Instant lastSentAt;

        public OtpRecord(
                String otpHash,
                Instant expiredAt,
                int failedAttempts,
                Instant lastSentAt
        ) {
            this.otpHash = otpHash;
            this.expiredAt = expiredAt;
            this.failedAttempts = failedAttempts;
            this.lastSentAt = lastSentAt;
        }

        public String getOtpHash() {
            return otpHash;
        }

        public Instant getExpiredAt() {
            return expiredAt;
        }

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public void setFailedAttempts(int failedAttempts) {
            this.failedAttempts = failedAttempts;
        }

        public Instant getLastSentAt() {
            return lastSentAt;
        }
    }
}