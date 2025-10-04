package org.bn.sensation.core.round.presentation;

import org.springframework.http.ResponseEntity;
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
public class JudgeRoundController {

    @Operation(summary = "Принять результаты раунда",
            description = "Результаты раунда принимаются для текущего пользователя который должен являться судьей раунда")
    @GetMapping(path = "/accept/{roundId}")
    public ResponseEntity<Void> acceptRound(@Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Отменить результаты раунда",
            description = "Результаты раунда отменяются для текущего пользователя который должен являться судьей раунда")
    @GetMapping(path = "/reject/{roundId}")
    public ResponseEntity<Void> rejectRound(@Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.noContent().build();
    }
}
