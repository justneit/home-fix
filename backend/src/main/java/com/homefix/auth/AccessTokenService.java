package com.homefix.auth;

import com.homefix.config.HomeFixSecurityProperties;
import com.homefix.user.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Issues short-lived HS256 access tokens: {@code sub} = user id, {@code role} = account role. */
@Service
class AccessTokenService {

    private final JwtEncoder encoder;
    private final Clock clock;
    private final String issuer;
    private final Duration ttl;

    AccessTokenService(JwtEncoder encoder, Clock clock, HomeFixSecurityProperties properties) {
        this.encoder = encoder;
        this.clock = clock;
        this.issuer = properties.jwt().issuer();
        this.ttl = properties.jwt().accessTokenTtl();
    }

    String issue(User user) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    long ttlSeconds() {
        return ttl.toSeconds();
    }
}
