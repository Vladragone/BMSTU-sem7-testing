package com.example.game.it;

import com.example.game.dto.UserRequestDTO;
import com.example.game.model.Profile;
import com.example.game.repository.ProfileRepository;
import com.example.game.repository.UserRepository;
import com.example.game.service.RatingService;
import com.example.game.service.RegistrationService;
import com.example.game.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RatingServiceITCase {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RatingService ratingService;

    @Test
    void getSortedRatingAndRank_returnsRank() {
        registrationService.register(new UserRequestDTO("rate_it_1", "rate_it_1@example.com", "pass1"));
        registrationService.register(new UserRequestDTO("rate_it_2", "rate_it_2@example.com", "pass2"));

        var user1 = userRepository.findByUsername("rate_it_1").orElseThrow();

        Profile p1 = profileRepository.findByUserUsername("rate_it_1").orElseThrow();
        p1.setScore(120);
        p1.setGameNum(3);
        profileRepository.save(p1);

        Profile p2 = profileRepository.findByUserUsername("rate_it_2").orElseThrow();
        p2.setScore(50);
        p2.setGameNum(1);
        profileRepository.save(p2);

        String token = "Bearer " + JwtUtil.generateToken(user1.getUsername(), user1.getRole(), user1.getId());
        var result = ratingService.getSortedRatingAndRank(token, "points", 10);

        assertNotNull(result);
        assertEquals(1, result.getCurrentUserRank());
        assertTrue(result.getTopUsers().size() >= 2);
    }
}
