package com.homefix.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Security settings bound from {@code homefix.security.*}. Startup fails if any is missing or invalid. */
@Validated
@ConfigurationProperties("homefix.security")
public record HomeFixSecurityProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull RefreshToken refreshToken,
        @Valid @NotNull Cors cors,
        @Valid @NotNull RateLimit rateLimit) {

    /**
     * @param secret HMAC-SHA256 signing key; at least 32 bytes (256 bits).
     */
    public record Jwt(
            @NotBlank @Size(min = 32, message = "must be at least 32 characters (256 bits)")
            String secret,

            @NotBlank String issuer,
            @NotNull Duration accessTokenTtl) {}

    public record RefreshToken(
            @NotNull Duration ttl, @NotBlank String cookieName, boolean cookieSecure) {}

    public record Cors(List<String> allowedOrigins) {
        public Cors {
            allowedOrigins = allowedOrigins == null
                    ? List.of()
                    : allowedOrigins.stream().filter(o -> !o.isBlank()).toList();
        }
    }

    public record RateLimit(
            @Min(1) int authRequestsPerMinute, @Min(1) int refreshRequestsPerMinute) {}
}
