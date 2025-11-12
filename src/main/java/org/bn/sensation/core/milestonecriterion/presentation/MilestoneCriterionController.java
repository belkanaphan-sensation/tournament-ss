package org.bn.sensation.core.milestonecriterion.presentation;

import java.util.List;

import org.bn.sensation.core.milestonecriterion.service.MilestoneCriterionService;
import org.bn.sensation.core.milestonecriterion.service.dto.CreateMilestoneCriterionRequest;
import org.bn.sensation.core.milestonecriterion.service.dto.MilestoneCriterionDto;
import org.bn.sensation.core.milestonecriterion.service.dto.UpdateMilestoneCriterionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/milestone-criterion")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone Criterion Assignment", description = "The Milestone Criterion Assignment API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ANNOUNCER')")
public class MilestoneCriterionController {

    private final MilestoneCriterionService milestoneCriterionService;

    @Operation(summary = "Получить назначение по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MilestoneCriterionDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneCriterionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Получить назначение по ID правила этапа и критерия")
    @GetMapping(path = "/milestone-rule/{milestoneRuleId}/criterion/{criterionId}")
    public ResponseEntity<MilestoneCriterionDto> getByMilestoneRuleIdAndCriterionId(
            @Parameter @PathVariable("milestoneRuleId") @NotNull Long milestoneRuleId,
            @Parameter @PathVariable("criterionId") @NotNull Long criterionId) {
        MilestoneCriterionDto assignment = milestoneCriterionService.findByMilestoneRuleIdAndCriterionId(milestoneRuleId, criterionId);
        return ResponseEntity.ok(assignment);
    }

    @Operation(summary = "Получить критерии для правила этапа для текущего юзера")
    @GetMapping(path = "/milestone-rule/{milestoneRuleId}/currentUser")
    public ResponseEntity<List<MilestoneCriterionDto>> getByMilestoneRuleIdForCurrentUser(
            @Parameter @PathVariable("milestoneRuleId") @NotNull Long milestoneRuleId) {
        return ResponseEntity.ok(milestoneCriterionService.findByMilestoneRuleIdForCurrentUser(milestoneRuleId));
    }

    @Operation(summary = "Получить критерии для этапа для текущего юзера")
    @GetMapping(path = "/milestone/{milestoneId}/currentUser")
    public ResponseEntity<List<MilestoneCriterionDto>> getByMilestoneIdForCurrentUser(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(milestoneCriterionService.findByMilestoneIdForCurrentUser(milestoneId));
    }

    @Operation(summary = "Получить назначение по ID этапа и критерия")
    @GetMapping(path = "/milestone/{milestoneId}/criterion/{criterionId}")
    public ResponseEntity<MilestoneCriterionDto> getByMilestoneIdAndCriterionId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId,
            @Parameter @PathVariable("criterionId") @NotNull Long criterionId) {
        MilestoneCriterionDto assignment = milestoneCriterionService.findByMilestoneIdAndCriterionId(milestoneId, criterionId);
        return ResponseEntity.ok(assignment);
    }

    @Operation(summary = "Получить все назначения")
    @GetMapping
    public ResponseEntity<Page<MilestoneCriterionDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(milestoneCriterionService.findAll(pageable));
    }

    @Operation(summary = "Получить назначения этапа")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<List<MilestoneCriterionDto>> getByMilestoneId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(milestoneCriterionService.findByMilestoneId(milestoneId));
    }

    @Operation(summary = "Получить назначения правила этапа")
    @GetMapping(path = "/milestone-rule/{milestoneRuleId}")
    public ResponseEntity<List<MilestoneCriterionDto>> getByMilestoneRuleId(
            @Parameter @PathVariable("milestoneRuleId") @NotNull Long milestoneRuleId) {
        return ResponseEntity.ok(milestoneCriterionService.findByMilestoneRuleId(milestoneRuleId));
    }

    @Operation(summary = "Получить назначения критерия")
    @GetMapping(path = "/criterion/{criterionId}")
    public ResponseEntity<List<MilestoneCriterionDto>> getByCriterionId(
            @Parameter @PathVariable("criterionId") @NotNull Long criterionId) {
        return ResponseEntity.ok(milestoneCriterionService.findByCriterionId(criterionId));
    }


    @Operation(summary = "Создать новое назначение")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<MilestoneCriterionDto> create(@Valid @RequestBody CreateMilestoneCriterionRequest request) {
        MilestoneCriterionDto created = milestoneCriterionService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить назначение по ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<MilestoneCriterionDto> update(@PathVariable("id") @NotNull Long id,
                                                        @Valid @RequestBody UpdateMilestoneCriterionRequest request) {
        MilestoneCriterionDto updated = milestoneCriterionService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить назначение по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneCriterionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
