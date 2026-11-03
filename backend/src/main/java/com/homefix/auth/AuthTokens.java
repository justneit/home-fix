package com.homefix.auth;

import com.homefix.auth.dto.AuthResponse;

/** Result of a successful authentication: the response body plus the raw refresh token for the cookie. */
record AuthTokens(AuthResponse response, String refreshToken) {}
