package com.example.game.it;

import com.example.game.dto.UserRequestDTO;
import com.example.game.repository.ProfileRepository;
import com.example.game.repository.UserRepository;
import com.example.game.service.RegistrationService;
import com.example.game.util.TestIds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RegistrationServiceITCase {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void register_createsUserAndProfile() {
        String username = TestIds.username("reg_it");
        String email = TestIds.email("reg_it");

        UserRequestDTO req = new UserRequestDTO(username, email, "password");
        var created = registrationService.register(req);

        assertNotNull(created.getId());
        assertEquals(username, created.getUsername());

        var userOpt = userRepository.findByUsername(username);
        assertTrue(userOpt.isPresent());

        var profileOpt = profileRepository.findByUserUsername(username);
        assertTrue(profileOpt.isPresent());
    }
}
