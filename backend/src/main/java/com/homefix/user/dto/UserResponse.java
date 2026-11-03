package com.homefix.user.dto;

import com.homefix.user.AccountStatus;
import com.homefix.user.Role;
import java.time.Instant;

public record UserResponse(
        Long id, String email, String fullName, String phone, Role role, AccountStatus status, Instant createdAt) {}
