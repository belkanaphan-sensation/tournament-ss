package org.bn.sensation.core.participant.presentation;

import java.util.List;

import org.bn.sensation.core.participant.service.ParticipantRoundResultService;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRoundResultRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantRoundResultDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRoundResultRequest;
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
@RequestMapping("/api/v1/participant-round-result")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Round Result", description = "The Round Result API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class ParticipantRoundResultController {

    private final ParticipantRoundResultService participantRoundResultService;

    @Operation(summary = "Получить результат раунда по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ParticipantRoundResultDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return participantRoundResultService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить все результаты раундов")
    @GetMapping
    public ResponseEntity<Page<ParticipantRoundResultDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(participantRoundResultService.findAll(pageable));
    }

    @Operation(summary = "Получить результаты раунда по ID раунда")
    @GetMapping(path = "/round/{roundId}")
    public ResponseEntity<List<ParticipantRoundResultDto>> getByRoundId(
            @Parameter @PathVariable("roundId") @NotNull Long roundId) {
        return ResponseEntity.ok(participantRoundResultService.findByRoundId(roundId));
    }

    @Operation(summary = "Получить результаты раунда по ID этапа")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<List<ParticipantRoundResultDto>> getResultByMilestoneId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(participantRoundResultService.findByMilestoneId(milestoneId));
    }

    @Operation(summary = "Получить результаты раунда по ID участника")
    @GetMapping(path = "/participant/{participantId}")
    public ResponseEntity<List<ParticipantRoundResultDto>> getByParticipantId(
            @Parameter @PathVariable("participantId") @NotNull Long participantId) {
        return ResponseEntity.ok(participantRoundResultService.findByParticipantId(participantId));
    }

    @Operation(summary = "Получить результаты раунда по ID судьи")
    @GetMapping(path = "/judge/{activityUserId}")
    public ResponseEntity<List<ParticipantRoundResultDto>> getByActivityUserId(
            @Parameter @PathVariable("activityUserId") @NotNull Long activityUserId) {
        return ResponseEntity.ok(participantRoundResultService.findByActivityUserId(activityUserId));
    }

    @Operation(summary = "Создать новый результат раунда")
    @PostMapping
    public ResponseEntity<ParticipantRoundResultDto> create(@Valid @RequestBody CreateParticipantRoundResultRequest request) {
        ParticipantRoundResultDto created = participantRoundResultService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить результат раунда по ID")
    @PutMapping("/{id}")
    public ResponseEntity<ParticipantRoundResultDto> update(@PathVariable("id") @NotNull Long id,
                                                            @Valid @RequestBody UpdateParticipantRoundResultRequest request) {
        ParticipantRoundResultDto updated = participantRoundResultService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить результат раунда по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        participantRoundResultService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
