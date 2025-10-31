package com.example.game.repository;

import com.example.game.model.GameError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameErrorRepository extends JpaRepository<GameError, Long> {
}
