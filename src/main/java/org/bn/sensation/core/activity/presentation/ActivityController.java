package org.bn.sensation.core.activity.presentation;

import java.util.List;

import org.bn.sensation.core.activity.service.ActivityService;
import org.bn.sensation.core.activity.service.ActivityStateMachineService;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
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
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Activity", description = "The Activity API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityStateMachineService activityStateMachineService;

    @Operation(summary = "Получить активность по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ActivityDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return activityService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить активности по ID мероприятия")
    @GetMapping(path = "/occasion/{id}")
    public ResponseEntity<List<ActivityDto>> getByOccasionId(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(activityService.findByOccasionId(id));
    }

    @Operation(summary = "Получить активности по ID мероприятия в лайфстейтах")
    @GetMapping(path = "/occasion/{id}/life/currentUser")
    public ResponseEntity<List<ActivityDto>> getByOccasionIdInLifeStates(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(activityService.findByOccasionIdInLifeStatesForCurrentUser(id));
    }

    @Operation(summary = "Получить все активности с пагинацией")
    @GetMapping
    public ResponseEntity<Page<ActivityDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(activityService.findAll(pageable));
    }

    @Operation(summary = "Создать новую активность")
    @PostMapping
    public ResponseEntity<ActivityDto> create(@Valid @RequestBody CreateActivityRequest request) {
        ActivityDto created = activityService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить активность по ID")
    @PutMapping("/{id}")
    public ResponseEntity<ActivityDto> update(@PathVariable("id") @NotNull Long id,
                                            @Valid @RequestBody UpdateActivityRequest request) {
        ActivityDto updated = activityService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить активность по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        activityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить результаты активности")
    @GetMapping(path = "/{id}/result")
    public ResponseEntity<List<ActivityResultDto>> getResultById(
            @Parameter @PathVariable("id") @NotNull Long id) {
        List<ActivityResultDto> result = List.of(
                ActivityResultDto.builder()
                        .participant(new EntityLinkDto(1L, "25"))
                        .scoreSum(71)
                        .build(),
                ActivityResultDto.builder()
                        .participant(new EntityLinkDto(2L, "35"))
                        .scoreSum(22)
                        .build()
        );
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Запланировать активность по ID",
            description = "Запланировать активность может администратор")
    @GetMapping(path = "/plan/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> planActivity(@Parameter @PathVariable("id") @NotNull Long id) {
        activityStateMachineService.sendEvent(id, ActivityEvent.PLAN);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Начать активность по ID",
            description = "Начать активность может администратор")
    @GetMapping(path = "/start/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> startActivity(@Parameter @PathVariable("id") @NotNull Long id) {
        activityStateMachineService.sendEvent(id, ActivityEvent.START);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Завершить активность по ID",
            description = "Завершить активность может администратор")
    @GetMapping(path = "/stop/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> completeActivity(@Parameter @PathVariable("id") @NotNull Long id) {
        activityStateMachineService.sendEvent(id, ActivityEvent.COMPLETE);
        return ResponseEntity.noContent().build();
    }
}
