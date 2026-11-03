package com.homefix.common.security;

import com.homefix.common.error.BusinessException;
import com.homefix.common.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

/** Throttles credential-handling endpoints per client IP to slow down brute-force attempts. */
public class AuthRateLimitFilter extends OncePerRequestFilter {

    /** Request path to the limiter guarding it. */
    private final Map<String, FixedWindowRateLimiter> limiters;

    private final ProblemDetailSecurityHandlers problemHandlers;

    public AuthRateLimitFilter(
            Map<String, FixedWindowRateLimiter> limiters, ProblemDetailSecurityHandlers problemHandlers) {
        this.limiters = Map.copyOf(limiters);
        this.problemHandlers = problemHandlers;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !limiters.containsKey(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Behind a trusted reverse proxy getRemoteAddr() is the client IP (server.forward-headers-strategy).
        FixedWindowRateLimiter limiter = limiters.get(request.getRequestURI());
        if (!limiter.tryAcquire(request.getRemoteAddr())) {
            response.setHeader("Retry-After", "60");
            problemHandlers.resolve(
                    request,
                    response,
                    new BusinessException(ErrorCode.RATE_LIMITED, "Too many requests; try again later"));
            return;
        }
        chain.doFilter(request, response);
    }
}
