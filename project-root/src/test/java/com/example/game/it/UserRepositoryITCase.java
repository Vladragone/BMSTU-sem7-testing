package com.example.game.it;

import com.example.game.model.User;
import com.example.game.repository.UserRepository;
import com.example.game.util.TestIds;
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
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserRepositoryITCase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_success() {
        String username = TestIds.username("it_user");
        String email = TestIds.email("it_user");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("pass");
        user.setRole("user");
        userRepository.save(user);

        var found = userRepository.findByUsername(username);
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    void existsByEmail_success() {
        String username = TestIds.username("it_user2");
        String email = TestIds.email("it_user2");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("pass");
        user.setRole("user");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail(email));
    }
}
