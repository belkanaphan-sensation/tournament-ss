package org.bn.sensation.core.activity.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bn.sensation.core.activity.service.ActivityService;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tournament/activity")
@RequiredArgsConstructor
@Validated
@Tag(name = "Activity", description = "Activity management APIs")
public class ActivityController {

    private final ActivityService activityService;

    private static final String ACTIVITY = "Activity";
    private static final String ACTIVITY_LOW_CASE = "activity";

    @Operation(
            summary = "Get " + ACTIVITY_LOW_CASE + " by id",
            description = "Returns a single " + ACTIVITY_LOW_CASE + " by its id")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = ACTIVITY + " found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ActivityDto.class))),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden"),
                @ApiResponse(
                        responseCode = "404",
                        description = ACTIVITY + " not found",
                        content = @Content)
            })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ActivityDto getById(@PathVariable Long id) {
        return null;
    }
}
