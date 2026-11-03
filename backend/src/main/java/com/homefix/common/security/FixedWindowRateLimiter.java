package com.homefix.common.security;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory fixed-window rate limiter keyed by an arbitrary string (e.g. client IP + endpoint).
 *
 * <p>Suitable for a single backend instance. Replace with a shared store (e.g. Redis) before
 * running more than one instance.
 */
public class FixedWindowRateLimiter {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private final int limit;
    private final long windowMillis;
    private final Clock clock;
    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int limit, Duration window, Clock clock) {
        this.limit = limit;
        this.windowMillis = window.toMillis();
        this.clock = clock;
    }

    /** Records one attempt for {@code key} and returns whether it is within the limit. */
    public boolean tryAcquire(String key) {
        long now = clock.millis();
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.values().removeIf(w -> now - w.start >= windowMillis);
        }
        Window window = windows.compute(key, (k, current) -> {
            if (current == null || now - current.start >= windowMillis) {
                return new Window(now, 1);
            }
            return new Window(current.start, current.count + 1);
        });
        return window.count <= limit;
    }

    private record Window(long start, int count) {}
}
