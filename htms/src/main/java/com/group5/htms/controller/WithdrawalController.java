package com.group5.htms.controller;

import com.group5.htms.dto.withdrawal.request.WithdrawalCreateRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalMarkPaidRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalRejectRequest;
import com.group5.htms.dto.withdrawal.response.WithdrawalResponse;
import com.group5.htms.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<WithdrawalResponse> createWithdrawal(
            @Valid @RequestBody WithdrawalCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(withdrawalService.createWithdrawal(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<List<WithdrawalResponse>> getMyWithdrawals() {
        return ResponseEntity.ok(withdrawalService.getMyWithdrawals());
    }

    @GetMapping("/my/{withdrawalId}")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<WithdrawalResponse> getMyWithdrawal(
            @PathVariable Integer withdrawalId
    ) {
        return ResponseEntity.ok(withdrawalService.getMyWithdrawal(withdrawalId));
    }

    @GetMapping("/admin/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WithdrawalResponse>> getAllWithdrawals(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(withdrawalService.getAllWithdrawals(status));
    }

    @PatchMapping("/admin/{withdrawalId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WithdrawalResponse> approveWithdrawal(
            @PathVariable Integer withdrawalId
    ) {
        return ResponseEntity.ok(withdrawalService.approveWithdrawal(withdrawalId));
    }

    @PatchMapping("/admin/{withdrawalId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WithdrawalResponse> rejectWithdrawal(
            @PathVariable Integer withdrawalId,
            @Valid @RequestBody WithdrawalRejectRequest request
    ) {
        return ResponseEntity.ok(withdrawalService.rejectWithdrawal(withdrawalId, request));
    }

    @PatchMapping("/admin/{withdrawalId}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WithdrawalResponse> markWithdrawalPaid(
            @PathVariable Integer withdrawalId,
            @Valid @RequestBody WithdrawalMarkPaidRequest request
    ) {
        return ResponseEntity.ok(withdrawalService.markWithdrawalPaid(withdrawalId, request));
    }

    @PostMapping("/admin/{withdrawalId}/resend-invoice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WithdrawalResponse> resendInvoice(
            @PathVariable Integer withdrawalId
    ) {
        return ResponseEntity.ok(withdrawalService.resendInvoice(withdrawalId));
    }
}