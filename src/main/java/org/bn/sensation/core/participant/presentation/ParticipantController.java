package org.bn.sensation.core.participant.presentation;

import org.bn.sensation.core.participant.service.ParticipantService;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Participant", description = "The Participant API")
public class ParticipantController {

    private final ParticipantService participantService;

    @Operation(summary = "Получить участника по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return participantService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить всех участников с пагинацией")
    @GetMapping
    public ResponseEntity<Page<ParticipantDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(participantService.findAll(pageable));
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
