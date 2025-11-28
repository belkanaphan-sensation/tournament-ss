package org.bn.sensation.core.milestoneresult.presentation;

import java.util.List;
import java.util.Map;

import org.bn.sensation.core.milestoneresult.service.MilestoneResultReportService;
import org.bn.sensation.core.milestoneresult.service.MilestoneResultService;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
@RequestMapping("/api/v1/milestone-result")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Milestone Result", description = "The Milestone Result API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
public class MilestoneResultController {

    private final MilestoneResultService milestoneResultService;
    private final MilestoneResultReportService milestoneResultReportService;

    @Operation(summary = "Обновить результаты этапа")
    @PostMapping("/update/milestone/{milestoneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<List<MilestoneResultDto>> updateForMilestone(@PathVariable("milestoneId") @NotNull Long milestoneId,
                                                                       @Valid @RequestBody List<UpdateMilestoneResultRequest> request) {
        List<MilestoneResultDto> dtos = milestoneResultService.acceptResults(milestoneId, request);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Получить результат этапа по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<MilestoneResultDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return milestoneResultService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить результаты этапов по ID этапа")
    @GetMapping("/milestone/{milestoneId}")
    public ResponseEntity<List<MilestoneResultDto>> getByMilestoneId(@PathVariable("milestoneId") @NotNull Long milestoneId) {
        return ResponseEntity.ok(milestoneResultService.getByMilestoneId(milestoneId));
    }

    @Operation(summary = "Получить результаты этапов по ID активности в порядке этапов от финала к отборочным")
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Map<Integer, List<MilestoneResultDto>>> getByActivityId(@PathVariable("activityId") @NotNull Long activityId) {
        return ResponseEntity.ok(milestoneResultService.getByActivityId(activityId));
    }

    @Operation(summary = "Скачать Excel-отчет по результатам этапов активности")
    @GetMapping("/activity/{activityId}/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER')")
    public ResponseEntity<Resource> getInReportByActivityId(
            @Parameter(description = "ID активности")
            @PathVariable("activityId") @NotNull Long activityId) {

        Map<Integer, List<MilestoneResultDto>> milestoneResults = milestoneResultService.getByActivityId(activityId);
        byte[] report = milestoneResultReportService.generateMilestoneResultReport(milestoneResults);
        ByteArrayResource resource = new ByteArrayResource(report);
        String filename = String.format("milestone-results-activity-%d-report.xlsx", activityId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(milestoneResultReportService.getContentType()))
                .contentLength(report.length)
                .body(resource);
    }
}
