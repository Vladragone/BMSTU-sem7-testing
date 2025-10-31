package com.example.game.service;

import com.example.game.dto.RatingResponseDTO;
import com.example.game.model.Profile;
import com.example.game.model.User;
import com.example.game.repository.ProfileRepository;
import com.example.game.service.interfaces.ITokenParser;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RatingServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ITokenParser tokenParser;

    @InjectMocks
    private RatingService ratingService;

    private Profile profile1;
    private Profile profile2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = new User();
        user1.setUsername("alice");
        user2 = new User();
        user2.setUsername("bob");

        profile1 = new Profile();
        profile1.setUser(user1);
        profile1.setScore(200);
        profile1.setGameNum(5);

        profile2 = new Profile();
        profile2.setUser(user2);
        profile2.setScore(100);
        profile2.setGameNum(10);
    }

    @Test
    void getSortedRatingAndRank_success_sortByScore() {
        when(tokenParser.getUsername("token")).thenReturn("alice");
        when(profileRepository.findAll()).thenReturn(List.of(profile1, profile2));
        RatingResponseDTO response = ratingService.getSortedRatingAndRank("token", "score", 5);
        assertEquals(2, response.getTopUsers().size());
        assertEquals(1, response.getCurrentUserRank());
        assertEquals("score", response.getSortBy());
    }

    @Test
    void getSortedRatingAndRank_success_sortByGames() {
        when(tokenParser.getUsername("token")).thenReturn("bob");
        when(profileRepository.findAll()).thenReturn(List.of(profile1, profile2));
        RatingResponseDTO response = ratingService.getSortedRatingAndRank("token", "games", 5);
        assertEquals(2, response.getTopUsers().size());
        assertEquals(1, response.getCurrentUserRank());
        assertEquals("games", response.getSortBy());
    }

    @Test
    void getSortedRatingAndRank_noProfiles() {
        when(tokenParser.getUsername("token")).thenReturn("alice");
        when(profileRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(ResponseStatusException.class, () -> ratingService.getSortedRatingAndRank("token", "score", 5));
    }

    @Test
    void getSortedRatingAndRank_userNotFound() {
        when(tokenParser.getUsername("token")).thenReturn("charlie");
        when(profileRepository.findAll()).thenReturn(List.of(profile1, profile2));
        assertThrows(ResponseStatusException.class, () -> ratingService.getSortedRatingAndRank("token", "score", 5));
    }

    @Test
    void getSortedRatingAndRank_error() {
        when(tokenParser.getUsername("token")).thenReturn("alice");
        when(profileRepository.findAll()).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> ratingService.getSortedRatingAndRank("token", "score", 5));
    }
}
