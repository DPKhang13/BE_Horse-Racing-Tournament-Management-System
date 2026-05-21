package com.group5.htms.repository;

import com.group5.htms.entity.Wallets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletsRepository extends JpaRepository<Wallets, Integer> {
}