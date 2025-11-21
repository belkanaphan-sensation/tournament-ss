package org.bn.sensation.core.contestant.presentation;


import java.util.List;

import org.bn.sensation.core.contestant.service.ContestantService;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
import org.bn.sensation.core.contestant.service.dto.CreateContestantRequest;
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
@RequestMapping("/api/v1/contestant")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Contestant", description = "The Contestant API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
public class ContestantController {

    private final ContestantService contestantService;
    @Operation(summary = "Получить участника по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ContestantDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return contestantService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Создать нового участника")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<ContestantDto> create(@Valid @RequestBody CreateContestantRequest request) {
        ContestantDto created = contestantService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Удалить участника по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        contestantService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить всех участников по ID раунда")
    @GetMapping(path = "/round/{roundId}")
    public ResponseEntity<List<ContestantDto>> getByRoundId(@PathVariable("roundId") Long roundId) {
        return ResponseEntity.ok(contestantService.findByRoundId(roundId));
    }

    @Operation(summary = "Получить участников, расформированных по раундам по ID раунда для текущего пользователя")
    @GetMapping(path = "/by-round/round/{roundId}/currentUser")
    public ResponseEntity<List<ContestantDto>> getByRoundByRoundIdForCurrentUser(@PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(contestantService.getByRoundByRoundIdForCurrentUser(roundId));
    }
}
