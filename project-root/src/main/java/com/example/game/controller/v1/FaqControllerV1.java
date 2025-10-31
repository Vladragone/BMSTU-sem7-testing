package com.example.game.controller.v1;

import com.example.game.dto.FaqRequestDTO;
import com.example.game.dto.FaqResponseDTO;
import com.example.game.dto.FaqUpdateDTO;
import com.example.game.service.interfaces.IFaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/faqs")
public class FaqControllerV1 {

    private final IFaqService faqService;

    public FaqControllerV1(IFaqService faqService) {
        this.faqService = faqService;
    }

    @Operation(summary = "Получить список всех FAQ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQ успешно получены"),
            @ApiResponse(responseCode = "204", description = "FAQ отсутствуют")
    })
    @GetMapping
    public ResponseEntity<List<FaqResponseDTO>> getAllFaqs() {
        List<FaqResponseDTO> faqs = faqService.getAllFaqs()
                .stream()
                .map(f -> new FaqResponseDTO(f.getId(), f.getQuestion(), f.getAnswer(), f.getUserId()))
                .collect(Collectors.toList());
        if (faqs.isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.ok(faqs);
    }

    @Operation(summary = "Получить FAQ по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQ найден"),
            @ApiResponse(responseCode = "404", description = "FAQ не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FaqResponseDTO> getFaqById(@PathVariable Long id) {
        return faqService.getFaqById(id)
                .map(f -> ResponseEntity.ok(new FaqResponseDTO(f.getId(), f.getQuestion(), f.getAnswer(), f.getUserId())))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Создать новый FAQ")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "FAQ создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    @PostMapping
    public ResponseEntity<FaqResponseDTO> createFaq(@RequestBody FaqRequestDTO dto) {
        var created = faqService.saveFaq(dto.toEntity());
        var response = new FaqResponseDTO(created.getId(), created.getQuestion(), created.getAnswer(), created.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Обновить существующий FAQ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQ обновлён"),
            @ApiResponse(responseCode = "404", description = "FAQ не найден")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<FaqResponseDTO> updateFaq(@PathVariable Long id, @RequestBody FaqUpdateDTO dto) {
        var updated = faqService.updateFaq(id, dto);
        var response = new FaqResponseDTO(updated.getId(), updated.getQuestion(), updated.getAnswer(), updated.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить FAQ по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "FAQ удалён"),
            @ApiResponse(responseCode = "404", description = "FAQ не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}
