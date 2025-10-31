package com.example.game.service;

import com.example.game.dto.GameSessionRequestDTO;
import com.example.game.model.GameSession;
import com.example.game.model.LocationGroup;
import com.example.game.repository.GameSessionRepository;
import com.example.game.repository.LocationGroupRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameSessionServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private LocationGroupRepository locationGroupRepository;

    @InjectMocks
    private GameSessionService gameSessionService;

    private GameSessionRequestDTO dto;
    private GameSession session;
    private LocationGroup group;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        group = new LocationGroup();
        group.setId(1L);
        group.setName("Europe");

        dto = new GameSessionRequestDTO();
        dto.setUserId(5L);
        dto.setLocationGroupId(1L);
        dto.setTotalRounds(3);

        session = new GameSession();
        session.setId(10L);
        session.setUserId(5L);
        session.setLocationGroup(group);
        session.setTotalRounds(3);
        session.setTotalScore(0);
    }

    @Test
    void createFromDto_success() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(session);
        GameSession result = gameSessionService.createFromDto(dto);
        assertEquals(5L, result.getUserId());
        verify(gameSessionRepository).save(any(GameSession.class));
    }

    @Test
    void createFromDto_groupNotFound() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> gameSessionService.createFromDto(dto));
    }

    @Test
    void saveGameSession_success() {
        when(gameSessionRepository.save(session)).thenReturn(session);
        GameSession result = gameSessionService.saveGameSession(session);
        assertEquals(10L, result.getId());
    }

    @Test
    void saveGameSession_error() {
        when(gameSessionRepository.save(any())).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> gameSessionService.saveGameSession(session));
    }

    @Test
    void getSessionsByUser_success() {
        when(gameSessionRepository.findByUserId(5L)).thenReturn(List.of(session));
        List<GameSession> result = gameSessionService.getSessionsByUser(5L);
        assertEquals(1, result.size());
    }

    @Test
    void getSessionsByUser_error() {
        when(gameSessionRepository.findByUserId(5L)).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> gameSessionService.getSessionsByUser(5L));
    }

    @Test
    void getSessionById_success() {
        when(gameSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        Optional<GameSession> result = gameSessionService.getSessionById(10L);
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void getSessionById_empty() {
        when(gameSessionRepository.findById(10L)).thenReturn(Optional.empty());
        Optional<GameSession> result = gameSessionService.getSessionById(10L);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteSession_success() {
        when(gameSessionRepository.existsById(10L)).thenReturn(true);
        doNothing().when(gameSessionRepository).deleteById(10L);
        assertDoesNotThrow(() -> gameSessionService.deleteSession(10L));
    }

    @Test
    void deleteSession_notFound() {
        when(gameSessionRepository.existsById(10L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> gameSessionService.deleteSession(10L));
    }
}
