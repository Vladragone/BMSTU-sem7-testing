package com.example.game.repository;

import com.example.game.model.Profile;
import com.example.game.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository repository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john");
        user.setPassword("123");
        user.setEmail("j@e.com");
        user.setRole("user");
        userRepository.save(user);

        Profile p = new Profile();
        p.setUser(user);
        p.setScore(100);
        p.setGameNum(3);
        p.setRegDate(LocalDateTime.now());
        repository.save(p);
    }

    @Test
    void findByUserUsername_success() {
        Optional<Profile> profile = repository.findByUserUsername("john");
        assertTrue(profile.isPresent());
    }

    @Test
    void findByUserUsername_notFound() {
        Optional<Profile> profile = repository.findByUserUsername("unknown");
        assertTrue(profile.isEmpty());
    }
}
