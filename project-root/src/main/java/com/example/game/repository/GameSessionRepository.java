package com.example.game.repository;

import com.example.game.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    List<GameSession> findByUserId(Long userId);
    //Optional<GameSession> findByUserIdAndIsActiveTrue(Long userId);
}
