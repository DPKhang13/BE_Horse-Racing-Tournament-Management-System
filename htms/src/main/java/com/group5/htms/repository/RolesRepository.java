package com.group5.htms.repository;

import com.group5.htms.entity.Roles;
import com.group5.htms.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Integer> {

    List<Roles> findByUsers(Users users);

    Optional<Roles> findByUsersAndRoleType(Users users, String roleType);
}