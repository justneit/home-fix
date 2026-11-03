package com.homefix.user;

public enum AccountStatus {
    /** Can sign in and use the platform. */
    ACTIVE,
    /** Temporarily blocked by an administrator. */
    SUSPENDED,
    /** Closed account; kept for history and referential integrity. */
    DISABLED
}
