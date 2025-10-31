package com.example.game.service;

import com.example.game.dto.LocationRequestDTO;
import com.example.game.model.Location;
import com.example.game.model.LocationGroup;
import com.example.game.repository.LocationRepository;
import com.example.game.repository.LocationGroupRepository;
import com.example.game.service.interfaces.ILocationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

@Service
public class LocationService implements ILocationService {

    private final LocationRepository locationRepository;
    private final LocationGroupRepository locationGroupRepository;
    private final Random random = new Random();

    public LocationService(LocationRepository locationRepository,
                           LocationGroupRepository locationGroupRepository) {
        this.locationRepository = locationRepository;
        this.locationGroupRepository = locationGroupRepository;
    }

    @Override
    public List<Location> getAllLocations() {
        try {
            return locationRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при получении списка локаций", e);
        }
    }

    @Override
    public List<Location> getLocationsByGroupId(Long groupId) {
        LocationGroup group = locationGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена"));
        return locationRepository.findByGroup(group);
    }

    @Override
    public Location addLocationFromDto(LocationRequestDTO dto) {
        LocationGroup group = locationGroupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена"));

        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLng(dto.getLng());
        location.setGroup(group);

        try {
            return locationRepository.save(location);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при создании локации", e);
        }
    }

    @Override
    public Location getRandomLocationByGroupId(Long groupId) {
        LocationGroup group = locationGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена"));

        List<Location> locations = locationRepository.findByGroup(group);
        if (locations.isEmpty()) return null;

        return locations.get(random.nextInt(locations.size()));
    }

    @Override
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Локация с id " + id + " не найдена");
        }
        locationRepository.deleteById(id);
    }
}
