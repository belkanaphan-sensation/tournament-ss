package org.bn.sensation.core.milestone.presentation;

import java.util.List;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.milestone.service.MilestoneService;
import org.bn.sensation.core.milestone.service.MilestoneStateMachineService;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone", description = "The Milestone API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class MilestoneController {

    private final MilestoneService milestoneService;
    private final MilestoneStateMachineService milestoneStateMachineService;

    @Operation(summary = "Получить этап по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Запланировать этап по ID",
            description = "Запланировать этап может администратор")
    @GetMapping(path = "/plan/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> planMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        milestoneStateMachineService.sendEvent(id, MilestoneEvent.PLAN);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Начать этап по ID",
            description = "Начать этап может администратор")
    @GetMapping(path = "/start/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> startMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        milestoneStateMachineService.sendEvent(id, MilestoneEvent.START);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Завершить этап по ID",
            description = "Завершить этап может администратор")
    @GetMapping(path = "/stop/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> completeMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        milestoneStateMachineService.sendEvent(id, MilestoneEvent.COMPLETE);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить этапы по ID активности")
    @GetMapping(path = "/activity/{id}")
    public ResponseEntity<List<MilestoneDto>> getByActivityId(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(milestoneService.findByActivityId(id));
    }

    @Operation(summary = "Получить этапы по ID активности в лайфстейтах")
    @GetMapping(path = "/activity/{id}/life")
    public ResponseEntity<List<MilestoneDto>> getByActivityIdInLifeStates(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(milestoneService.findByActivityIdInLifeStates(id));
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

    @Operation(summary = "Получить результаты этапа для участников")
    @GetMapping(path = "/{id}/result")
    public ResponseEntity<List<MilestoneResultDto>> getResultById(
            @Parameter @PathVariable("id") @NotNull Long id) {
        List<MilestoneResultDto> result = List.of(
                MilestoneResultDto.builder()
                        .participant(new EntityLinkDto(1L, "25"))
                        .milestone(new EntityLinkDto(1L, "milestone 1"))
                        .totalScore(71)
                        .build(),
                MilestoneResultDto.builder()
                        .participant(new EntityLinkDto(2L, "35"))
                        .milestone(new EntityLinkDto(1L, "milestone 1"))
                        .totalScore(22)
                        .build()
        );
        return ResponseEntity.ok(result);
    }
}
