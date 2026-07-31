package pl.klejczyk.tpm.auth.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import pl.klejczyk.tpm.auth.TestcontainersConfiguration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerIT {

    /**
     * The tests generate their own key pair instead of reading the deployment one, so the suite
     * runs anywhere - including a clean checkout where no keys have been generated yet - and no
     * key material ends up in the repository.
     */
    @DynamicPropertySource
    static void signingKey(DynamicPropertyRegistry registry) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                        .encodeToString(pair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";

        Path file = Files.createTempFile("tpm-test-key-", ".pem");
        Files.writeString(file, pem);
        file.toFile().deleteOnExit();

        registry.add("tpm.jwt.private-key-location", () -> "file:" + file);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String requestToken(String username, String password) throws Exception {
        return mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * A JWT is signed, not encrypted, so the payload can be read without any key.
     */
    private JsonNode claimsOf(String token) {
        String payload = token.split("\\.")[1];
        return objectMapper.readTree(new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8));
    }

    @Test
    void issuesTokenCarryingTheUserIdAndRole() throws Exception {
        String body = requestToken("technik", "technik");

        JsonNode response = objectMapper.readTree(body);
        assertThat(response.get("expiresIn").asLong()).isEqualTo(1800);

        JsonNode claims = claimsOf(response.get("token").asString());
        assertThat(claims.get("sub").asString()).isEqualTo("tech-1");
        assertThat(claims.get("role").asString()).isEqualTo("TECHNICIAN");
        assertThat(claims.get("iss").asString()).isEqualTo("tpm-auth-service");
        assertThat(claims.get("exp").asLong()).isGreaterThan(claims.get("iat").asLong());
    }

    @Test
    void mapsEachDemoUserToItsOwnRole() throws Exception {
        assertThat(claimsOf(objectMapper.readTree(requestToken("operator", "operator"))
                .get("token").asString()).get("role").asString()).isEqualTo("OPERATOR");

        assertThat(claimsOf(objectMapper.readTree(requestToken("kierownik", "kierownik"))
                .get("token").asString()).get("role").asString()).isEqualTo("MANAGER");
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"technik\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Security property, not a feature: if a wrong password and an unknown username produced
     * different responses, the endpoint would become a user enumeration oracle.
     */
    @Test
    void doesNotRevealWhetherTheUsernameExists() throws Exception {
        String wrongPassword = mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"technik\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String unknownUser = mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(unknownUser).isEqualTo(wrongPassword);
    }

    @Test
    void rejectsRequestWithoutCredentials() throws Exception {
        mockMvc.perform(post("/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"technik\"}"))
                .andExpect(status().isBadRequest());
    }
}
