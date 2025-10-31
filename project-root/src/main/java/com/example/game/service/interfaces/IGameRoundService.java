package com.example.game.service.interfaces;

import com.example.game.dto.GameRoundRequestDTO;
import com.example.game.model.GameRound;

import java.util.List;

public interface IGameRoundService {

    GameRound createFromDto(GameRoundRequestDTO dto);

    List<GameRound> getRoundsBySessionId(Long sessionId);

    GameRound getCurrentRoundBySessionId(Long sessionId);

    GameRound saveRound(GameRound round);
}
