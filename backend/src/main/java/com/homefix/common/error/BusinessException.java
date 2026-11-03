package com.homefix.common.error;

import java.util.List;

/** Expected business-rule failure. The message is safe to return to clients. */
public class BusinessException extends RuntimeException {

    private final ErrorCode code;
    private final List<FieldViolation> violations;

    public BusinessException(ErrorCode code, String message) {
        this(code, message, List.of());
    }

    public BusinessException(ErrorCode code, String message, List<FieldViolation> violations) {
        super(message);
        this.code = code;
        this.violations = List.copyOf(violations);
    }

    public static BusinessException notFound(String resource) {
        return new BusinessException(ErrorCode.NOT_FOUND, resource + " not found");
    }

    public ErrorCode code() {
        return code;
    }

    public List<FieldViolation> violations() {
        return violations;
    }
}
