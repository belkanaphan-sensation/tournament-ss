package org.bn.sensation.core.round.presentation;

import org.bn.sensation.core.round.service.RoundService;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/round")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Round", description = "The Round API")
public class RoundController {

    private final RoundService roundService;

    @Operation(summary = "Получить раунд по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return roundService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить раунды по ID этапа")
    @GetMapping(path = "/milestone/{id}")
    public ResponseEntity<Page<RoundDto>> getByMilestoneId(@Parameter @PathVariable("id") @NotNull Long id, Pageable pageable) {
        return ResponseEntity.ok(roundService.findByMilestoneId(id, pageable));
    }

    @Operation(summary = "Получить все раунды с пагинацией")
    @GetMapping
    public ResponseEntity<Page<RoundDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(roundService.findAll(pageable));
    }

    @Operation(summary = "Создать новый раунд")
    @PostMapping
    public ResponseEntity<RoundDto> create(@Valid @RequestBody CreateRoundRequest request) {
        RoundDto created = roundService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить раунд по ID")
    @PutMapping("/{id}")
    public ResponseEntity<RoundDto> update(@PathVariable("id") @NotNull Long id,
                                         @Valid @RequestBody UpdateRoundRequest request) {
        RoundDto updated = roundService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить раунд по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        roundService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
