package com.example.game.repository;

import com.example.game.model.LocationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationGroupRepository extends JpaRepository<LocationGroup, Long> {
    LocationGroup findByName(String name);
}
