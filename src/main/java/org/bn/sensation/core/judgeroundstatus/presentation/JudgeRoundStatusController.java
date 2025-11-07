package org.bn.sensation.core.judgeroundstatus.presentation;

import java.util.List;

import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.service.JudgeRoundStatusService;
import org.bn.sensation.core.judgeroundstatus.service.dto.JudgeRoundStatusDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/judge-round-status")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Judge Round Status", description = "The Judge Round Status API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class JudgeRoundStatusController {

    private final JudgeRoundStatusService judgeRoundStatusService;

    //Меняется при отправлении результатов
/*    @Operation(summary = "Принять результаты раунда",
            description = "Результаты раунда принимаются для текущего пользователя который должен являться судьей раунда")
    @GetMapping(path = "/ready/{roundId}")
    public ResponseEntity<JudgeRoundStatusDto> readyRound(@Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeRoundStatusService.changeJudgeRoundStatus(roundId, JudgeRoundStatus.READY));
    }*/

    @Operation(summary = "Отменить результаты раунда",
            description = "Результаты раунда отменяются для текущего пользователя который должен являться судьей раунда")
    @PostMapping(path = "/not-ready")
    public ResponseEntity<JudgeRoundStatusDto> notReadyRound(@RequestParam @NotNull Long roundId) {
        return ResponseEntity.ok(judgeRoundStatusService.markNotReady(roundId));
    }

    @Operation(summary = "Получить статус раунда текущего пользователя",
            description = "Получить статус раунда текущего пользователя")
    @GetMapping(path = "/round/{roundId}/currentUser")
    public ResponseEntity<JudgeRoundStatus> getRoundStatus(@Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeRoundStatusService.getRoundStatusForCurrentUser(roundId));
    }

    @Operation(summary = "Получить статусы раунда по судьям",
            description = "Получить статусы раунда по судьям")
    @GetMapping(path = "/round/{roundId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN')")
    public ResponseEntity<List<JudgeRoundStatusDto>> getByRoundId(@Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeRoundStatusService.getByRoundId(roundId));
    }

    @Operation(summary = "Получить статусы всех раундов текущего пользователя по id этапа",
            description = "Получить статусы всех раундов текущего пользователя по id этапа")
    @GetMapping(path = "/milestone/{milestoneId}/currentUser")
    public ResponseEntity<List<JudgeRoundStatusDto>> getRoundStatusByMilestoneId(@Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(judgeRoundStatusService.getByMilestoneIdForCurrentUser(milestoneId));
    }
}
