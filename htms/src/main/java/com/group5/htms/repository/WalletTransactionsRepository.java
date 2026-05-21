package com.group5.htms.repository;

import com.group5.htms.entity.WalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionsRepository extends JpaRepository<WalletTransactions, Integer> {
}