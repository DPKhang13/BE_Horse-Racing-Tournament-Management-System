package com.group5.htms.config;


import com.group5.htms.entity.Users;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsConfig implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!UserStatus.ACTIVE.getValue().equalsIgnoreCase(user.getStatus())) {
            throw new DisabledException("User account is not active");
        }

        if (user.getRoleType() == null || user.getRoleType().isBlank()) {
            throw new DisabledException("User has no role");
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRoleType().toUpperCase());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authority)
                .disabled(false)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}

