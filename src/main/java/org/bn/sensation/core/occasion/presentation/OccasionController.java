package org.bn.sensation.core.occasion.presentation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.bn.sensation.core.occasion.service.OccasionService;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/occasion")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Occasion", description = "The Occasion API")
public class OccasionController {

    private final OccasionService occasionService;

    @Operation(summary = "Получить мероприятие по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return occasionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved all occasions",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PagedModel.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            })
    @Operation(summary = "Получить все мероприятия с пагинацией")
    @GetMapping
    public ResponseEntity<Page<OccasionDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(occasionService.findAll(pageable));
    }

    @Operation(summary = "Создать новое мероприятие")
    @PostMapping
    public ResponseEntity<OccasionDto> create(@Valid @RequestBody CreateOccasionRequest request) {
        OccasionDto created = occasionService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить мероприятие по ID")
    @PutMapping("/{id}")
    public ResponseEntity<OccasionDto> update(@PathVariable("id") @NotNull Long id,
                                            @Valid @RequestBody UpdateOccasionRequest request) {
        OccasionDto updated = occasionService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить мероприятие по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        occasionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
