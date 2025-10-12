package org.bn.sensation.core.judge.presentation;

import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.bn.sensation.core.judge.service.JudgeRoundService;
import org.bn.sensation.core.judge.service.dto.JudgeRoundDto;
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
@RequestMapping("/api/v1/judge-round")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Judge Round Status", description = "The Judge Round Status API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class JudgeRoundController {

    private final JudgeRoundService judgeRoundService;

    @Operation(summary = "Принять результаты раунда",
            description = "Результаты раунда принимаются для текущего пользователя который должен являться судьей раунда")
    @GetMapping(path = "/ready/{roundId}")
    public ResponseEntity<JudgeRoundDto> readyRound(@Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeRoundService.changeJudgeRoundStatus(roundId, JudgeRoundStatus.READY));
    }

    @Operation(summary = "Отменить результаты раунда",
            description = "Результаты раунда отменяются для текущего пользователя который должен являться судьей раунда")
    @GetMapping(path = "/not-ready/{roundId}")
    public ResponseEntity<JudgeRoundDto> notReadyRound(@Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(judgeRoundService.changeJudgeRoundStatus(roundId, JudgeRoundStatus.NOT_READY));
    }
}
