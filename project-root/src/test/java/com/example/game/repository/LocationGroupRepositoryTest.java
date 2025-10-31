package com.example.game.repository;

import com.example.game.model.LocationGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LocationGroupRepositoryTest {

    @Autowired
    private LocationGroupRepository repository;

    private LocationGroup group;

    @BeforeEach
    void setUp() {
        group = new LocationGroup();
        group.setName("TestGroup");
        repository.save(group);
    }

    @Test
    void findByName_success() {
        LocationGroup found = repository.findByName("TestGroup");
        assertNotNull(found);
        assertEquals("TestGroup", found.getName());
    }

    @Test
    void findByName_notFound() {
        LocationGroup found = repository.findByName("UnknownGroup");
        assertNull(found);
    }
}
