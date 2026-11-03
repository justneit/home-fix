package com.homefix.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FixedWindowRateLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(2, Duration.ofMinutes(1), clock);

    @Test
    void rejectsRequestsBeyondTheLimitWithinAWindow() {
        assertThat(limiter.tryAcquire("ip")).isTrue();
        assertThat(limiter.tryAcquire("ip")).isTrue();
        assertThat(limiter.tryAcquire("ip")).isFalse();
    }

    @Test
    void keysAreIndependent() {
        limiter.tryAcquire("a");
        limiter.tryAcquire("a");
        assertThat(limiter.tryAcquire("a")).isFalse();
        assertThat(limiter.tryAcquire("b")).isTrue();
    }

    @Test
    void windowResetsAfterItElapses() {
        limiter.tryAcquire("ip");
        limiter.tryAcquire("ip");
        clock.advance(Duration.ofSeconds(61));
        assertThat(limiter.tryAcquire("ip")).isTrue();
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
