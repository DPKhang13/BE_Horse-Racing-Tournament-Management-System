package com.group5.htms.repository;

import com.group5.htms.entity.Withdrawals;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalsRepository extends JpaRepository<Withdrawals, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Withdrawals> findFirstById(Integer withdrawalId);

    List<Withdrawals> findAllByOrderByCreatedAtDesc();

    List<Withdrawals> findByUsersIdOrderByCreatedAtDesc(Integer userId);

    List<Withdrawals> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    Optional<Withdrawals> findByIdAndUsersId(Integer withdrawalId, Integer userId);
}