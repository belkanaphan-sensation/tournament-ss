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

    @Operation(summary = "Get milestone by id")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Get all milestones with pagination")
    @GetMapping
    public ResponseEntity<Page<MilestoneDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(milestoneService.findAll(pageable));
    }

    @Operation(summary = "Create new milestone")
    @PostMapping
    public ResponseEntity<MilestoneDto> create(@Valid @RequestBody CreateMilestoneRequest request) {
        MilestoneDto created = milestoneService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Update milestone by id")
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneDto> update(@PathVariable("id") @NotNull Long id,
                                             @Valid @RequestBody UpdateMilestoneRequest request) {
        MilestoneDto updated = milestoneService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete milestone by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
