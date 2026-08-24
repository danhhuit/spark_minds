package com.sparkminds.library.auth.entity;

import com.sparkminds.library.member.entity.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "email_change_verifications")
@Getter
@Setter
@NoArgsConstructor
public class EmailChangeVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(
        name = "new_email",
        nullable = false,
        length = 255
    )
    private String newEmail;

    @Column(
        name = "code_hash",
        nullable = false,
        length = 255
    )
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;
}