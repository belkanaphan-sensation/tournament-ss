package org.bn.sensation.core.activityuser.presentation;

import java.util.List;

import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.bn.sensation.core.activityuser.service.ActivityUserService;
import org.bn.sensation.core.activityuser.service.dto.CreateActivityUserRequest;
import org.bn.sensation.core.activityuser.service.dto.UpdateActivityUserRequest;
import org.bn.sensation.core.activityuser.service.dto.ActivityUserDto;
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
@RequestMapping("/api/v1/activity-user")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Activity User Assignment", description = "The Activity User Assignment API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ANNOUNCER')")
public class ActivityUserController {

    private final ActivityUserService activityUserService;

    @Operation(summary = "Получить назначение по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ActivityUserDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return activityUserService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить назначение по ID пользователя и активности")
    @GetMapping(path = "/user/{userId}/activity/{activityId}")
    public ResponseEntity<ActivityUserDto> getByUserIdAndActivityId(
            @Parameter @PathVariable("userId") @NotNull Long userId,
            @Parameter @PathVariable("activityId") @NotNull Long activityId) {
        ActivityUserDto assignment = activityUserService.findByUserIdAndActivityId(userId, activityId);
        return ResponseEntity.ok(assignment);
    }

    @Operation(summary = "Получить назначение по ID мероприятия для текущего юзера")
    @GetMapping(path = "/occasion/{occasionId}/currentUser")
    public ResponseEntity<List<ActivityUserDto>> getByOccasionIdForCurrentUser(
            @Parameter @PathVariable("occasionId") @NotNull Long occasionId) {
        List<ActivityUserDto> assignments = activityUserService.findByOccasionIdForCurrentUser(occasionId);
        return ResponseEntity.ok(assignments);
    }

    @Operation(summary = "Получить назначение по ID активности для текущего юзера")
    @GetMapping(path = "/activity/{activityId}/currentUser")
    public ResponseEntity<ActivityUserDto> getByActivityIdForCurrentUser(
            @Parameter @PathVariable("activityId") @NotNull Long activityId) {
        ActivityUserDto assignment = activityUserService.findByActivityIdForCurrentUser(activityId);
        return ResponseEntity.ok(assignment);
    }


    @Operation(summary = "Получить назначения активности по роли")
    @GetMapping(path = "/activity/{activityId}/position/{position}")
    public ResponseEntity<List<ActivityUserDto>> getByActivityIdAndPosition(
            @Parameter @PathVariable("activityId") @NotNull Long activityId,
            @Parameter @PathVariable("position") @NotNull UserActivityPosition position) {
        return ResponseEntity.ok(activityUserService.findByActivityIdAndPosition(activityId, position));
    }

    @Operation(summary = "Получить все назначения")
    @GetMapping
    public ResponseEntity<Page<ActivityUserDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(activityUserService.findAll(pageable));
    }

    @Operation(summary = "Получить назначения пользователя")
    @GetMapping(path = "/user/{userId}")
    public ResponseEntity<List<ActivityUserDto>> getByUserId(
            @Parameter @PathVariable("userId") @NotNull Long userId) {
        return ResponseEntity.ok(activityUserService.findByUserId(userId));
    }

    @Operation(summary = "Получить назначения активности")
    @GetMapping(path = "/activity/{activityId}")
    public ResponseEntity<List<ActivityUserDto>> getByActivityId(
            @Parameter @PathVariable("activityId") @NotNull Long activityId) {
        return ResponseEntity.ok(activityUserService.findByActivityId(activityId));
    }

    @Operation(summary = "Получить назначения по роли")
    @GetMapping(path = "/position/{position}")
    public ResponseEntity<List<ActivityUserDto>> getByPosition(
            @Parameter @PathVariable("position") @NotNull UserActivityPosition position) {
        return ResponseEntity.ok(activityUserService.findByPosition(position));
    }

    @Operation(summary = "Создать новое назначение")
    @PostMapping
    public ResponseEntity<ActivityUserDto> create(@Valid @RequestBody CreateActivityUserRequest request) {
        ActivityUserDto created = activityUserService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить назначение по ID")
    @PutMapping("/{id}")
    public ResponseEntity<ActivityUserDto> update(@PathVariable("id") @NotNull Long id,
                                                  @Valid @RequestBody UpdateActivityUserRequest request) {
        ActivityUserDto updated = activityUserService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить назначение по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        activityUserService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
