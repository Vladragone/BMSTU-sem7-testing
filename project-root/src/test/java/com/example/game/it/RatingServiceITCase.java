package com.example.game.it;

import com.example.game.dto.UserRequestDTO;
import com.example.game.model.Profile;
import com.example.game.repository.ProfileRepository;
import com.example.game.repository.UserRepository;
import com.example.game.service.RatingService;
import com.example.game.service.RegistrationService;
import com.example.game.util.JwtUtil;
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
        String username1 = TestIds.username("rate_it_1");
        String email1 = TestIds.email("rate_it_1");
        String username2 = TestIds.username("rate_it_2");
        String email2 = TestIds.email("rate_it_2");

        registrationService.register(new UserRequestDTO(username1, email1, "pass1"));
        registrationService.register(new UserRequestDTO(username2, email2, "pass2"));

        var user1 = userRepository.findByUsername(username1).orElseThrow();

        Profile p1 = profileRepository.findByUserUsername(username1).orElseThrow();
        p1.setScore(TestIds.highScore());
        p1.setGameNum(3);
        profileRepository.save(p1);

        Profile p2 = profileRepository.findByUserUsername(username2).orElseThrow();
        p2.setScore(1);
        p2.setGameNum(1);
        profileRepository.save(p2);

        String token = "Bearer " + JwtUtil.generateToken(user1.getUsername(), user1.getRole(), user1.getId());
        var result = ratingService.getSortedRatingAndRank(token, "points", 10);

        assertNotNull(result);
        assertTrue(result.getCurrentUserRank() >= 1 && result.getCurrentUserRank() <= 2);
        assertTrue(result.getTopUsers().size() >= 2);
    }
}
