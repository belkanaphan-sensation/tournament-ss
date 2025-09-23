package org.bn.sensation.core.milestone.presentation;

import org.bn.sensation.core.milestone.service.MilestoneService;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
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
@RequestMapping("/api/v1/milestone")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone", description = "The Milestone API")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @Operation(summary = "Получить этап по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить этапы по ID активности")
    @GetMapping(path = "/activity/{id}")
    public ResponseEntity<Page<MilestoneDto>> getByActivityId(@Parameter @PathVariable("id") @NotNull Long id, Pageable pageable) {
        return ResponseEntity.ok(milestoneService.findByActivityId(id, pageable));
    }

    @Operation(summary = "Получить этапы по ID активности в лайфстейтах")
    @GetMapping(path = "/activity/{id}/life")
    public ResponseEntity<Page<MilestoneDto>> getByActivityIdInLifeStates(@Parameter @PathVariable("id") @NotNull Long id, Pageable pageable) {
        return ResponseEntity.ok(milestoneService.findByActivityIdInLifeStates(id, pageable));
    }

    @Operation(summary = "Получить все этапы с пагинацией")
    @GetMapping
    public ResponseEntity<Page<MilestoneDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(milestoneService.findAll(pageable));
    }

    @Operation(summary = "Создать новую этап")
    @PostMapping
    public ResponseEntity<MilestoneDto> create(@Valid @RequestBody CreateMilestoneRequest request) {
        MilestoneDto created = milestoneService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить этап по ID")
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneDto> update(@PathVariable("id") @NotNull Long id,
                                             @Valid @RequestBody UpdateMilestoneRequest request) {
        MilestoneDto updated = milestoneService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить этап по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

