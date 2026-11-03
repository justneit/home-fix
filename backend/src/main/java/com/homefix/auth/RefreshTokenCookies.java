package com.homefix.auth;

import com.homefix.config.HomeFixSecurityProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Builds the HttpOnly refresh-token cookie, scoped to the auth endpoints only. */
@Component
class RefreshTokenCookies {

    static final String PATH = "/api/v1/auth";

    private final String name;
    private final boolean secure;
    private final Duration ttl;

    RefreshTokenCookies(HomeFixSecurityProperties properties) {
        this.name = properties.refreshToken().cookieName();
        this.secure = properties.refreshToken().cookieSecure();
        this.ttl = properties.refreshToken().ttl();
    }

    String name() {
        return name;
    }

    ResponseCookie create(String rawToken) {
        return base(rawToken).maxAge(ttl).build();
    }

    ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(PATH);
    }
}
