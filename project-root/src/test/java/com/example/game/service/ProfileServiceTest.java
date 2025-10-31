package com.example.game.service;

import com.example.game.dto.ProfileRequestDTO;
import com.example.game.model.Profile;
import com.example.game.model.User;
import com.example.game.repository.ProfileRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private Profile profile;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUsername("testuser");

        profile = new Profile();
        profile.setUser(user);
        profile.setScore(100);
        profile.setGameNum(5);
    }

    @Test
    void getProfile_success() {
        when(profileRepository.findByUserUsername("testuser")).thenReturn(Optional.of(profile));
        Profile result = profileService.getProfile("testuser");
        assertNotNull(result);
        assertEquals(100, result.getScore());
        verify(profileRepository).findByUserUsername("testuser");
    }

    @Test
    void getProfile_notFound() {
        when(profileRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        Profile result = profileService.getProfile("unknown");
        assertNull(result);
    }

    @Test
    void getProfile_error() {
        when(profileRepository.findByUserUsername(any())).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> profileService.getProfile("testuser"));
    }

    @Test
    void updateProfile_success() {
        ProfileRequestDTO dto = new ProfileRequestDTO();
        dto.setScore(50);
        when(profileRepository.findByUserUsername("testuser")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        Profile updated = profileService.updateProfile(dto, "testuser");
        assertEquals(150, updated.getScore());
        assertEquals(6, updated.getGameNum());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void updateProfile_profileNotFound() {
        ProfileRequestDTO dto = new ProfileRequestDTO();
        dto.setScore(50);
        when(profileRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        Profile result = profileService.updateProfile(dto, "unknown");
        assertNull(result);
    }

    @Test
    void updateProfile_noScoreProvided() {
        ProfileRequestDTO dto = new ProfileRequestDTO();
        when(profileRepository.findByUserUsername("testuser")).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        Profile updated = profileService.updateProfile(dto, "testuser");
        assertEquals(100, updated.getScore());
        assertEquals(5, updated.getGameNum());
    }

    @Test
    void updateProfile_error() {
        ProfileRequestDTO dto = new ProfileRequestDTO();
        dto.setScore(50);
        when(profileRepository.findByUserUsername("testuser")).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> profileService.updateProfile(dto, "testuser"));
    }
}
