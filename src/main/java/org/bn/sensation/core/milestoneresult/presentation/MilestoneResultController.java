package org.bn.sensation.core.milestoneresult.presentation;

import org.bn.sensation.core.milestoneresult.service.MilestoneResultService;
import org.bn.sensation.core.milestoneresult.service.dto.CreateMilestoneResultRequest;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;
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
@RequestMapping("/api/v1/milestone-result")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone Result", description = "The Milestone Result API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN')")
public class MilestoneResultController {

    private final MilestoneResultService milestoneResultService;

    @Operation(summary = "Получить результат этапа по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MilestoneResultDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneResultService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить все результаты этапов")
    @GetMapping
    public ResponseEntity<Page<MilestoneResultDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(milestoneResultService.findAll(pageable));
    }

    @Operation(summary = "Создать новый результат этапа")
    @PostMapping
    public ResponseEntity<MilestoneResultDto> create(@Valid @RequestBody CreateMilestoneResultRequest request) {
        MilestoneResultDto created = milestoneResultService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить результат этапа по ID")
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneResultDto> update(@PathVariable("id") @NotNull Long id,
                                                    @Valid @RequestBody UpdateMilestoneResultRequest request) {
        MilestoneResultDto updated = milestoneResultService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить результат этапа по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneResultService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
