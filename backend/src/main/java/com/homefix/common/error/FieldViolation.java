package com.homefix.common.error;

/** A single invalid input field, e.g. {@code {"field":"email","code":"Email","message":"..."}}. */
public record FieldViolation(String field, String code, String message) {}
