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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletTransactions> findFirstById(Integer txId);

    List<WalletTransactions> findByUsersIdOrderByCreatedAtDesc(Integer userId);

    Optional<WalletTransactions> findByIdAndUsersId(Integer txId, Integer userId);
}
