package com.example.game.controller.v1;

import com.example.game.dto.FeedbackRequestDTO;
import com.example.game.dto.FeedbackResponseDTO;
import com.example.game.service.interfaces.IFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackControllerV1 {

    private final IFeedbackService feedbackService;

    public FeedbackControllerV1(IFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @Operation(summary = "Добавить отзыв")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Отзыв успешно сохранён"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    @PostMapping
    public ResponseEntity<FeedbackResponseDTO> addFeedback(@RequestBody FeedbackRequestDTO dto) {
        var saved = feedbackService.saveFeedback(dto.toEntity());
        var response = new FeedbackResponseDTO(saved.getId(), saved.getUserId(), saved.getRating(), saved.getProblem(), saved.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить список всех отзывов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Отзывы успешно получены"),
            @ApiResponse(responseCode = "204", description = "Отзывы отсутствуют")
    })
    @GetMapping
    public ResponseEntity<List<FeedbackResponseDTO>> getAllFeedbacks() {
        List<FeedbackResponseDTO> feedbacks = feedbackService.getAllFeedbacks()
                .stream()
                .map(f -> new FeedbackResponseDTO(f.getId(), f.getUserId(), f.getRating(), f.getProblem(), f.getDescription()))
                .collect(Collectors.toList());
        if (feedbacks.isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.ok(feedbacks);
    }

    @Operation(summary = "Получить отзыв по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Отзыв найден"),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponseDTO> getFeedbackById(@PathVariable Long id) {
        var feedback = feedbackService.getFeedbackById(id);
        var response = new FeedbackResponseDTO(feedback.getId(), feedback.getUserId(), feedback.getRating(), feedback.getProblem(), feedback.getDescription());
        return ResponseEntity.ok(response);
    }
}
