package com.example.game.controller.v1;

import com.example.game.dto.GameSessionRequestDTO;
import com.example.game.dto.GameSessionResponseDTO;
import com.example.game.model.GameSession;
import com.example.game.service.interfaces.IGameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/gamesessions")
public class GameSessionControllerV1 {

    private final IGameSessionService gameSessionService;

    public GameSessionControllerV1(IGameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @Operation(summary = "Создать новую игровую сессию")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Игровая сессия успешно создана"),
            @ApiResponse(responseCode = "500", description = "Ошибка при сохранении сессии")
    })
    @PostMapping
    public ResponseEntity<GameSessionResponseDTO> createGameSession(@RequestBody GameSessionRequestDTO dto) {
        GameSession created = gameSessionService.createFromDto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDto(created));
    }

    @Operation(summary = "Получить все сессии пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сессии успешно получены"),
            @ApiResponse(responseCode = "204", description = "Сессий нет")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GameSessionResponseDTO>> getUserSessions(@PathVariable Long userId) {
        List<GameSession> sessions = gameSessionService.getSessionsByUser(userId);
        if (sessions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<GameSessionResponseDTO> dtos = sessions.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить сессию по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сессия найдена"),
            @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GameSessionResponseDTO> getSessionById(@PathVariable Long id) {
        return gameSessionService.getSessionById(id)
                .map(session -> ResponseEntity.ok(toResponseDto(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Удалить игровую сессию")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Сессия удалена"),
            @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        gameSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    private GameSessionResponseDTO toResponseDto(GameSession entity) {
        GameSessionResponseDTO dto = new GameSessionResponseDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setLocationGroupId(entity.getLocationGroup().getId());
        dto.setTotalScore(entity.getTotalScore());
        dto.setTotalRounds(entity.getTotalRounds());
        return dto;
    }
}
