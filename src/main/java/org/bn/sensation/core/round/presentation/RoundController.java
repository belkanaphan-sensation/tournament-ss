package org.bn.sensation.core.round.presentation;

import java.util.List;

import org.bn.sensation.core.round.service.RoundService;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;
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
@RequestMapping("/api/v1/round")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Round", description = "The Round API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
public class RoundController {

    private final RoundService roundService;

    @Operation(summary = "Получить раунд по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<RoundDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return roundService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Удалить раунд по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        roundService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить раунды по ID этапа")
    @GetMapping(path = "/milestone/{id}")
    public ResponseEntity<List<RoundDto>> getByMilestoneId(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(roundService.findByMilestoneId(id));
    }

    @Operation(summary = "Получить раунды по ID этапа в лайфстейтах со статусом раунда для текущего пользователя")
    @GetMapping(path = "/milestone/{id}/life")
    public ResponseEntity<List<RoundWithJRStatusDto>> getByMilestoneIdInLifeStates(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(roundService.findByMilestoneIdInLifeStates(id));
    }

}
