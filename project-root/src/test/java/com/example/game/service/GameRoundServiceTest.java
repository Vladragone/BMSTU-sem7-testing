package com.example.game.service;

import com.example.game.dto.GameRoundRequestDTO;
import com.example.game.model.GameRound;
import com.example.game.model.GameSession;
import com.example.game.model.Location;
import com.example.game.repository.GameRoundRepository;
import com.example.game.repository.GameSessionRepository;
import com.example.game.repository.LocationRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameRoundServiceTest {

    @Mock
    private GameRoundRepository gameRoundRepository;
    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private GameRoundService gameRoundService;

    private GameRoundRequestDTO dto;
    private GameSession session;
    private Location location;
    private GameRound round;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dto = new GameRoundRequestDTO();
        dto.setSessionId(1L);
        dto.setLocationId(2L);
        dto.setGuessLat(55.7);
        dto.setGuessLng(37.6);
        dto.setScore(500);
        dto.setRoundNumber(1);

        session = new GameSession();
        session.setId(1L);

        location = new Location();
        location.setId(2L);

        round = new GameRound();
        round.setId(10L);
        round.setSession(session);
        round.setLocation(location);
    }

    @Test
    void createFromDto_success() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(gameRoundRepository.save(any(GameRound.class))).thenReturn(round);

        GameRound result = gameRoundService.createFromDto(dto);

        assertNotNull(result);
        verify(gameRoundRepository).save(any(GameRound.class));
    }

    @Test
    void createFromDto_sessionNotFound() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> gameRoundService.createFromDto(dto));
    }

    @Test
    void createFromDto_locationNotFound() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(locationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> gameRoundService.createFromDto(dto));
    }

    @Test
    void createFromDto_saveError() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(gameRoundRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> gameRoundService.createFromDto(dto));
    }

    @Test
    void getRoundsBySessionId_success() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(gameRoundRepository.findBySession(session)).thenReturn(List.of(round));
        List<GameRound> result = gameRoundService.getRoundsBySessionId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getRoundsBySessionId_sessionNotFound() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> gameRoundService.getRoundsBySessionId(1L));
    }

    @Test
    void getCurrentRoundBySessionId_success() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(gameRoundRepository.findTopBySessionOrderByRoundNumberDesc(session)).thenReturn(round);
        GameRound result = gameRoundService.getCurrentRoundBySessionId(1L);
        assertEquals(10L, result.getId());
    }

    @Test
    void getCurrentRoundBySessionId_sessionNotFound() {
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> gameRoundService.getCurrentRoundBySessionId(1L));
    }

    @Test
    void saveRound_success() {
        when(gameRoundRepository.save(round)).thenReturn(round);
        GameRound result = gameRoundService.saveRound(round);
        assertEquals(10L, result.getId());
    }

    @Test
    void saveRound_error() {
        when(gameRoundRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> gameRoundService.saveRound(round));
    }
}
