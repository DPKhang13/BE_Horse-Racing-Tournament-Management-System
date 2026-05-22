package com.group5.htms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"Users\"")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 100)
    @NotNull
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'active'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "users",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @ToString.Exclude
    private Set<Roles> roles = new HashSet<>();

    public void addRole(Roles role) {
        roles.add(role);
        role.setUsers(this);
    }

    public void removeRole(Roles role) {
        roles.remove(role);
        role.setUsers(null);
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "active";
        }

        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}