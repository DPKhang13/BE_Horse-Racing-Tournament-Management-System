package com.group5.htms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"Roles\"")
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private Users users;

    @Size(max = 30)
    @NotNull
    @Column(name = "role_type", nullable = false, length = 30)
    private String roleType;

    @Size(max = 50)
    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'active'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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