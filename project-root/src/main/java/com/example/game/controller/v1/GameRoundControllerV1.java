package com.example.game.controller.v1;

import com.example.game.dto.GameRoundRequestDTO;
import com.example.game.dto.GameRoundResponseDTO;
import com.example.game.model.GameRound;
import com.example.game.service.interfaces.IGameRoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/gamerounds")
public class GameRoundControllerV1 {

    private final IGameRoundService gameRoundService;

    public GameRoundControllerV1(IGameRoundService gameRoundService) {
        this.gameRoundService = gameRoundService;
    }

    @Operation(summary = "Создать новый раунд")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Раунд успешно создан"),
            @ApiResponse(responseCode = "500", description = "Ошибка при сохранении раунда")
    })
    @PostMapping
    public ResponseEntity<GameRoundResponseDTO> createRound(@RequestBody GameRoundRequestDTO dto) {
        GameRound saved = gameRoundService.createFromDto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDto(saved));
    }

    @Operation(summary = "Получить все раунды по ID сессии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Раунды успешно получены"),
            @ApiResponse(responseCode = "204", description = "Раунды отсутствуют")
    })
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<GameRoundResponseDTO>> getRoundsBySession(@PathVariable Long sessionId) {
        List<GameRound> rounds = gameRoundService.getRoundsBySessionId(sessionId);
        if (rounds.isEmpty()) return ResponseEntity.noContent().build();

        List<GameRoundResponseDTO> dtos = rounds.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить текущий (последний) раунд по ID сессии")
    @GetMapping("/session/{sessionId}/current")
    public ResponseEntity<GameRoundResponseDTO> getCurrentRound(@PathVariable Long sessionId) {
        GameRound round = gameRoundService.getCurrentRoundBySessionId(sessionId);
        if (round == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponseDto(round));
    }

    private GameRoundResponseDTO toResponseDto(GameRound entity) {
        GameRoundResponseDTO dto = new GameRoundResponseDTO();
        dto.setId(entity.getId());
        dto.setSessionId(entity.getSession().getId());
        dto.setLocationId(entity.getLocation().getId());
        dto.setGuessLat(entity.getGuessLat());
        dto.setGuessLng(entity.getGuessLng());
        dto.setScore(entity.getScore());
        dto.setRoundNumber(entity.getRoundNumber());
        return dto;
    }
}
