package com.homefix.config;

import com.homefix.common.security.AuthRateLimitFilter;
import com.homefix.common.security.FixedWindowRateLimiter;
import com.homefix.common.security.ProblemDetailSecurityHandlers;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
class SecurityConfig {

    static final String ROLE_CLAIM = "role";

    @Bean
    SecurityFilterChain apiSecurity(
            HttpSecurity http,
            ProblemDetailSecurityHandlers problemHandlers,
            HomeFixSecurityProperties properties,
            Clock clock) {
        PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();

        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource(properties)))
                // Bearer-token requests are not CSRF-prone. Only the endpoints that authenticate with the
                // refresh-token cookie need a token (double-submit XSRF-TOKEN cookie / X-XSRF-TOKEN header).
                .csrf(csrf -> csrf.spa()
                        .requireCsrfProtectionMatcher(new OrRequestMatcher(
                                paths.matcher(HttpMethod.POST, "/api/v1/auth/refresh"),
                                paths.matcher(HttpMethod.POST, "/api/v1/auth/logout"))))
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf", "/api/v1/services", "/api/v1/services/**")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(problemHandlers)
                        .accessDeniedHandler(problemHandlers))
                .exceptionHandling(
                        e -> e.authenticationEntryPoint(problemHandlers).accessDeniedHandler(problemHandlers))
                .addFilterBefore(
                        new AuthRateLimitFilter(authRateLimiters(properties.rateLimit(), clock), problemHandlers),
                        BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Delegating encoder: BCrypt today, stored hashes carry an {id} prefix so the algorithm can evolve.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Credential endpoints get a strict limit; refresh runs on every page load so it gets a looser one. */
    private static Map<String, FixedWindowRateLimiter> authRateLimiters(
            HomeFixSecurityProperties.RateLimit limits, Clock clock) {
        Duration minute = Duration.ofMinutes(1);
        return Map.of(
                "/api/v1/auth/login", new FixedWindowRateLimiter(limits.authRequestsPerMinute(), minute, clock),
                "/api/v1/auth/register", new FixedWindowRateLimiter(limits.authRequestsPerMinute(), minute, clock),
                "/api/v1/auth/refresh", new FixedWindowRateLimiter(limits.refreshRequestsPerMinute(), minute, clock));
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(ROLE_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    private static CorsConfigurationSource corsConfigurationSource(HomeFixSecurityProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
