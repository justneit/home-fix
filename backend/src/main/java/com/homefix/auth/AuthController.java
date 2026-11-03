package com.homefix.auth;

import com.homefix.auth.dto.AuthResponse;
import com.homefix.auth.dto.LoginRequest;
import com.homefix.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
@SecurityRequirements
class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookies cookies;

    AuthController(AuthService authService, RefreshTokenCookies cookies) {
        this.authService = authService;
        this.cookies = cookies;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a customer or worker account and sign in")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return withRefreshCookie(HttpStatus.CREATED, authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Sign in with e-mail and password")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return withRefreshCookie(HttpStatus.OK, authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh-token cookie and issue a new access token (requires X-XSRF-TOKEN)")
    ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        return withRefreshCookie(HttpStatus.OK, authService.refresh(readRefreshCookie(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the current session (requires X-XSRF-TOKEN)")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(readRefreshCookie(request));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                .build();
    }

    /** Issues the XSRF-TOKEN cookie that the SPA echoes in the X-XSRF-TOKEN header. */
    @GetMapping("/csrf")
    @Operation(summary = "Obtain the CSRF cookie used by refresh and logout")
    ResponseEntity<Void> csrf(CsrfToken token) {
        token.getToken(); // loading the deferred token makes the repository write the cookie
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(HttpStatus status, AuthTokens tokens) {
        return ResponseEntity.status(status)
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookies.create(tokens.refreshToken()).toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(tokens.response());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] all = request.getCookies();
        if (all == null) {
            return null;
        }
        return Arrays.stream(all)
                .filter(c -> cookies.name().equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
