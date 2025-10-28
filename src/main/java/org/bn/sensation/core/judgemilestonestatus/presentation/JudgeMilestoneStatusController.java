package org.bn.sensation.core.judgemilestonestatus.presentation;

import java.util.List;

import org.bn.sensation.core.judgemilestonestatus.dto.JudgeMilestoneStatusDto;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/judge-milestone-status")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Judge Milestone Status", description = "The Judge Milestone Status API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN')")
public class JudgeMilestoneStatusController {

    private final JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;

    @Operation(summary = "Получить статусы всех судей для этапа",
            description = "Возвращает список статусов всех судей для указанного этапа. " +
                    "Данные берутся из кэша (обновляется каждые 10 секунд).")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<List<JudgeMilestoneStatusDto>> getJudgesStatusForMilestone(
            @Parameter(description = "ID этапа") @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(milestoneId));
    }
}
