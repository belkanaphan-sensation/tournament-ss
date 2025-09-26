package org.bn.sensation.core.round.presentation;

import java.util.List;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.round.service.RoundResultService;
import org.bn.sensation.core.round.service.dto.CreateRoundResultRequest;
import org.bn.sensation.core.round.service.dto.RoundResultDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundResultRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/round-result")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Round Result", description = "The Round Result API")
public class RoundResultController {

    private final RoundResultService roundResultService;

    @Operation(summary = "Получить результат раунда по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return roundResultService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить все результаты раундов")
    @GetMapping
    public ResponseEntity<Page<RoundResultDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(roundResultService.findAll(pageable));
    }

    @Operation(summary = "Получить результаты раунда по ID раунда")
    @GetMapping(path = "/round/{roundId}")
    public ResponseEntity<List<RoundResultDto>> getByRoundId(
            @Parameter @PathVariable("roundId") @NotNull Long roundId) {
        List<RoundResultDto> result = List.of(
                RoundResultDto.builder()
                        .participant(new EntityLinkDto(1L, "25"))
                        .round(new EntityLinkDto(1L, "round 1"))
                        .milestoneCriteria(new EntityLinkDto(1L, "milestone criteria 1"))
                        .activityUser(new EntityLinkDto(1L, "activity user 1"))
                        .score(7)
                        .build(),
                RoundResultDto.builder()
                        .participant(new EntityLinkDto(2L, "25"))
                        .round(new EntityLinkDto(1L, "round 1"))
                        .milestoneCriteria(new EntityLinkDto(1L, "milestone criteria 1"))
                        .activityUser(new EntityLinkDto(1L, "activity user 1"))
                        .score(9)
                        .build()
        );
        return ResponseEntity.ok(result);
//        return ResponseEntity.ok(roundResultService.findByRoundId(roundId));
    }

    @Operation(summary = "Получить результаты раунда по ID этапа")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<List<RoundResultDto>> getResultByMilestoneId(
            @Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        List<RoundResultDto> result = List.of(
                RoundResultDto.builder()
                        .participant(new EntityLinkDto(1L, "25"))
                        .round(new EntityLinkDto(1L, "round 1"))
                        .milestoneCriteria(new EntityLinkDto(1L, "milestone criteria 1"))
                        .activityUser(new EntityLinkDto(1L, "activity user 1"))
                        .score(7)
                        .build(),
                RoundResultDto.builder()
                        .participant(new EntityLinkDto(2L, "25"))
                        .round(new EntityLinkDto(1L, "round 1"))
                        .milestoneCriteria(new EntityLinkDto(1L, "milestone criteria 1"))
                        .activityUser(new EntityLinkDto(1L, "activity user 1"))
                        .score(9)
                        .build(),
                RoundResultDto.builder()
                        .participant(new EntityLinkDto(2L, "25"))
                        .round(new EntityLinkDto(2L, "round 2"))
                        .milestoneCriteria(new EntityLinkDto(1L, "milestone criteria 1"))
                        .activityUser(new EntityLinkDto(1L, "activity user 1"))
                        .score(4)
                        .build()
        );
        return ResponseEntity.ok(result);
//        return ResponseEntity.ok(roundResultService.findByMilestoneId(milestoneId));
    }

    @Operation(summary = "Получить результаты раунда по ID участника")
    @GetMapping(path = "/participant/{participantId}")
    public ResponseEntity<List<RoundResultDto>> getByParticipantId(
            @Parameter @PathVariable("participantId") @NotNull Long participantId) {
        return ResponseEntity.ok(roundResultService.findByParticipantId(participantId));
    }

    @Operation(summary = "Получить результаты раунда по ID судьи")
    @GetMapping(path = "/judge/{activityUserId}")
    public ResponseEntity<List<RoundResultDto>> getByActivityUserId(
            @Parameter @PathVariable("activityUserId") @NotNull Long activityUserId) {
        return ResponseEntity.ok(roundResultService.findByActivityUserId(activityUserId));
    }

    @Operation(summary = "Создать новый результат раунда")
    @PostMapping
    public ResponseEntity<RoundResultDto> create(@Valid @RequestBody CreateRoundResultRequest request) {
        RoundResultDto created = roundResultService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить результат раунда по ID")
    @PutMapping("/{id}")
    public ResponseEntity<RoundResultDto> update(@PathVariable("id") @NotNull Long id,
                                               @Valid @RequestBody UpdateRoundResultRequest request) {
        RoundResultDto updated = roundResultService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить результат раунда по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        roundResultService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
