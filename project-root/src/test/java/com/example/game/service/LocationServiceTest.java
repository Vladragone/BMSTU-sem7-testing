package com.example.game.service;

import com.example.game.dto.LocationRequestDTO;
import com.example.game.model.Location;
import com.example.game.model.LocationGroup;
import com.example.game.repository.LocationGroupRepository;
import com.example.game.repository.LocationRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationGroupRepository locationGroupRepository;

    @InjectMocks
    private LocationService locationService;

    private Location location;
    private LocationGroup group;
    private LocationRequestDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        group = new LocationGroup();
        group.setId(1L);
        group.setName("Europe");

        location = new Location();
        location.setId(2L);
        location.setLat(55.75);
        location.setLng(37.62);
        location.setGroup(group);

        dto = new LocationRequestDTO();
        dto.setLat(10.5);
        dto.setLng(20.5);
        dto.setGroupId(1L);
    }

    @Test
    void getAllLocations_success() {
        when(locationRepository.findAll()).thenReturn(List.of(location));
        List<Location> result = locationService.getAllLocations();
        assertEquals(1, result.size());
        verify(locationRepository).findAll();
    }

    @Test
    void getAllLocations_error() {
        when(locationRepository.findAll()).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> locationService.getAllLocations());
    }

    @Test
    void getLocationsByGroupId_success() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(locationRepository.findByGroup(group)).thenReturn(List.of(location));
        List<Location> result = locationService.getLocationsByGroupId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getLocationsByGroupId_groupNotFound() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> locationService.getLocationsByGroupId(1L));
    }

    @Test
    void addLocationFromDto_success() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        Location result = locationService.addLocationFromDto(dto);
        assertNotNull(result);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void addLocationFromDto_groupNotFound() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> locationService.addLocationFromDto(dto));
    }

    @Test
    void addLocationFromDto_saveError() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(locationRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> locationService.addLocationFromDto(dto));
    }

    @Test
    void getRandomLocationByGroupId_success() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(locationRepository.findByGroup(group)).thenReturn(List.of(location));
        Location result = locationService.getRandomLocationByGroupId(1L);
        assertNotNull(result);
    }

    @Test
    void getRandomLocationByGroupId_emptyList() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(locationRepository.findByGroup(group)).thenReturn(Collections.emptyList());
        Location result = locationService.getRandomLocationByGroupId(1L);
        assertNull(result);
    }

    @Test
    void getRandomLocationByGroupId_groupNotFound() {
        when(locationGroupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> locationService.getRandomLocationByGroupId(1L));
    }

    @Test
    void deleteLocation_success() {
        when(locationRepository.existsById(2L)).thenReturn(true);
        doNothing().when(locationRepository).deleteById(2L);
        assertDoesNotThrow(() -> locationService.deleteLocation(2L));
    }

    @Test
    void deleteLocation_notFound() {
        when(locationRepository.existsById(2L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> locationService.deleteLocation(2L));
    }
}
