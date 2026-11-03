package com.homefix.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.homefix.support.IntegrationTest;
import com.homefix.user.AccountStatus;
import com.homefix.user.UserRepository;
import com.homefix.worker.VerificationStatus;
import com.homefix.worker.WorkerProfileRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class AuthFlowIntegrationTest {

    private static final String PASSWORD = "Secret123";

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository users;

    @Autowired
    WorkerProfileRepository workerProfiles;

    @Test
    void registerCustomerReturnsAccessTokenAndSecureRefreshCookie() throws Exception {
        String email = uniqueEmail();

        MockHttpServletResponse response = register(email, "CUSTOMER")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn()
                .getResponse();

        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie)
                .startsWith("hf_refresh=")
                .contains("HttpOnly", "Secure", "SameSite=Strict", "Path=/api/v1/auth");
    }

    @Test
    void registerWorkerCreatesPendingWorkerProfile() throws Exception {
        String email = uniqueEmail();
        register(email, "WORKER").andExpect(status().isCreated());

        Long id = users.findByEmail(email).orElseThrow().getId();
        assertThat(workerProfiles.findById(id))
                .hasValueSatisfying(p -> assertThat(p.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING));
    }

    @Test
    void duplicateEmailIsRejectedCaseInsensitively() throws Exception {
        String email = uniqueEmail();
        register(email, "CUSTOMER").andExpect(status().isCreated());

        register(email.toUpperCase(), "CUSTOMER")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void cannotSelfRegisterAsAdmin() throws Exception {
        register(uniqueEmail(), "ADMIN")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"short","fullName":"","accountType":"CUSTOMER"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("password")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("fullName")));
    }

    @Test
    void loginFailuresAreGenericForUnknownEmailAndWrongPassword() throws Exception {
        String email = uniqueEmail();
        register(email, "CUSTOMER");

        login(email, "WrongPass1")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("Invalid e-mail or password"));
        login(uniqueEmail(), PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("Invalid e-mail or password"));
    }

    @Test
    void suspendedAccountCannotLogIn() throws Exception {
        String email = uniqueEmail();
        register(email, "CUSTOMER");
        var user = users.findByEmail(email).orElseThrow();
        user.setStatus(AccountStatus.SUSPENDED);
        users.save(user);

        login(email, PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_INACTIVE"));
    }

    @Test
    void meRequiresAValidBearerToken() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        String email = uniqueEmail();
        String token = accessToken(register(email, "CUSTOMER"));
        mvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void adminEndpointsRejectNonAdminRoles() throws Exception {
        String token = accessToken(register(uniqueEmail(), "CUSTOMER"));
        mvc.perform(get("/api/v1/admin/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void refreshRequiresCsrfToken() throws Exception {
        Cookie refresh = refreshCookie(register(uniqueEmail(), "CUSTOMER"));

        mvc.perform(post("/api/v1/auth/refresh").cookie(refresh))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void refreshRotatesTokenAndReuseRevokesTheWholeFamily() throws Exception {
        Cookie original = refreshCookie(register(uniqueEmail(), "CUSTOMER"));
        Cookie xsrf = xsrfCookie();

        ResultActions first = refresh(original, xsrf)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        Cookie rotated = refreshCookie(first);
        assertThat(rotated.getValue()).isNotEqualTo(original.getValue());

        // Replaying the already-rotated token is treated as theft...
        refresh(original, xsrf)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
        // ...and invalidates the legitimate successor too.
        refresh(rotated, xsrf).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshTokenAndClearsCookie() throws Exception {
        Cookie refresh = refreshCookie(register(uniqueEmail(), "CUSTOMER"));
        Cookie xsrf = xsrfCookie();

        String cleared = mvc.perform(
                        post("/api/v1/auth/logout").cookie(refresh, xsrf).header("X-XSRF-TOKEN", xsrf.getValue()))
                .andExpect(status().isNoContent())
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);
        assertThat(cleared).startsWith("hf_refresh=;").contains("Max-Age=0");

        refresh(refresh, xsrf).andExpect(status().isUnauthorized());
    }

    private ResultActions register(String email, String accountType) throws Exception {
        return mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s","fullName":"Người Dùng Thử","phone":"0912345678","accountType":"%s"}
                        """.formatted(email, PASSWORD, accountType)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    private ResultActions refresh(Cookie refresh, Cookie xsrf) throws Exception {
        return mvc.perform(post("/api/v1/auth/refresh").cookie(refresh, xsrf).header("X-XSRF-TOKEN", xsrf.getValue()));
    }

    private Cookie xsrfCookie() throws Exception {
        Cookie cookie = mvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn()
                .getResponse()
                .getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        return cookie;
    }

    private static Cookie refreshCookie(ResultActions result) {
        Cookie cookie = result.andReturn().getResponse().getCookie("hf_refresh");
        assertThat(cookie).isNotNull();
        return new Cookie(cookie.getName(), cookie.getValue());
    }

    private static String accessToken(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.accessToken");
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
