package com.example.game.repository;

import com.example.game.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("mike");
        user.setPassword("pass");
        user.setEmail("m@e.com");
        user.setRole("user");
        repository.save(user);
    }

    @Test
    void findByUsername_success() {
        Optional<User> found = repository.findByUsername("mike");
        assertTrue(found.isPresent());
    }

    @Test
    void existsByEmail_success() {
        boolean exists = repository.existsByEmail("m@e.com");
        assertTrue(exists);
    }
}
