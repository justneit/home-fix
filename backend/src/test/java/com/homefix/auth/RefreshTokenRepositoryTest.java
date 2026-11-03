package com.homefix.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.homefix.support.TestcontainersConfiguration;
import com.homefix.user.Role;
import com.homefix.user.User;
import com.homefix.user.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

/** Runs the real Flyway migrations against MySQL and checks schema-level guarantees. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RefreshTokenRepositoryTest {

    @Autowired
    RefreshTokenRepository tokens;

    @Autowired
    UserRepository users;

    @Test
    void emailMustBeUnique() {
        users.saveAndFlush(user("dup@example.com"));
        assertThatThrownBy(() -> users.saveAndFlush(user("dup@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void tokenHashMustBeUnique() {
        User owner = users.save(user("hash@example.com"));
        Instant expiry = Instant.now().plus(1, ChronoUnit.DAYS);
        tokens.saveAndFlush(
                new RefreshToken(owner, "a".repeat(64), UUID.randomUUID().toString(), expiry));
        assertThatThrownBy(() -> tokens.saveAndFlush(new RefreshToken(
                        owner, "a".repeat(64), UUID.randomUUID().toString(), expiry)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void revokeFamilyOnlyTouchesThatFamily() {
        User owner = users.save(user("family@example.com"));
        Instant expiry = Instant.now().plus(1, ChronoUnit.DAYS);
        RefreshToken a = tokens.save(new RefreshToken(owner, "b".repeat(64), "family-a", expiry));
        RefreshToken b = tokens.save(new RefreshToken(owner, "c".repeat(64), "family-b", expiry));
        tokens.flush();

        assertThat(tokens.revokeFamily("family-a", Instant.now())).isEqualTo(1);

        assertThat(tokens.findById(a.getId()).orElseThrow().getRevokedAt()).isNotNull();
        assertThat(tokens.findById(b.getId()).orElseThrow().getRevokedAt()).isNull();
    }

    @Test
    void deletesExpiredRotationChains() {
        User owner = users.save(user("expired@example.com"));
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        RefreshToken older = tokens.save(new RefreshToken(owner, "d".repeat(64), "family-c", past));
        RefreshToken newer = tokens.save(new RefreshToken(owner, "e".repeat(64), "family-c", past));
        older.rotateTo(newer, past);
        tokens.flush();

        assertThat(tokens.deleteExpiredBefore(Instant.now())).isEqualTo(2);
    }

    private static User user(String email) {
        return new User(email, "{noop}x", "Test", null, Role.CUSTOMER);
    }
}
