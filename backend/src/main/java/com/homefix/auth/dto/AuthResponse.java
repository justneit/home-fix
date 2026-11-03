package com.homefix.auth.dto;

import com.homefix.user.dto.UserResponse;

/** Returned by register/login/refresh. The refresh token travels only in an HttpOnly cookie. */
public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {

    public static AuthResponse bearer(String accessToken, long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, user);
    }
}
