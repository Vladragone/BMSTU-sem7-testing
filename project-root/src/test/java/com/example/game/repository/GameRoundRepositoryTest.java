package com.example.game.repository;

import com.example.game.model.GameRound;
import com.example.game.model.GameSession;
import com.example.game.model.Location;
import com.example.game.model.LocationGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameRoundRepositoryTest {

    @Autowired
    private GameRoundRepository repository;

    @Autowired
    private GameSessionRepository sessionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private LocationGroupRepository groupRepository;

    private GameSession session;
    private Location location;

    @BeforeEach
    void setUp() {
        LocationGroup group = new LocationGroup();
        group.setName("Test Group");
        groupRepository.save(group);

        location = new Location();
        location.setGroup(group);
        location.setLat(55.75);
        location.setLng(37.61);
        locationRepository.save(location);

        session = new GameSession();
        session.setUserId(1L);
        session.setTotalRounds(5);
        session.setTotalScore(0);
        session.setLocationGroup(group);
        sessionRepository.save(session);
    }

    @Test
    void findBySession_success() {
        GameRound r = new GameRound();
        r.setSession(session);
        r.setLocation(location);
        r.setRoundNumber(1);
        repository.save(r);

        List<GameRound> rounds = repository.findBySession(session);
        assertEquals(1, rounds.size());
        assertEquals(1, rounds.get(0).getRoundNumber());
    }

    @Test
    void findTopBySessionOrderByRoundNumberDesc_empty() {
        assertNull(repository.findTopBySessionOrderByRoundNumberDesc(session));
    }
}
