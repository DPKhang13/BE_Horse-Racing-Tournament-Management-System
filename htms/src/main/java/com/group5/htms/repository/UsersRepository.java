package com.group5.htms.repository;

import com.group5.htms.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Users> findByUsernameOrEmail(String username, String email);
}
