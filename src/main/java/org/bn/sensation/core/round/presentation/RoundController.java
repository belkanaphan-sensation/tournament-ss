package org.bn.sensation.core.round.presentation;

import java.util.List;

import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.round.service.RoundService;
import org.bn.sensation.core.round.service.RoundStateMachineService;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
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
@RequestMapping("/api/v1/round")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Round", description = "The Round API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class RoundController {

    private final RoundService roundService;
    private final RoundStateMachineService roundStateMachineService;

    @Operation(summary = "Сменить стейт раунда по ID и ивенту",
            description = "Сменить стейт раунда может администратор")
    @GetMapping(path = "/{id}/change-state/{event}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> changeRoundState(@Parameter @PathVariable("id") @NotNull Long id,
                                                 @Parameter @PathVariable("event") @NotNull RoundEvent event) {
        roundStateMachineService.sendEvent(id, event);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Запланировать раунд по ID",
            description = "Запланировать раунд может администратор")
    @GetMapping(path = "/plan/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> planRound(@Parameter @PathVariable("id") @NotNull Long id) {
        roundStateMachineService.sendEvent(id, RoundEvent.PLAN);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Начать раунд по ID",
            description = "Начать раунд может администратор")
    @GetMapping(path = "/start/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> startRound(@Parameter @PathVariable("id") @NotNull Long id) {
        roundStateMachineService.sendEvent(id, RoundEvent.START);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Завершить раунд по ID",
            description = "Завершить раунд может администратор")
    @GetMapping(path = "/stop/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> completeRound(@Parameter @PathVariable("id") @NotNull Long id) {
        roundStateMachineService.sendEvent(id, RoundEvent.COMPLETE);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить раунд по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<RoundDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return roundService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
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

    @Operation(summary = "Получить все раунды с пагинацией")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<RoundDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(roundService.findAll(pageable));
    }

    @Operation(summary = "Создать новый раунд")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<RoundDto> create(@Valid @RequestBody CreateRoundRequest request) {
        RoundDto created = roundService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить раунд по ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<RoundDto> update(@PathVariable("id") @NotNull Long id,
                                           @Valid @RequestBody UpdateRoundRequest request) {
        RoundDto updated = roundService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить раунд по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        roundService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
