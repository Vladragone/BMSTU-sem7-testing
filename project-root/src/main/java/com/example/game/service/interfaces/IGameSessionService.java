package com.example.game.service.interfaces;

import com.example.game.dto.GameSessionRequestDTO;
import com.example.game.model.GameSession;

import java.util.List;
import java.util.Optional;

public interface IGameSessionService {

    GameSession createFromDto(GameSessionRequestDTO dto);

    GameSession saveGameSession(GameSession gameSession);

    List<GameSession> getSessionsByUser(Long userId);

    Optional<GameSession> getSessionById(Long id);

    void deleteSession(Long id);
}
