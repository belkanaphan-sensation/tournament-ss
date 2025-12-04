package org.bn.sensation.core.activity.presentation;

import java.util.List;

import org.bn.sensation.core.activity.service.ActivityReportService;
import org.bn.sensation.core.activity.service.ActivityService;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.activityresult.ActivityResultService;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activityresult.service.dto.CreateActivityResultRequest;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityResultService activityResultService;
    private final ActivityReportService activityReportService;

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

    @Operation(summary = "Получить запланированные активности по ID мероприятия")
    @GetMapping(path = "/planned/occasion/{id}")
    public ResponseEntity<List<EntityLinkDto>> getPlannedByOccasionId(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(activityService.findPlannedByOccasionId(id));
    }

    @Operation(summary = "Получить активности по ID мероприятия в лайфстейтах")
    @GetMapping(path = "/occasion/{id}/life/currentUser")
    public ResponseEntity<List<ActivityDto>> getByOccasionIdInLifeStates(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(activityService.findByOccasionIdInLifeStatesForCurrentUser(id));
    }

    @Operation(summary = "Получить активности по ID мероприятия в InProgress стейте для текущего пользователя")
    @GetMapping(path = "/occasion/{id}/inProgress/currentUser")
    public ResponseEntity<List<ActivityDto>> getByOccasionIdInInProgressState(@Parameter @PathVariable("id") @NotNull Long id) {
        return ResponseEntity.ok(activityService.findByOccasionIdInInProgressStateForCurrentUser(id));
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

    @Operation(summary = "Запланировать активность по ID",
            description = "доступно для администратора")
    @PostMapping(path = "/plan/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Void> planActivity(@Parameter @PathVariable("id") @NotNull Long id) {
        activityService.planActivity(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Завершить регистрацию участников в активность по ID",
            description = "доступно для администратора")
    @PostMapping(path = "/close-registration/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<List<ContestantDto>> closeRegistrationToActivity(@Parameter @PathVariable("id") @NotNull Long id) {
        activityService.closeRegistrationToActivity(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Начать активность по ID",
            description = "доступно для администратора")
    @PostMapping(path = "/start/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Void> startActivity(@Parameter @PathVariable("id") @NotNull Long id) {
        activityService.startActivity(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Подвести итоги активности по ID",
            description = "доступно для администратора")
    @PostMapping(path = "/sum-up/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<List<ActivityResultDto>> sumUpActivity(@Parameter @PathVariable("id") @NotNull Long id,
                                                                 @Valid @RequestBody List<CreateActivityResultRequest> request) {
        List<ActivityResultDto> dtos = activityService.sumUpActivity(id, request);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить итоги активности по ID",
            description = "доступно для администратора")
    @GetMapping(path = "/results/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<List<ActivityResultDto>> getActivityResults(@Parameter @PathVariable("id") @NotNull Long id) {
        List<ActivityResultDto> dtos = activityResultService.getByActivityId(id);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Завершить активность по ID",
            description = "доступно для администратора")
    @PostMapping(path = "/complete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Void> completeActivity(@Parameter @PathVariable("id") @NotNull Long id) {
        activityService.completeActivity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{id}/report")
    @Operation(summary = "Скачать Excel-отчет по активности")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Resource> downloadReport(
            @Parameter(description = "ID активности")
            @PathVariable("id") @NotNull Long id) {

        byte[] report = activityReportService.generateActivityReport(id);
        ByteArrayResource resource = new ByteArrayResource(report);
        String filename = String.format("activity-%d-report.xlsx", id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(activityReportService.getContentType()))
                .contentLength(report.length)
                .body(resource);
    }
}
