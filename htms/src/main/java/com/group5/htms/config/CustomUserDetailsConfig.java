package com.group5.htms.config;


import com.group5.htms.entity.Roles;
import com.group5.htms.entity.Users;
import com.group5.htms.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsConfig implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsernameOrEmailWithRoles(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new DisabledException("User account is not active");
        }

        List<GrantedAuthority> authorities = user.getRoles()
                .stream()
                .filter(role -> "active".equalsIgnoreCase(role.getStatus()))
                .map(Roles::getRoleType)
                .map(roleType -> "ROLE_" + roleType.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        if (authorities.isEmpty()) {
            throw new DisabledException("User has no active role");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(false)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}