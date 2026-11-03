package com.homefix.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) String email,

        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).+$", message = "must contain at least one letter and one digit")
        String password,

        @NotBlank @Size(max = 100) String fullName,

        @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "must be a valid Vietnamese phone number")
        String phone,

        @NotNull AccountType accountType) {

    /** Roles that can be self-registered. Administrators are provisioned separately. */
    public enum AccountType {
        CUSTOMER,
        WORKER
    }
}
