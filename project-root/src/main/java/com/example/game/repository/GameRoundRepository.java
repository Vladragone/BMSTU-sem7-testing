package com.example.game.repository;

import com.example.game.model.GameRound;
import com.example.game.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRoundRepository extends JpaRepository<GameRound, Long> {

    List<GameRound> findBySession(GameSession session);
    GameRound findTopBySessionOrderByRoundNumberDesc(GameSession session);
}
