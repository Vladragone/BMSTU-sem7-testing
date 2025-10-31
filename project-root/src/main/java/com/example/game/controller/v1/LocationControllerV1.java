package com.example.game.controller.v1;

import com.example.game.dto.LocationRequestDTO;
import com.example.game.dto.LocationResponseDTO;
import com.example.game.model.Location;
import com.example.game.service.interfaces.ILocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationControllerV1 {

    private final ILocationService locationService;

    public LocationControllerV1(ILocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "Получить все локации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Локации успешно получены"),
            @ApiResponse(responseCode = "204", description = "Локации отсутствуют")
    })
    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        if (locations.isEmpty()) return ResponseEntity.noContent().build();

        List<LocationResponseDTO> dtos = locations.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить все локации по ID группы")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Локации успешно получены"),
            @ApiResponse(responseCode = "204", description = "Локации отсутствуют")
    })
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<LocationResponseDTO>> getLocationsByGroup(@PathVariable Long groupId) {
        List<Location> locations = locationService.getLocationsByGroupId(groupId);
        if (locations.isEmpty()) return ResponseEntity.noContent().build();

        List<LocationResponseDTO> dtos = locations.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Добавить новую локацию")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Локация создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PostMapping
    public ResponseEntity<LocationResponseDTO> addLocation(@RequestBody LocationRequestDTO dto) {
        Location saved = locationService.addLocationFromDto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDto(saved));
    }

    @Operation(summary = "Получить случайную локацию по ID группы")
    @GetMapping("/random/{groupId}")
    public ResponseEntity<LocationResponseDTO> getRandomByGroup(@PathVariable Long groupId) {
        Location random = locationService.getRandomLocationByGroupId(groupId);
        if (random == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponseDto(random));
    }

    @Operation(summary = "Удалить локацию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    private LocationResponseDTO toResponseDto(Location entity) {
        LocationResponseDTO dto = new LocationResponseDTO();
        dto.setId(entity.getId());
        dto.setLat(entity.getLat());
        dto.setLng(entity.getLng());
        dto.setGroupId(entity.getGroup().getId());
        return dto;
    }
}
