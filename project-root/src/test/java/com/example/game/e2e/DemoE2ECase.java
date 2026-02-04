package com.example.game.e2e;

import com.example.game.dto.LoginRequestDTO;
import com.example.game.dto.ProfileRequestDTO;
import com.example.game.dto.TokenResponseDTO;
import com.example.game.dto.UserRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DemoE2ECase {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void demoFlow_registerLoginUpdateProfileAndRating() {
        String baseUrl = "http://localhost:" + port;

        registerUser(baseUrl, "e2e_user_1", "e2e1@example.com", "pass1");
        registerUser(baseUrl, "e2e_user_2", "e2e2@example.com", "pass2");

        String token1 = login(baseUrl, "e2e_user_1", "pass1");
        String token2 = login(baseUrl, "e2e_user_2", "pass2");

        updateProfile(baseUrl, token1, 100);
        updateProfile(baseUrl, token2, 50);

        ResponseEntity<String> profileResp = authorizedGet(baseUrl + "/api/v1/profiles/me", token1);
        assertEquals(HttpStatus.OK, profileResp.getStatusCode());
        assertTrue(profileResp.getBody().contains("\"score\":100"));

        ResponseEntity<String> ratingResp = authorizedGet(baseUrl + "/api/v1/ratings?sortBy=points&limit=10", token1);
        assertEquals(HttpStatus.OK, ratingResp.getStatusCode());
        assertTrue(ratingResp.getBody().contains("\"currentUserRank\":1"));
    }

    private void registerUser(String baseUrl, String username, String email, String password) {
        UserRequestDTO req = new UserRequestDTO(username, email, password);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/api/v1/users", req, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    private String login(String baseUrl, String username, String password) {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername(username);
        login.setPassword(password);
        ResponseEntity<TokenResponseDTO> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/tokens",
                login,
                TokenResponseDTO.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return "Bearer " + response.getBody().getToken();
    }

    private void updateProfile(String baseUrl, String token, int score) {
        ProfileRequestDTO update = new ProfileRequestDTO();
        update.setScore(score);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ProfileRequestDTO> entity = new HttpEntity<>(update, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/profiles/me",
                HttpMethod.PATCH,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private ResponseEntity<String> authorizedGet(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}
