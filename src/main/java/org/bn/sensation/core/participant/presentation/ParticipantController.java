package org.bn.sensation.core.participant.presentation;

import java.util.List;

import org.bn.sensation.core.participant.service.ParticipantService;
import org.bn.sensation.core.participant.service.dto.*;
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
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
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

    @Operation(summary = "Создать нового участника")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<ParticipantDto> create(@Valid @RequestBody CreateParticipantRequest request) {
        ParticipantDto created = participantService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить участника по ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<ParticipantDto> update(@PathVariable("id") @NotNull Long id,
                                                 @Valid @RequestBody UpdateParticipantRequest request) {
        ParticipantDto updated = participantService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить участника по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        participantService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить всех участников по ID активности")
    @GetMapping(path = "/activity/{activityId}")
    public ResponseEntity<List<ParticipantDto>> getByActivityId(@PathVariable("activityId") Long activityId) {
        return ResponseEntity.ok(participantService.findByActivityId(activityId));
    }

//    @Operation(summary = "Получить всех участников по ID раунда")
//    @GetMapping(path = "/round/{roundId}")
//    public ResponseEntity<List<ParticipantDto>> getByRoundId(@PathVariable("roundId") Long roundId) {
//        return ResponseEntity.ok(participantService.findByRoundId(roundId));

//    }

    @Operation(summary = "Зарегистрировать участника")
    @PostMapping("/{participantId}/register/{number}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<ParticipantDto> registerParticipant(@PathVariable @NotNull Long participantId,
                                                                    @PathVariable @NotNull String number) {
        UpdateParticipantRequest request = UpdateParticipantRequest.builder()
                .number(number)
                .isRegistered(true)
                .build();
        ParticipantDto updated = participantService.update(participantId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Снять с регистрации участника")
    @PostMapping("/{participantId}/unregister")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<ParticipantDto> unregisterParticipant(@PathVariable @NotNull Long participantId) {
        UpdateParticipantRequest request = UpdateParticipantRequest.builder()
                .isRegistered(false)
                .build();
        ParticipantDto updated = participantService.update(participantId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Переместить участника в другую активность")
    @PostMapping("/{participantId}/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<ParticipantDto> changeActivity(@PathVariable @NotNull Long participantId,
                                                                @PathVariable @NotNull Long activityId) {
        UpdateParticipantRequest request = UpdateParticipantRequest.builder()
                .activityId(activityId)
                .build();
        ParticipantDto updated = participantService.update(participantId, request);
        return ResponseEntity.ok(updated);
    }
}
