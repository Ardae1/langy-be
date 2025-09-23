package com.example.languageservice;
import com.example.languageservice.api.dto.ChatMessageRequest;
import com.example.languageservice.api.dto.StartSessionRequest;
import com.example.languageservice.api.dto.AnswerRequest;
import com.example.languageservice.api.dto.EndSessionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.lang.NonNull;
import org.testcontainers.containers.GenericContainer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.notNullValue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = FullIntegrationTest.Initializer.class)
@ActiveProfiles("test")
public class FullIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static final String KEYCLOAK_USERNAME = "testuser";
    private static final String KEYCLOAK_PASSWORD = "testpassword";

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("languageservice")
            .withUsername("postgres")
            .withPassword("password");

    @Container
    private static final GenericContainer<?> keycloakContainer = new GenericContainer<>("quay.io/keycloak/keycloak:15.0.2")
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", KEYCLOAK_USERNAME)
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", KEYCLOAK_PASSWORD)
            .withCommand("-b 0.0.0.0");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + "http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080) + "/auth/realms/master/protocol/openid-connect/certs",
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + "http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080) + "/auth/realms/master"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @BeforeAll
    static void setUp() throws Exception {
        // Wait for keycloak to start and get admin token
        String tokenEndpoint = "http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080) + "/auth/realms/master/protocol/openid-connect/token";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "client_id=admin-cli&grant_type=password&username=" + KEYCLOAK_USERNAME + "&password=" + KEYCLOAK_PASSWORD
                ))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        String adminToken = om.readTree(response.body()).get("access_token").asText();

        // Create a test user in Keycloak for our tests
        String usersEndpoint = "http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080) + "/auth/admin/realms/master/users";
        String userPayload = "{\"username\": \"testuser\", \"enabled\": true, \"credentials\": [{\"type\": \"password\", \"value\": \"testpassword\"}]}";

        request = HttpRequest.newBuilder()
                .uri(URI.create(usersEndpoint))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userPayload))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        // Now get a token for the test user
        request = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "client_id=account&grant_type=password&username=testuser&password=testpassword"
                ))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jwtToken = om.readTree(response.body()).get("access_token").asText();
    }

    @Test
    public void testFullSessionFlow() throws Exception {
        // Step 1: Start a new session
        StartSessionRequest startRequest = new StartSessionRequest("en");
        MvcResult startResult = mockMvc.perform(post("/api/v1/sessions/start")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").exists())
                .andReturn();

        String sessionId = objectMapper.readTree(startResult.getResponse().getContentAsString()).get("sessionId").asText();

        // Step 2: Send a chat message
        ChatMessageRequest chatRequest = new ChatMessageRequest(UUID.fromString(sessionId), "What is a good starting word?");
        mockMvc.perform(post("/api/v1/chat/message")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());

        // Step 3: Answer a word
        AnswerRequest answerRequest = new AnswerRequest(UUID.fromString(sessionId), "hello", true);
        mockMvc.perform(post("/api/v1/sessions/answer")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
                .andExpect(status().isOk());

        // Step 4: End the session
        EndSessionRequest endRequest = new EndSessionRequest(UUID.fromString(sessionId));
        mockMvc.perform(post("/api/v1/sessions/end")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(endRequest)))
                .andExpect(status().isOk());

        // Step 5: Get session summary
        mockMvc.perform(get("/api/v1/sessions/{sessionId}/summary", sessionId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.summary").value(notNullValue()));
    }
}
