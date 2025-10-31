package com.example.game.controller.v1;

import com.example.game.dto.LocationGroupRequestDTO;
import com.example.game.dto.LocationGroupResponseDTO;
import com.example.game.service.interfaces.ILocationGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/location-groups")
public class LocationGroupControllerV1 {

    private final ILocationGroupService locationGroupService;

    public LocationGroupControllerV1(ILocationGroupService locationGroupService) {
        this.locationGroupService = locationGroupService;
    }

    @Operation(summary = "Получить все группы локаций")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Группы успешно получены"),
            @ApiResponse(responseCode = "204", description = "Нет данных")
    })
    @GetMapping
    public ResponseEntity<List<LocationGroupResponseDTO>> getAllGroups() {
        List<LocationGroupResponseDTO> groups = locationGroupService.getAllGroups()
                .stream()
                .map(g -> new LocationGroupResponseDTO(g.getId(), g.getName()))
                .collect(Collectors.toList());
        if (groups.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Создать новую группу локаций")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Группа успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PostMapping
    public ResponseEntity<LocationGroupResponseDTO> createGroup(@RequestBody LocationGroupRequestDTO dto) {
        var saved = locationGroupService.addGroup(dto.toEntity());
        var response = new LocationGroupResponseDTO(saved.getId(), saved.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить группу по имени")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Группа найдена"),
            @ApiResponse(responseCode = "404", description = "Группа не найдена")
    })
    @GetMapping("/{name}")
    public ResponseEntity<LocationGroupResponseDTO> getGroupByName(@PathVariable String name) {
        var group = locationGroupService.getGroupByName(name);
        var response = new LocationGroupResponseDTO(group.getId(), group.getName());
        return ResponseEntity.ok(response);
    }
}
