package com.example.game.service;

import com.example.game.model.LocationGroup;
import com.example.game.repository.LocationGroupRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationGroupServiceTest {

    @Mock
    private LocationGroupRepository repository;

    @InjectMocks
    private LocationGroupService service;

    private LocationGroup group;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        group = new LocationGroup();
        group.setId(1L);
        group.setName("Asia");
    }

    @Test
    void addGroup_success() {
        when(repository.save(group)).thenReturn(group);
        LocationGroup result = service.addGroup(group);
        assertEquals("Asia", result.getName());
        verify(repository).save(group);
    }

    @Test
    void addGroup_error() {
        when(repository.save(any())).thenThrow(new RuntimeException("DB fail"));
        assertThrows(ResponseStatusException.class, () -> service.addGroup(group));
    }

    @Test
    void getAllGroups_success() {
        when(repository.findAll()).thenReturn(List.of(group));
        List<LocationGroup> result = service.getAllGroups();
        assertEquals(1, result.size());
    }

    @Test
    void getAllGroups_error() {
        when(repository.findAll()).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> service.getAllGroups());
    }

    @Test
    void getGroupByName_success() {
        when(repository.findByName("Asia")).thenReturn(group);
        LocationGroup result = service.getGroupByName("Asia");
        assertEquals("Asia", result.getName());
    }

    @Test
    void getGroupByName_notFound() {
        when(repository.findByName("Europe")).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> service.getGroupByName("Europe"));
    }

    @Test
    void getGroupByName_error() {
        when(repository.findByName(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(ResponseStatusException.class, () -> service.getGroupByName("Asia"));
    }

    @Test
    void deleteGroup_success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);
        assertDoesNotThrow(() -> service.deleteGroup(1L));
    }

    @Test
    void deleteGroup_notFound() {
        when(repository.existsById(1L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> service.deleteGroup(1L));
    }

    @Test
    void deleteGroup_error() {
        when(repository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(repository).deleteById(1L);
        assertThrows(ResponseStatusException.class, () -> service.deleteGroup(1L));
    }
}
