package com.example.game.it;

import com.example.game.dto.UserRequestDTO;
import com.example.game.repository.ProfileRepository;
import com.example.game.repository.UserRepository;
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
class RegistrationServiceITCase {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void register_createsUserAndProfile() {
        UserRequestDTO req = new UserRequestDTO("reg_it", "reg_it@example.com", "password");
        var created = registrationService.register(req);

        assertNotNull(created.getId());
        assertEquals("reg_it", created.getUsername());

        var userOpt = userRepository.findByUsername("reg_it");
        assertTrue(userOpt.isPresent());

        var profileOpt = profileRepository.findByUserUsername("reg_it");
        assertTrue(profileOpt.isPresent());
    }
}

