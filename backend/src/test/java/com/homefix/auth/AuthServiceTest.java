package com.homefix.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.homefix.auth.dto.LoginRequest;
import com.homefix.auth.dto.RegisterRequest;
import com.homefix.common.error.BusinessException;
import com.homefix.common.error.ErrorCode;
import com.homefix.user.AccountStatus;
import com.homefix.user.Role;
import com.homefix.user.User;
import com.homefix.user.UserMapper;
import com.homefix.user.UserRepository;
import com.homefix.worker.WorkerProfileRepository;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
    private AuthService service;

    @BeforeEach
    void setUp() {
        when(encoder.encode("dummy-password-for-timing")).thenReturn("DUMMY");
        service = new AuthService(
                users,
                mock(WorkerProfileRepository.class),
                encoder,
                mock(AccessTokenService.class),
                refreshTokens,
                mock(UserMapper.class),
                Clock.systemUTC());
    }

    @Test
    void unknownEmailStillPerformsAPasswordCheck() {
        when(users.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("Nobody@Example.com", "whatever1")))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(encoder).matches("whatever1", "DUMMY");
    }

    @Test
    void inactiveAccountIsRejectedOnlyAfterPasswordVerification() {
        User user = new User("u@example.com", "HASH", "U", null, Role.CUSTOMER);
        user.setStatus(AccountStatus.SUSPENDED);
        when(users.findByEmail("u@example.com")).thenReturn(Optional.of(user));

        when(encoder.matches("wrong", "HASH")).thenReturn(false);
        assertThatThrownBy(() -> service.login(new LoginRequest("u@example.com", "wrong")))
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        when(encoder.matches("right", "HASH")).thenReturn(true);
        assertThatThrownBy(() -> service.login(new LoginRequest("u@example.com", "right")))
                .extracting("code")
                .isEqualTo(ErrorCode.ACCOUNT_INACTIVE);
        verify(refreshTokens, never()).issue(any());
    }

    @Test
    void passwordsLongerThan72BytesAreRejectedBeforeHashing() {
        String longPassword = "ă".repeat(37) + "1"; // 75 bytes in UTF-8, under 72 characters
        RegisterRequest request =
                new RegisterRequest("x@example.com", longPassword, "X", null, RegisterRequest.AccountType.CUSTOMER);

        assertThatThrownBy(() -> service.register(request)).extracting("code").isEqualTo(ErrorCode.VALIDATION_FAILED);
        verify(encoder, never()).encode(eq(longPassword));
        verify(users, never()).existsByEmail(anyString());
    }
}
