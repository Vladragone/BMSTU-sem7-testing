package com.example.game.service.interfaces;

import com.example.game.dto.LocationRequestDTO;
import com.example.game.model.Location;

import java.util.List;

public interface ILocationService {
    List<Location> getAllLocations();
    List<Location> getLocationsByGroupId(Long groupId);
    Location addLocationFromDto(LocationRequestDTO dto);
    Location getRandomLocationByGroupId(Long groupId);
    void deleteLocation(Long id);
}
