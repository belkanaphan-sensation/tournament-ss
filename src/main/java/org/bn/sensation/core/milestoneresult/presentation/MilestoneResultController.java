package org.bn.sensation.core.milestoneresult.presentation;

import java.util.List;
import java.util.Map;

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
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
public class MilestoneResultController {

    private final MilestoneResultService milestoneResultService;

    @Operation(summary = "Обновить результаты этапа")
    @PostMapping("/update/milestone/{milestoneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<List<MilestoneResultDto>> updateForMilestone(@PathVariable("milestoneId") @NotNull Long milestoneId,
                                                                       @Valid @RequestBody List<UpdateMilestoneResultRequest> request) {
        List<MilestoneResultDto> dtos = milestoneResultService.acceptResults(milestoneId, request);
        return ResponseEntity.ok(dtos);
    }

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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<MilestoneResultDto> create(@Valid @RequestBody CreateMilestoneResultRequest request) {
        MilestoneResultDto created = milestoneResultService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить результат этапа по ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<MilestoneResultDto> update(@PathVariable("id") @NotNull Long id,
                                                    @Valid @RequestBody UpdateMilestoneResultRequest request) {
        MilestoneResultDto updated = milestoneResultService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить результат этапа по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneResultService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить результаты этапов по ID этапа")
    @GetMapping("/{milestoneId}")
    public ResponseEntity<List<MilestoneResultDto>> getByMilestoneId(@PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(milestoneResultService.getByMilestoneId(milestoneId));
    }

    @Operation(summary = "Получить результаты этапов по ID активности в порядке этапов от финала к отборочным")
    @GetMapping("/{activityId}")
    public ResponseEntity<Map<Integer, List<MilestoneResultDto>>> getByActivityId(@PathVariable("activityId") @NotNull Long activityId) {
        return ResponseEntity.ok(milestoneResultService.getByActivityId(activityId));
    }
}
