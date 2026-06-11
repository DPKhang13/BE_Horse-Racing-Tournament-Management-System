package com.group5.htms.repository;

import com.group5.htms.entity.Wallets;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletsRepository extends JpaRepository<Wallets, Integer> {

    Optional<Wallets> findByUsersId(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallets w
            WHERE w.id = :walletId
            """)
    Optional<Wallets> findByIdForUpdate(@Param("walletId") Integer walletId);
}
