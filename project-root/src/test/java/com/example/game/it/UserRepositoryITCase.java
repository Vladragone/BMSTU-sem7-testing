package com.example.game.it;

import com.example.game.model.User;
import com.example.game.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserRepositoryITCase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_success() {
        User user = new User();
        user.setUsername("it_user");
        user.setEmail("it_user@example.com");
        user.setPassword("pass");
        user.setRole("user");
        userRepository.save(user);

        var found = userRepository.findByUsername("it_user");
        assertTrue(found.isPresent());
        assertEquals("it_user@example.com", found.get().getEmail());
    }

    @Test
    void existsByEmail_success() {
        User user = new User();
        user.setUsername("it_user2");
        user.setEmail("it_user2@example.com");
        user.setPassword("pass");
        user.setRole("user");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("it_user2@example.com"));
    }
}

