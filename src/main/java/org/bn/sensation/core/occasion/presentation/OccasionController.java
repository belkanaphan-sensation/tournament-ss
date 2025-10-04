package org.bn.sensation.core.occasion.presentation;

import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.occasion.service.OccasionService;
import org.bn.sensation.core.occasion.service.OccasionStateMachineService;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/occasion")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Occasion", description = "The Occasion API")
public class OccasionController {

    private final OccasionService occasionService;
    private final OccasionStateMachineService occasionStateMachineService;

    @Operation(summary = "Получить мероприятие по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<OccasionDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return occasionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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

    @Operation(summary = "Запланировать мероприятие по ID",
            description = "Запланировать мероприятие может администратор")
    @GetMapping(path = "/plan/{id}")
    public ResponseEntity<Void> planOccasion(@Parameter @PathVariable("id") @NotNull Long id) {
        occasionStateMachineService.sendEvent(id, OccasionEvent.PLAN);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Начать мероприятие по ID",
            description = "Начать мероприятие может или администратор или судья данного мероприятия")
    @GetMapping(path = "/start/{id}")
    public ResponseEntity<Void> startOccasion(@Parameter @PathVariable("id") @NotNull Long id) {
        occasionStateMachineService.sendEvent(id, OccasionEvent.START);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Завершить мероприятие по ID",
            description = "Завершить мероприятие может или администратор или судья данного мероприятия")
    @GetMapping(path = "/stop/{id}")
    public ResponseEntity<Void> completeOccasion(@Parameter @PathVariable("id") @NotNull Long id) {
        occasionStateMachineService.sendEvent(id, OccasionEvent.COMPLETE);
        return ResponseEntity.noContent().build();
    }
}
