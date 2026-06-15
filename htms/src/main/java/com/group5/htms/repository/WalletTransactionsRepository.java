package com.group5.htms.repository;

import com.group5.htms.entity.WalletTransactions;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionsRepository extends JpaRepository<WalletTransactions, Integer> {

    /*
     Dùng cho IPN:
     VNPay gửi vnp_TxnRef về.
     Backend lookup bằng gatewayProvider + gatewayTxnRef.
     */
    Optional<WalletTransactions> findByGatewayProviderAndGatewayTxnRef(
            String gatewayProvider,
            String gatewayTxnRef
    );

    /*
     Dùng cho IPN có lock để tránh cộng điểm 2 lần nếu VNPay retry callback.
     Khi transaction đang được xử lý, row này bị lock trong DB transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletTransactions> findFirstByGatewayProviderAndGatewayTxnRef(
            String gatewayProvider,
            String gatewayTxnRef
    );

    /*
    Dùng cho user xem lịch sử giao dịch của chính mình.
     */
    List<WalletTransactions> findByUsersIdOrderByCreatedAtDesc(Integer userId);

    /*
     Dùng cho owner check:
     Chỉ cho user xem transaction nếu transaction thuộc role spectator của user đó.
     */
    Optional<WalletTransactions> findByGatewayProviderAndGatewayTxnRefAndUsersId(
            String gatewayProvider,
            String gatewayTxnRef,
            Integer userId
    );
}
