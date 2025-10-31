package com.example.game.controller.v1;

import com.example.game.dto.GameErrorResponseDTO;
import com.example.game.service.interfaces.IGameErrorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/game-errors")
public class GameErrorControllerV1 {

    private final IGameErrorService gameErrorService;

    public GameErrorControllerV1(IGameErrorService gameErrorService) {
        this.gameErrorService = gameErrorService;
    }

    @Operation(summary = "Получить список ошибок игры")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ошибки успешно получены"),
            @ApiResponse(responseCode = "204", description = "Ошибки отсутствуют")
    })
    @GetMapping
    public ResponseEntity<List<GameErrorResponseDTO>> getAllGameErrors() {
        List<GameErrorResponseDTO> errors = gameErrorService.getAllGameErrors()
                .stream()
                .map(e -> new GameErrorResponseDTO(e.getId(), e.getName()))
                .collect(Collectors.toList());
        if (errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(errors);
    }
}
