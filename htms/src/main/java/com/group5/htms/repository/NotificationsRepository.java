package com.group5.htms.repository;

import com.group5.htms.entity.Notifications;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {
    List<Notifications> findByUsers_Id(Integer userId);

    List<Notifications> findByUsers_IdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    long countByUsers_IdAndIsReadFalse(Integer userId);
}
