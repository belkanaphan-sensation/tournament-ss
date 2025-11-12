package org.bn.sensation.core.allowedaction.presentation;

import org.bn.sensation.core.allowedaction.service.AllowedActionService;
import org.bn.sensation.core.allowedaction.service.dto.AllowedActionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/action")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Allowed Action", description = "Returns allowed actions for entity for current user")
public class AllowedActionController {

    private final AllowedActionService allowedActionService;

    @Operation(summary = "Получить допустимые действия для текущего пользователя для мероприятия по ID")
    @GetMapping(path = "/occasion/{occasionId}")
    public ResponseEntity<AllowedActionDto> getByOccasionId(@Parameter @PathVariable("occasionId") @NotNull Long occasionId) {
        return ResponseEntity.ok(allowedActionService.getForOccasion(occasionId));
    }

    @Operation(summary = "Получить допустимые действия для текущего пользователя для активности по ID")
    @GetMapping(path = "/activity/{activityId}")
    public ResponseEntity<AllowedActionDto> getByActivityId(@Parameter @PathVariable("activityId") @NotNull Long activityId) {
        return ResponseEntity.ok(allowedActionService.getForActivity(activityId));
    }

    @Operation(summary = "Получить допустимые действия для текущего пользователя для этапа по ID")
    @GetMapping(path = "/milestone/{milestoneId}")
    public ResponseEntity<AllowedActionDto> getByMilestoneId(@Parameter @PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(allowedActionService.getForMilestone(milestoneId));
    }
}
