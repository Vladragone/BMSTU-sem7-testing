package com.example.game.it;

import com.example.game.dto.LoginRequestDTO;
import com.example.game.dto.UserRequestDTO;
import com.example.game.service.AuthService;
import com.example.game.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthServiceITCase {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuthService authService;

    @Test
    void authenticateUser_returnsToken() {
        registrationService.register(new UserRequestDTO("auth_it", "auth_it@example.com", "secret"));

        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername("auth_it");
        login.setPassword("secret");

        var token = authService.authenticateUser(login);
        assertNotNull(token);
        assertNotNull(token.getToken());
        assertFalse(token.getToken().isBlank());
    }
}

