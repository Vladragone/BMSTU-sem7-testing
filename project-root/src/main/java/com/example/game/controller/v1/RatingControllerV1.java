package com.example.game.controller.v1;

import com.example.game.dto.RatingResponseDTO;
import com.example.game.service.interfaces.IRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
public class RatingControllerV1 {

    private final IRatingService ratingService;

    public RatingControllerV1(IRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Operation(summary = "Получить рейтинг игроков")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Рейтинг успешно получен"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден в рейтинге"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<RatingResponseDTO> getRatings(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "points") String sortBy,
            @RequestParam(defaultValue = "10") int limit) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RatingResponseDTO result = ratingService.getSortedRatingAndRank(token, sortBy, limit);
        return ResponseEntity.ok(result);
    }
}
