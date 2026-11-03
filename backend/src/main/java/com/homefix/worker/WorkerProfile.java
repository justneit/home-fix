package com.homefix.worker;

import com.homefix.common.persistence.AuditableEntity;
import com.homefix.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Worker-specific data, sharing its primary key with the owning {@link User}. */
@Entity
@Table(name = "worker_profiles")
public class WorkerProfile extends AuditableEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(nullable = false)
    private boolean available;

    @Version
    private long version;

    protected WorkerProfile() {}

    public WorkerProfile(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return userId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public boolean isAvailable() {
        return available;
    }
}
