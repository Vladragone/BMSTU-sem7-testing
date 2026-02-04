package com.example.game.it;

import com.example.game.dto.GameSessionRequestDTO;
import com.example.game.model.LocationGroup;
import com.example.game.repository.LocationGroupRepository;
import com.example.game.service.GameSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class GameSessionServiceITCase {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private LocationGroupRepository locationGroupRepository;

    @Test
    void createFromDto_persistsSession() {
        LocationGroup group = locationGroupRepository.findByName("Default Group");
        assertNotNull(group);

        GameSessionRequestDTO dto = new GameSessionRequestDTO();
        dto.setUserId(10L);
        dto.setLocationGroupId(group.getId());
        dto.setTotalRounds(5);

        var session = gameSessionService.createFromDto(dto);
        assertNotNull(session.getId());
        assertEquals(0, session.getTotalScore());
        assertEquals(5, session.getTotalRounds());

        var sessions = gameSessionService.getSessionsByUser(10L);
        assertEquals(1, sessions.size());
    }
}

