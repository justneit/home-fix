package com.homefix.auth;

import com.homefix.auth.dto.AuthResponse;
import com.homefix.auth.dto.LoginRequest;
import com.homefix.auth.dto.RegisterRequest;
import com.homefix.common.error.BusinessException;
import com.homefix.common.error.ErrorCode;
import com.homefix.common.error.FieldViolation;
import com.homefix.user.Role;
import com.homefix.user.User;
import com.homefix.user.UserMapper;
import com.homefix.user.UserRepository;
import com.homefix.worker.WorkerProfile;
import com.homefix.worker.WorkerProfileRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** BCrypt accepts at most 72 bytes; longer input would be rejected by the encoder. */
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository users;
    private final WorkerProfileRepository workerProfiles;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokens;
    private final RefreshTokenService refreshTokens;
    private final UserMapper userMapper;
    private final Clock clock;
    /** Compared against when the e-mail is unknown, so both failure paths cost one hash check. */
    private final String dummyPasswordHash;

    AuthService(
            UserRepository users,
            WorkerProfileRepository workerProfiles,
            PasswordEncoder passwordEncoder,
            AccessTokenService accessTokens,
            RefreshTokenService refreshTokens,
            UserMapper userMapper,
            Clock clock) {
        this.users = users;
        this.workerProfiles = workerProfiles;
        this.passwordEncoder = passwordEncoder;
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
        this.userMapper = userMapper;
        this.clock = clock;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional
    AuthTokens register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "Request validation failed",
                    List.of(new FieldViolation("password", "Size", "is too long")));
        }
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED, "This e-mail is already registered");
        }

        Role role = Role.valueOf(request.accountType().name());
        User user = users.save(new User(
                email,
                passwordEncoder.encode(request.password()),
                request.fullName().strip(),
                blankToNull(request.phone()),
                role));
        if (role == Role.WORKER) {
            workerProfiles.save(new WorkerProfile(user));
        }
        return issueTokens(user);
    }

    @Transactional
    AuthTokens login(LoginRequest request) {
        Optional<User> found = users.findByEmail(normalizeEmail(request.email()));
        String hash = found.map(User::getPasswordHash).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);
        if (found.isEmpty() || !passwordMatches) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid e-mail or password");
        }

        User user = found.get();
        // Status is revealed only after the password has been verified.
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE, "This account is not active");
        }
        user.recordLogin(clock.instant());
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    AuthTokens refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Session expired; please sign in again");
        }
        RefreshTokenService.Rotation rotation = refreshTokens.rotate(rawRefreshToken);
        User user = rotation.user();
        AuthResponse response =
                AuthResponse.bearer(accessTokens.issue(user), accessTokens.ttlSeconds(), userMapper.toResponse(user));
        return new AuthTokens(response, rotation.refreshToken());
    }

    @Transactional
    void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokens.revoke(rawRefreshToken);
        }
    }

    private AuthTokens issueTokens(User user) {
        AuthResponse response =
                AuthResponse.bearer(accessTokens.issue(user), accessTokens.ttlSeconds(), userMapper.toResponse(user));
        return new AuthTokens(response, refreshTokens.issue(user));
    }

    private static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
