package com.group5.htms.repository;

import com.group5.htms.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT DISTINCT u
            FROM Users u
            LEFT JOIN FETCH u.roles
            WHERE u.username = :identifier OR u.email = :identifier
            """)
    Optional<Users> findByUsernameOrEmailWithRoles(@Param("identifier") String identifier);

    @Query("""
            SELECT DISTINCT u
            FROM Users u
            LEFT JOIN FETCH u.roles
            WHERE u.username = :username
            """)
    Optional<Users> findByUsernameWithRoles(@Param("username") String username);
}