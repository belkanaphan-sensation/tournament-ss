package org.bn.sensation.core.activity.presentation;

import org.bn.sensation.core.activity.service.ActivityService;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.ActivityStatisticsDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
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
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Activity", description = "The Activity API")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "Получить активность по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return activityService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить активности по ID мероприятия")
    @GetMapping(path = "/occasion/{id}")
    public ResponseEntity<Page<ActivityDto>> getByOccasionId(@Parameter @PathVariable("id") @NotNull Long id, Pageable pageable) {
        return ResponseEntity.ok(activityService.findByOccasionId(id, pageable));
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

    @Operation(summary = "Получить статистику этапов активности")
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ActivityStatisticsDto> getMilestoneStatistics(@Parameter @PathVariable("id") @NotNull Long id) {
        ActivityStatisticsDto statistics = activityService.getMilestoneStatistics(id);
        return ResponseEntity.ok(statistics);
    }
}
