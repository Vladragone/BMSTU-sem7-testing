package com.example.game.service;

import com.example.game.dto.GameRoundRequestDTO;
import com.example.game.model.GameRound;
import com.example.game.model.GameSession;
import com.example.game.model.Location;
import com.example.game.repository.GameRoundRepository;
import com.example.game.repository.GameSessionRepository;
import com.example.game.repository.LocationRepository;
import com.example.game.service.interfaces.IGameRoundService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameRoundService implements IGameRoundService {

    private final GameRoundRepository gameRoundRepository;
    private final GameSessionRepository gameSessionRepository;
    private final LocationRepository locationRepository;

    public GameRoundService(GameRoundRepository gameRoundRepository,
                            GameSessionRepository gameSessionRepository,
                            LocationRepository locationRepository) {
        this.gameRoundRepository = gameRoundRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public GameRound createFromDto(GameRoundRequestDTO dto) {
        GameSession session = gameSessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сессия не найдена"));
        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Локация не найдена"));

        GameRound round = new GameRound();
        round.setSession(session);
        round.setLocation(location);
        round.setGuessLat(dto.getGuessLat());
        round.setGuessLng(dto.getGuessLng());
        round.setScore(dto.getScore() != null ? dto.getScore() : 0);
        round.setRoundNumber(dto.getRoundNumber());

        try {
            return gameRoundRepository.save(round);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при сохранении раунда", e);
        }
    }

    @Override
    public List<GameRound> getRoundsBySessionId(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сессия не найдена"));
        return gameRoundRepository.findBySession(session);
    }

    @Override
    public GameRound getCurrentRoundBySessionId(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сессия не найдена"));
        return gameRoundRepository.findTopBySessionOrderByRoundNumberDesc(session);
    }

    @Override
    public GameRound saveRound(GameRound round) {
        try {
            return gameRoundRepository.save(round);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при сохранении раунда", e);
        }
    }
}
