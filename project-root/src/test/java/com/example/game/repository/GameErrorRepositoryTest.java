package com.example.game.repository;

import com.example.game.model.GameError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameErrorRepositoryTest {

    @Autowired
    private GameErrorRepository repository;

    @Test
    void saveAndFindAll() {
        GameError e = new GameError();
        e.setName("Crash");
        repository.save(e);
        List<GameError> result = repository.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void findAll_empty() {
        List<GameError> result = repository.findAll();
        assertTrue(result.isEmpty());
    }
}
