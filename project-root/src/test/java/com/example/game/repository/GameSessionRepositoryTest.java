package com.example.game.repository;

import com.example.game.model.GameSession;
import com.example.game.model.LocationGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameSessionRepositoryTest {

    @Autowired
    private GameSessionRepository repository;

    @Autowired
    private LocationGroupRepository groupRepository;

    private GameSession session;

    @BeforeEach
    void setUp() {
        LocationGroup group = new LocationGroup();
        group.setName("Test Group");
        groupRepository.save(group);

        session = new GameSession();
        session.setUserId(2L);
        session.setTotalRounds(3);
        session.setTotalScore(0);
        session.setLocationGroup(group);
        repository.save(session);
    }

    @Test
    void findByUserId_success() {
        List<GameSession> sessions = repository.findByUserId(2L);
        assertEquals(1, sessions.size());
        assertEquals(session.getUserId(), sessions.get(0).getUserId());
    }

    @Test
    void findById_notFound() {
        Optional<GameSession> result = repository.findById(999L);
        assertTrue(result.isEmpty());
    }
}
