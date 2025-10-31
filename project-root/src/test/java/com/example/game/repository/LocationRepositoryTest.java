package com.example.game.repository;

import com.example.game.model.Location;
import com.example.game.model.LocationGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private LocationGroupRepository groupRepository;

    private LocationGroup group;

    @BeforeEach
    void setUp() {
        group = new LocationGroup();
        group.setName("Europe");
        groupRepository.save(group);
    }

    @Test
    void findByGroup_success() {
        Location l = new Location();
        l.setLat(55.0);
        l.setLng(37.0);
        l.setGroup(group);
        locationRepository.save(l);
        List<Location> list = locationRepository.findByGroup(group);
        assertEquals(1, list.size());
    }

    @Test
    void findByGroup_empty() {
        LocationGroup other = new LocationGroup();
        other.setName("Asia");
        groupRepository.save(other);
        List<Location> list = locationRepository.findByGroup(other);
        assertTrue(list.isEmpty());
    }
}
