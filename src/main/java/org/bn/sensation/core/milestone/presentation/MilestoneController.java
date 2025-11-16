package org.bn.sensation.core.milestone.presentation;

import java.util.List;

import org.bn.sensation.core.milestone.service.MilestoneService;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.PrepareRoundsRequest;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
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
@RequestMapping("/api/v1/milestone")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone", description = "The Milestone API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ANNOUNCER')")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @Operation(summary = "Получить этап по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MilestoneDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить этапы по ID активности")
    @GetMapping(path = "/activity/{id}")
    public ResponseEntity<List<MilestoneDto>> getByActivityId(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(milestoneService.findByActivityId(id));
    }

    @Operation(summary = "Получить этапы по ID активности в лайфстейтах")
    @GetMapping(path = "/activity/{id}/life")
    public ResponseEntity<List<MilestoneDto>> getByActivityIdInLifeStates(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(milestoneService.findByActivityIdInLifeStates(id));
    }

    @Operation(summary = "Получить все этапы с пагинацией")
    @GetMapping
    public ResponseEntity<Page<MilestoneDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(milestoneService.findAll(pageable));
    }

    @Operation(summary = "Создать новую этап")
    @PostMapping
    public ResponseEntity<MilestoneDto> create(@Valid @RequestBody CreateMilestoneRequest request) {
        MilestoneDto created = milestoneService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить этап по ID")
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneDto> update(@PathVariable("id") @NotNull Long id,
                                               @Valid @RequestBody UpdateMilestoneRequest request) {
        MilestoneDto updated = milestoneService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить этап по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        milestoneService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Перевести этап обратно в черновик",
            description = "доступно для администратора")
    @PostMapping(path = "/draft/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<Void> draftMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        milestoneService.draftMilestone(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Запланировать этап по ID",
            description = "Запланировать этап может администратор")
    @PostMapping(path = "/plan/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<Void> planMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        milestoneService.planMilestone(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Сформировать этап с участниками и раундами по ID",
            description = "доступно для администратора")
    @PostMapping(path = "/prepare-rounds/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<List<RoundDto>> prepareRoundsForMilestone(@Parameter @PathVariable("id") @NotNull Long id,
                                                                    @Valid @RequestBody PrepareRoundsRequest request) {
        List<RoundDto> rounds = milestoneService.prepareRounds(id, request);
        return ResponseEntity.ok(rounds);
    }

    @Operation(summary = "Перегенерировать раунды")
    @PostMapping(path = "/regenerate-rounds/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<List<RoundDto>> regenerateRoundsForMilestone(@Parameter @PathVariable("id") @NotNull Long id,
                                                                    @Valid @RequestBody PrepareRoundsRequest request) {
        List<RoundDto> rounds = milestoneService.regenerateRounds(id, request);
        return ResponseEntity.ok(rounds);
    }

    @Operation(summary = "Начать этап по ID",
            description = "Начать этап может администратор")
    @PostMapping(path = "/start/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<Void> startMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        milestoneService.startMilestone(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Подвести предварительные итоги этапа по ID",
            description = "доступно для администратора")
    @PostMapping(path = "/sum-up/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<List<MilestoneResultDto>> sumUpMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        //TODO проверить или рассчитать участников и раунды
        List<MilestoneResultDto> milestoneResults = milestoneService.sumUpMilestone(id);
        return ResponseEntity.ok(milestoneResults);
    }

    @Operation(summary = "Завершить этап по ID",
            description = "Завершить этап может администратор")
    @PostMapping(path = "/complete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<Void> completeMilestone(@Parameter @PathVariable("id") @NotNull Long id,
                                                  @Valid @RequestBody List<UpdateMilestoneResultRequest> request) {
        milestoneService.completeMilestone(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Пропустить этап по ID",
            description = "Пропустить этап может администратор")
    @PostMapping(path = "/skip/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERADMIN')")
    public ResponseEntity<Void> skipMilestone(@Parameter @PathVariable("id") @NotNull Long id) {
        milestoneService.skipMilestone(id);
        return ResponseEntity.noContent().build();
    }

}
