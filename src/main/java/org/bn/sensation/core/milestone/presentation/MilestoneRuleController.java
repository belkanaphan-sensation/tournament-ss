package org.bn.sensation.core.milestone.presentation;

import org.bn.sensation.core.milestone.service.MilestoneRuleService;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRuleRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneRuleDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRuleRequest;
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
@RequestMapping("/api/v1/milestone-rule")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone Rule", description = "The Milestone Rule API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ANNOUNCER')")
public class MilestoneRuleController {

    private final MilestoneRuleService milestoneRuleService;

    @Operation(summary = "Получить правило этапа по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MilestoneRuleDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneRuleService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Получить правило этапа по ID этапа")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<MilestoneRuleDto> getByMilestoneId(@Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        MilestoneRuleDto rule = milestoneRuleService.findByMilestoneId(milestoneId);
        return ResponseEntity.ok(rule);
    }

    @Operation(summary = "Получить правило следующего этапа по ID текущего этапа")
    @GetMapping(path = "/next/milestone/{milestoneId}")
    public ResponseEntity<MilestoneRuleDto> getForNextMilestoneByMilestoneId(@Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        MilestoneRuleDto rule = milestoneRuleService.findForNextMilestoneByMilestoneId(milestoneId);
        return ResponseEntity.ok(rule);
    }

    @Operation(summary = "Получить все правила этапов")
    @GetMapping
    public ResponseEntity<Page<MilestoneRuleDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(milestoneRuleService.findAll(pageable));
    }

    @Operation(summary = "Создать новое правило этапа")
    @PostMapping
    public ResponseEntity<MilestoneRuleDto> create(@Valid @RequestBody CreateMilestoneRuleRequest request) {
        MilestoneRuleDto created = milestoneRuleService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить правило этапа по ID")
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneRuleDto> update(@PathVariable("id") @NotNull Long id,
                                                  @Valid @RequestBody UpdateMilestoneRuleRequest request) {
        MilestoneRuleDto updated = milestoneRuleService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить правило этапа по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneRuleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
