package org.bn.sensation.core.user.presentation;

import java.util.List;

import org.bn.sensation.core.user.entity.UserActivityPosition;
import org.bn.sensation.core.user.service.UserActivityAssignmentService;
import org.bn.sensation.core.user.service.dto.CreateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UpdateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UserActivityAssignmentDto;
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
@RequestMapping("/api/v1/user-activity-assignment")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "User Activity Assignment", description = "The User Activity Assignment API")
public class UserActivityAssignmentController {

    private final UserActivityAssignmentService userActivityAssignmentService;

    @Operation(summary = "Получить назначение по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserActivityAssignmentDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return userActivityAssignmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить назначение по ID пользователя и активности")
    @GetMapping(path = "/user/{userId}/activity/{activityId}")
    public ResponseEntity<UserActivityAssignmentDto> getByUserIdAndActivityId(
            @Parameter @PathVariable("userId") @NotNull Long userId,
            @Parameter @PathVariable("activityId") @NotNull Long activityId) {
        UserActivityAssignmentDto assignment = userActivityAssignmentService.findByUserIdAndActivityId(userId, activityId);
        return ResponseEntity.ok(assignment);
    }

    @Operation(summary = "Получить назначение по ID мероприятия для текущего юзера")
    @GetMapping(path = "/occasion/{occasionId}/currentUser")
    public ResponseEntity<List<UserActivityAssignmentDto>> getByOccasionIdForCurrentUser(
            @Parameter @PathVariable("occasionId") @NotNull Long occasionId) {
        List<UserActivityAssignmentDto> assignments = userActivityAssignmentService.findByOccasionIdForCurrentUser(occasionId);
        return ResponseEntity.ok(assignments);
    }

    @Operation(summary = "Получить назначение по ID активности для текущего юзера")
    @GetMapping(path = "/activity/{activityId}/currentUser")
    public ResponseEntity<UserActivityAssignmentDto> getByActivityIdForCurrentUser(
            @Parameter @PathVariable("activityId") @NotNull Long activityId) {
        UserActivityAssignmentDto assignment = userActivityAssignmentService.findByActivityIdForCurrentUser(activityId);
        return ResponseEntity.ok(assignment);
    }


    @Operation(summary = "Получить назначения активности по роли")
    @GetMapping(path = "/activity/{activityId}/position/{position}")
    public ResponseEntity<Page<UserActivityAssignmentDto>> getByActivityIdAndPosition(
            @Parameter @PathVariable("activityId") @NotNull Long activityId,
            @Parameter @PathVariable("position") @NotNull UserActivityPosition position,
            Pageable pageable) {
        return ResponseEntity.ok(userActivityAssignmentService.findByActivityIdAndPosition(activityId, position, pageable));
    }

    @Operation(summary = "Получить все назначения")
    @GetMapping
    public ResponseEntity<Page<UserActivityAssignmentDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userActivityAssignmentService.findAll(pageable));
    }

    @Operation(summary = "Получить назначения пользователя")
    @GetMapping(path = "/user/{userId}")
    public ResponseEntity<Page<UserActivityAssignmentDto>> getByUserId(
            @Parameter @PathVariable("userId") @NotNull Long userId, Pageable pageable) {
        return ResponseEntity.ok(userActivityAssignmentService.findByUserId(userId, pageable));
    }

    @Operation(summary = "Получить назначения активности")
    @GetMapping(path = "/activity/{activityId}")
    public ResponseEntity<Page<UserActivityAssignmentDto>> getByActivityId(
            @Parameter @PathVariable("activityId") @NotNull Long activityId, Pageable pageable) {
        return ResponseEntity.ok(userActivityAssignmentService.findByActivityId(activityId, pageable));
    }

    @Operation(summary = "Получить назначения по роли")
    @GetMapping(path = "/position/{position}")
    public ResponseEntity<Page<UserActivityAssignmentDto>> getByPosition(
            @Parameter @PathVariable("position") @NotNull UserActivityPosition position, Pageable pageable) {
        return ResponseEntity.ok(userActivityAssignmentService.findByPosition(position, pageable));
    }

    @Operation(summary = "Создать новое назначение")
    @PostMapping
    public ResponseEntity<UserActivityAssignmentDto> create(@Valid @RequestBody CreateUserActivityAssignmentRequest request) {
        UserActivityAssignmentDto created = userActivityAssignmentService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить назначение по ID")
    @PutMapping("/{id}")
    public ResponseEntity<UserActivityAssignmentDto> update(@PathVariable("id") @NotNull Long id,
                                                          @Valid @RequestBody UpdateUserActivityAssignmentRequest request) {
        UserActivityAssignmentDto updated = userActivityAssignmentService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить назначение по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        userActivityAssignmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
