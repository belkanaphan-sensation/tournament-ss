package org.bn.sensation.core.participant.presentation;

import java.util.List;

import org.bn.sensation.core.participant.service.ParticipantService;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.RoundParticipantsDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
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
@RequestMapping("/api/v1/participant")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Participant", description = "The Participant API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class ParticipantController {

    private final ParticipantService participantService;

    @Operation(summary = "Получить участника по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ParticipantDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return participantService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить всех участников")
    @GetMapping
    public ResponseEntity<Page<ParticipantDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(participantService.findAll(pageable));
    }

    @Operation(summary = "Получить всех участников по ID раунда")
    @GetMapping(path = "/round/{roundId}")
    public ResponseEntity<List<ParticipantDto>> getByRoundId(@PathVariable("roundId") Long roundId) {
        return ResponseEntity.ok(participantService.findByRoundId(roundId));
    }

    @Operation(summary = "Получить участников, расформированных по раундам по ID раунда")
    @GetMapping(path = "/by-round/round/{roundId}/currentUser")
    public ResponseEntity<List<ParticipantDto>> getByRoundByRoundIdForCurrentUser(@PathVariable("roundId") Long roundId) {
        return ResponseEntity.ok(participantService.getByRoundByRoundIdForCurrentUser(roundId));
    }

    @Operation(summary = "Получить всех участников, расформированных по раундам по ID этапа")
    @GetMapping(path = "/by-round/milestone/{milestoneId}/currentUser")
    public ResponseEntity<List<RoundParticipantsDto>> getByRoundByMilestoneIdForCurrentUser(@PathVariable("milestoneId") Long milestoneId) {
        return ResponseEntity.ok(participantService.getByRoundByMilestoneIdForCurrentUser(milestoneId));
    }

    @Operation(summary = "Создать нового участника")
    @PostMapping
    public ResponseEntity<ParticipantDto> create(@Valid @RequestBody CreateParticipantRequest request) {
        ParticipantDto created = participantService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить участника по ID")
    @PutMapping("/{id}")
    public ResponseEntity<ParticipantDto> update(@PathVariable("id") @NotNull Long id,
                                                 @Valid @RequestBody UpdateParticipantRequest request) {
        ParticipantDto updated = participantService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить участника по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        participantService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Привязать участника к раунду")
    @PostMapping("/{participantId}/rounds/{roundId}")
    public ResponseEntity<ParticipantDto> addParticipantToRound(@PathVariable Long participantId,
                                                                @PathVariable Long roundId) {
        ParticipantDto updated = participantService.assignParticipantToRound(participantId, roundId);
        return ResponseEntity.ok(updated);
    }
}
