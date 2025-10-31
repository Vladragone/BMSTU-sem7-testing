package com.example.game.repository;

import com.example.game.model.Location;
import com.example.game.model.LocationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByGroup(LocationGroup group);
}
