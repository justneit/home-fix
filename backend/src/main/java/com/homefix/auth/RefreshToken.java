package com.homefix.auth;

import com.homefix.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, length = 36, updatable = false)
    private String familyId;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id")
    private RefreshToken replacedBy;

    protected RefreshToken() {}

    RefreshToken(User user, String tokenHash, String familyId, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
    }

    boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    boolean isRevoked() {
        return revokedAt != null;
    }

    void rotateTo(RefreshToken successor, Instant now) {
        this.revokedAt = now;
        this.replacedBy = successor;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFamilyId() {
        return familyId;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}
