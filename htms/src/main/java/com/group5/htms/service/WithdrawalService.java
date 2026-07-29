package com.group5.htms.service;

import com.group5.htms.dto.withdrawal.request.WithdrawalApproveRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalCreateRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalMarkPaidRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalRejectRequest;
import com.group5.htms.dto.withdrawal.response.WithdrawalResponse;

import java.util.List;

public interface WithdrawalService {

    WithdrawalResponse createWithdrawal(WithdrawalCreateRequest request);

    List<WithdrawalResponse> getMyWithdrawals();

    WithdrawalResponse getMyWithdrawal(Integer withdrawalId);

    List<WithdrawalResponse> getAllWithdrawals(String status);

    WithdrawalResponse approveWithdrawal(Integer withdrawalId, WithdrawalApproveRequest request);

    WithdrawalResponse rejectWithdrawal(Integer withdrawalId, WithdrawalRejectRequest request);

    WithdrawalResponse markWithdrawalPaid(Integer withdrawalId, WithdrawalMarkPaidRequest request);

    WithdrawalResponse resendInvoice(Integer withdrawalId);
}