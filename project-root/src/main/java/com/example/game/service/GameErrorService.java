package com.example.game.service;

import com.example.game.model.GameError;
import com.example.game.repository.GameErrorRepository;
import com.example.game.service.interfaces.IGameErrorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameErrorService implements IGameErrorService {

    private final GameErrorRepository gameErrorRepository;

    public GameErrorService(GameErrorRepository gameErrorRepository) {
        this.gameErrorRepository = gameErrorRepository;
    }

    @Override
    public List<GameError> getAllGameErrors() {
        try {
            return gameErrorRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при получении списка ошибок");
        }
    }
}
