package com.homefix.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.homefix.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
@TestPropertySource(properties = "homefix.security.rate-limit.auth-requests-per-minute=3")
class AuthRateLimitIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void loginIsThrottledPerClientAddress() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(login("10.0.0.1")).andExpect(status().isUnauthorized());
        }
        mvc.perform(login("10.0.0.1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));

        mvc.perform(login("10.0.0.2")).andExpect(status().isUnauthorized());
    }

    private static MockHttpServletRequestBuilder login(String clientIp) {
        return post("/api/v1/auth/login")
                .with(request -> {
                    request.setRemoteAddr(clientIp);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"Wrong12345\"}");
    }
}
