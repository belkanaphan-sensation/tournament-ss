package org.bn.sensation.core.criteria.presentation;

import java.util.List;

import org.bn.sensation.core.criteria.service.MilestoneCriteriaAssignmentService;
import org.bn.sensation.core.criteria.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.criteria.service.dto.MilestoneCriteriaAssignmentDto;
import org.bn.sensation.core.criteria.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/milestone-criteria-assignment")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone Criteria Assignment", description = "The Milestone Criteria Assignment API")
public class MilestoneCriteriaAssignmentController {

    private final MilestoneCriteriaAssignmentService milestoneCriteriaAssignmentService;

    @Operation(summary = "Получить назначение по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MilestoneCriteriaAssignmentDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneCriteriaAssignmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Получить назначение по ID этапа и критерия")
    @GetMapping(path = "/milestone/{milestoneId}/criteria/{criteriaId}")
    public ResponseEntity<MilestoneCriteriaAssignmentDto> getByMilestoneIdAndCriteriaId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId,
            @Parameter @PathVariable("criteriaId") @NotNull Long criteriaId) {
        MilestoneCriteriaAssignmentDto assignment = milestoneCriteriaAssignmentService.findByMilestoneIdAndCriteriaId(milestoneId, criteriaId);
        return ResponseEntity.ok(assignment);
    }

    @Operation(summary = "Получить критерии для этапа для текущего юзера")
    @GetMapping(path = "/milestone/{milestoneId}/currentUser")
    public ResponseEntity<List<MilestoneCriteriaAssignmentDto>> getByMilestoneIdForCurrentUser(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(milestoneId));
    }

    @Operation(summary = "Получить все назначения")
    @GetMapping
    public ResponseEntity<Page<MilestoneCriteriaAssignmentDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(milestoneCriteriaAssignmentService.findAll(pageable));
    }

    @Operation(summary = "Получить назначения этапа")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<Page<MilestoneCriteriaAssignmentDto>> getByMilestoneId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId, Pageable pageable) {
        return ResponseEntity.ok(milestoneCriteriaAssignmentService.findByMilestoneId(milestoneId, pageable));
    }

    @Operation(summary = "Получить назначения критерия")
    @GetMapping(path = "/criteria/{criteriaId}")
    public ResponseEntity<Page<MilestoneCriteriaAssignmentDto>> getByCriteriaId(
            @Parameter @PathVariable("criteriaId") @NotNull Long criteriaId, Pageable pageable) {
        return ResponseEntity.ok(milestoneCriteriaAssignmentService.findByCriteriaId(criteriaId, pageable));
    }


    @Operation(summary = "Создать новое назначение")
    @PostMapping
    public ResponseEntity<MilestoneCriteriaAssignmentDto> create(@Valid @RequestBody CreateMilestoneCriteriaAssignmentRequest request) {
        MilestoneCriteriaAssignmentDto created = milestoneCriteriaAssignmentService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить назначение по ID")
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneCriteriaAssignmentDto> update(@PathVariable("id") @NotNull Long id,
                                                          @Valid @RequestBody UpdateMilestoneCriteriaAssignmentRequest request) {
        MilestoneCriteriaAssignmentDto updated = milestoneCriteriaAssignmentService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить назначение по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneCriteriaAssignmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
