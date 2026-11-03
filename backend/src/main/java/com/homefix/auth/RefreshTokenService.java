package com.homefix.auth;

import com.homefix.common.error.BusinessException;
import com.homefix.common.error.ErrorCode;
import com.homefix.config.HomeFixSecurityProperties;
import com.homefix.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Opaque refresh tokens with rotation and reuse detection.
 *
 * <p>Each use rotates the token. Presenting an already-rotated token means it was copied, so the
 * whole family (every token descending from the same login) is revoked.
 */
@Service
class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository tokens;
    private final Clock clock;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    RefreshTokenService(RefreshTokenRepository tokens, Clock clock, HomeFixSecurityProperties properties) {
        this.tokens = tokens;
        this.clock = clock;
        this.ttl = properties.refreshToken().ttl();
    }

    /** Starts a new token family (a new login). Returns the raw token. */
    @Transactional
    String issue(User user) {
        return create(user, UUID.randomUUID().toString()).raw();
    }

    /**
     * Validates and rotates {@code rawToken}. Revocations performed while rejecting a token are
     * committed even though an exception is thrown.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    Rotation rotate(String rawToken) {
        Instant now = clock.instant();
        RefreshToken current =
                tokens.findByTokenHashForUpdate(hash(rawToken)).orElseThrow(RefreshTokenService::invalid);

        if (current.isRevoked()) {
            log.warn(
                    "Refresh token reuse detected; revoking token family for user {}",
                    current.getUser().getId());
            tokens.revokeFamily(current.getFamilyId(), now);
            throw invalid();
        }
        if (current.isExpired(now) || !current.getUser().isActive()) {
            tokens.revokeFamily(current.getFamilyId(), now);
            throw invalid();
        }

        Created next = create(current.getUser(), current.getFamilyId());
        current.rotateTo(next.entity(), now);
        return new Rotation(current.getUser(), next.raw());
    }

    /** Revokes the family of {@code rawToken}, if it exists. Unknown tokens are ignored. */
    @Transactional
    void revoke(String rawToken) {
        tokens.findByTokenHashForUpdate(hash(rawToken))
                .ifPresent(t -> tokens.revokeFamily(t.getFamilyId(), clock.instant()));
    }

    @Scheduled(cron = "${homefix.security.refresh-token.cleanup-cron:0 30 3 * * *}", zone = "UTC")
    @Transactional
    void deleteExpired() {
        int deleted = tokens.deleteExpiredBefore(clock.instant());
        log.info("Deleted {} expired refresh tokens", deleted);
    }

    private Created create(User user, String familyId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken entity = tokens.save(
                new RefreshToken(user, hash(raw), familyId, clock.instant().plus(ttl)));
        return new Created(entity, raw);
    }

    static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static BusinessException invalid() {
        return new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Session expired; please sign in again");
    }

    record Rotation(User user, String refreshToken) {}

    private record Created(RefreshToken entity, String raw) {}
}
