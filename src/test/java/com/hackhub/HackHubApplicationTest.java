package com.hackhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import com.hackhub.domain.exception.AuthenticationException;
import com.hackhub.service.AuthenticationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class HackHubApplicationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies Spring wiring and JPA mappings.
    }

    @Test
    void registeredUserCanLoginAndResolveSession() {
        var registered = authenticationService.register(
                "New User", "new.user@example.org", "secure-pass"
        );
        var authenticated = authenticationService.login("new.user@example.org", "secure-pass");

        assertEquals(registered.id(), authenticated.user().id());
        assertEquals(registered.id(), authenticationService.currentUser(authenticated.accessToken()).id());
    }

    @Test
    void loginRejectsInvalidPassword() {
        authenticationService.register("Other User", "other.user@example.org", "secure-pass");
        assertThrows(
                AuthenticationException.class,
                () -> authenticationService.login("other.user@example.org", "wrong-pass")
        );
    }

    @Test
    void visitorsCanOnlyReadPublicHackathonInformation() throws Exception {
        mockMvc.perform(get("/api/hackathons")).andExpect(status().isOk());
        mockMvc.perform(get("/api/hackathons/1/submissions")).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedIdentityIsTakenFromTokenWhenCreatingTeam() throws Exception {
        String suffix = UUID.randomUUID().toString();
        var user = authenticationService.register("Team owner", suffix + "@example.org", "secure-pass");
        var session = authenticationService.login(suffix + "@example.org", "secure-pass");

        mockMvc.perform(post("/api/users/me/teams")
                        .header("X-Access-Token", session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Authenticated team\",\"maxSize\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberIds[0]").value(user.id()));
    }

    @Test
    void invalidPayloadIsRejectedBeforeDomainExecution() throws Exception {
        String suffix = UUID.randomUUID().toString();
        authenticationService.register("Validation user", suffix + "@example.org", "secure-pass");
        var session = authenticationService.login(suffix + "@example.org", "secure-pass");

        mockMvc.perform(post("/api/users/me/teams")
                        .header("X-Access-Token", session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"maxSize\":0}"))
                .andExpect(status().isBadRequest());
    }
}
