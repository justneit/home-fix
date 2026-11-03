package com.homefix.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Routes security-filter failures (401/403) to {@code GlobalExceptionHandler} so they are
 * rendered as the same problem-detail JSON as every other API error.
 */
@Component
public class ProblemDetailSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    public ProblemDetailSecurityHandlers(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        resolver.resolveException(request, response, null, authException);
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        resolver.resolveException(request, response, null, accessDeniedException);
    }

    /** Lets other filters (e.g. rate limiting) emit a problem detail for any exception. */
    public void resolve(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        resolver.resolveException(request, response, null, exception);
    }
}
