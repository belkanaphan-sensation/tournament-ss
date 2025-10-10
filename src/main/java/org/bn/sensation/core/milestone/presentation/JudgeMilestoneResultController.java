package org.bn.sensation.core.milestone.presentation;

import java.util.List;

import org.bn.sensation.core.milestone.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.milestone.service.JudgeMilestoneResultService;
import org.bn.sensation.core.milestone.service.MilestoneService;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneDto;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultRoundRequest;
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
@RequestMapping("/api/v1/judge-milestone-result")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Judge milestone Result", description = "The Judge milestone result API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class JudgeMilestoneResultController {

    private final JudgeMilestoneResultService judgeMilestoneResultService;
    private final MilestoneService milestoneService;

    @Operation(summary = "Получить результат судьи по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<JudgeMilestoneResultDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return judgeMilestoneResultService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить все результаты судей")
    @GetMapping
    public ResponseEntity<Page<JudgeMilestoneResultDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(judgeMilestoneResultService.findAll(pageable));
    }

    @Operation(summary = "Получить результаты по ID раунда")
    @GetMapping(path = "/round/{roundId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getByRoundId(
            @Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByRoundId(roundId));
    }

    @Operation(summary = "Получить результаты по ID этапа")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getResultByMilestoneId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByMilestoneId(milestoneId));
    }

    @Operation(summary = "Получить результаты по ID участника")
    @GetMapping(path = "/participant/{participantId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getByParticipantId(
            @Parameter @PathVariable("participantId") @NotNull Long participantId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByParticipantId(participantId));
    }

    @Operation(summary = "Получить результаты по ID судьи")
    @GetMapping(path = "/judge/{activityUserId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getByActivityUserId(
            @Parameter @PathVariable("activityUserId") @NotNull Long activityUserId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByActivityUserId(activityUserId));
    }

    @Operation(summary = "Создать новые результаты судьи для раунда. Судья - текущий пользователь")
    @PostMapping(path = "/createOrUpdateForRound")
    public ResponseEntity<List<JudgeMilestoneResultDto>> createOrUpdateForRound(@Valid @RequestBody List<JudgeMilestoneResultRoundRequest> request) {
        return ResponseEntity.ok(judgeMilestoneResultService.createOrUpdateForRound(request));
    }

    @Operation(summary = "Обновить результаты судьи по этапу. Судья - текущий пользователь")
    @PostMapping(path = "/updateForMilestone/{milestoneId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> updateForMilestone(@Parameter @PathVariable("milestoneId") @NotNull Long milestoneId,
                                                                            @Valid @RequestBody List<JudgeMilestoneResultMilestoneRequest> request) {
        return ResponseEntity.ok(judgeMilestoneResultService.createOrUpdateForMilestone(milestoneId, request));
    }

    @Operation(summary = "Создать новый результат судьи")
    @PostMapping
    public ResponseEntity<JudgeMilestoneResultDto> create(@Valid @RequestBody JudgeMilestoneResultRoundRequest request) {
        JudgeMilestoneResultDto created = judgeMilestoneResultService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить результат судьи по ID")
    @PutMapping("/{id}")
    public ResponseEntity<JudgeMilestoneResultDto> update(@PathVariable("id") @NotNull Long id,
                                                          @Valid @RequestBody JudgeMilestoneResultRoundRequest request) {
        JudgeMilestoneResultDto updated = judgeMilestoneResultService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить результат судьи по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        judgeMilestoneResultService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Принять результаты этапа",
            description = "Результаты этапа принимаются для текущего пользователя который должен являться судьей этапа")
    @GetMapping(path = "/accept/{milestoneId}")
    public ResponseEntity<JudgeMilestoneDto> acceptRound(@Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(milestoneService.changeMilestoneStatus(milestoneId, JudgeMilestoneStatus.ACCEPTED));
    }

    @Operation(summary = "Отменить результаты раунда",
            description = "Результаты этапа отменяются для текущего пользователя который должен являться судьей этапа")
    @GetMapping(path = "/reject/{milestoneId}")
    public ResponseEntity<JudgeMilestoneDto> rejectRound(@Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(milestoneService.changeMilestoneStatus(milestoneId, JudgeMilestoneStatus.REJECTED));
    }
}
