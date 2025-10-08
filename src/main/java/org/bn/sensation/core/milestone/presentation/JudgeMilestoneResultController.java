package org.bn.sensation.core.milestone.presentation;

import java.util.Comparator;
import java.util.List;

import org.bn.sensation.core.milestone.service.JudgeMilestoneResultService;
import org.bn.sensation.core.milestone.service.dto.CreateJudgeMilestoneResultRequest;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.UpdateJudgeMilestoneResultRequest;
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
@Tag(name = "Judge milestone Result", description = "The Judge milestone API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class JudgeMilestoneResultController {

    private final JudgeMilestoneResultService judgeMilestoneResultService;

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

    @Operation(summary = "Создать новый результат судьи")
    @PostMapping
    public ResponseEntity<JudgeMilestoneResultDto> create(@Valid @RequestBody CreateJudgeMilestoneResultRequest request) {
        JudgeMilestoneResultDto created = judgeMilestoneResultService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Создать новые результаты судьи")
    @PostMapping(path = "/createBulk")
    public ResponseEntity<List<JudgeMilestoneResultDto>> createBulk(@Valid @RequestBody List<CreateJudgeMilestoneResultRequest> request) {
        List<JudgeMilestoneResultDto> created = request.stream()
                .map(req -> judgeMilestoneResultService.create(req))
                .sorted(Comparator.comparing(JudgeMilestoneResultDto::getScore).reversed())
                .toList();
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить результат судьи по ID")
    @PutMapping("/{id}")
    public ResponseEntity<JudgeMilestoneResultDto> update(@PathVariable("id") @NotNull Long id,
                                                          @Valid @RequestBody UpdateJudgeMilestoneResultRequest request) {
        JudgeMilestoneResultDto updated = judgeMilestoneResultService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Обновить результаты судьи по ID")
    @PostMapping(path = "/updateBulk")
    public ResponseEntity<List<JudgeMilestoneResultDto>> updateBulk(@Valid @RequestBody List<UpdateJudgeMilestoneResultRequest> request) {
        List<JudgeMilestoneResultDto> updated = request.stream()
                .map(req -> judgeMilestoneResultService.update(req.getId(), req))
                .sorted(Comparator.comparing(JudgeMilestoneResultDto::getScore).reversed())
                .toList();
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить результат судьи по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        judgeMilestoneResultService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
