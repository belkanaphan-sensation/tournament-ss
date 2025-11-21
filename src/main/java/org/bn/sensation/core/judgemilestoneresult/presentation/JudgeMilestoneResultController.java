package org.bn.sensation.core.judgemilestoneresult.presentation;

import java.util.List;

import org.bn.sensation.core.judgemilestoneresult.service.JudgeMilestoneResultService;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultRoundRequest;
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
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ANNOUNCER')")
public class JudgeMilestoneResultController {

    private final JudgeMilestoneResultService judgeMilestoneResultService;

    @Operation(summary = "Получить результат судьи по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<JudgeMilestoneResultDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return judgeMilestoneResultService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить результаты по ID раунда")
    @GetMapping(path = "/round/{roundId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getByRoundId(
            @Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByRoundId(roundId));
    }

    @Operation(summary = "Получить результаты по ID раунда для текущего пользователя")
    @GetMapping(path = "/round/{roundId}/currentUser")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getResultByRoundIdCurrentUser(
            @Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByRoundIdForCurrentUser(roundId));
    }

    @Operation(summary = "Получить результаты по ID этапа")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getResultByMilestoneId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByMilestoneId(milestoneId));
    }

    @Operation(summary = "Получить результаты по ID этапа для текущего пользователя")
    @GetMapping(path = "/milestone/{milestoneId}/currentUser")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getResultByMilestoneIdCurrentUser(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByMilestoneIdForCurrentUser(milestoneId));
    }

    @Operation(summary = "Получить результаты по ID участника")
    @GetMapping(path = "/contestant/{contestantId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getByContestantId(
            @Parameter @PathVariable("contestantId") @NotNull Long contestantId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByContestantId(contestantId));
    }

    @Operation(summary = "Получить результаты по ID судьи")
    @GetMapping(path = "/judge/{activityUserId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> getByActivityUserId(
            @Parameter @PathVariable("activityUserId") @NotNull Long activityUserId) {
        return ResponseEntity.ok(judgeMilestoneResultService.findByActivityUserId(activityUserId));
    }

    @Operation(summary = "Создать новые результаты судьи для раунда. Судья - текущий пользователь",
            description = "Запрос на апдейт полностью перезаписывает предыдущие результаты, поэтому нужно передавать все значащие поля")
    @PostMapping(path = "/createOrUpdateForRound/{roundId}")
    public ResponseEntity<List<JudgeMilestoneResultDto>> createOrUpdateForRound(@Parameter @PathVariable("roundId") @NotNull Long roundId,
                                                                                @Valid @NotNull @RequestBody List<JudgeMilestoneResultRoundRequest> request) {
        return ResponseEntity.ok(judgeMilestoneResultService.createOrUpdateForRound(roundId, request));
    }

}
