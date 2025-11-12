package org.bn.sensation.core.criterion.presentation;

import org.bn.sensation.core.criterion.service.CriterionService;
import org.bn.sensation.core.criterion.service.dto.CriterionRequest;
import org.bn.sensation.core.criterion.service.dto.CriterionDto;
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
@RequestMapping("/api/v1/criterion")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Criterion", description = "The Criterion API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
public class CriterionController {

    private final CriterionService criterionService;

    @Operation(summary = "Получить критерий по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<CriterionDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return criterionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить все критерии с пагинацией")
    @GetMapping
    public ResponseEntity<Page<CriterionDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(criterionService.findAll(pageable));
    }


    @Operation(summary = "Создать новый критерий")
    @PostMapping
    public ResponseEntity<CriterionDto> create(@Valid @RequestBody CriterionRequest request) {
        CriterionDto created = criterionService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить критерий по ID")
    @PutMapping("/{id}")
    public ResponseEntity<CriterionDto> update(@PathVariable("id") @NotNull Long id,
                                               @Valid @RequestBody CriterionRequest request) {
        CriterionDto updated = criterionService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить критерий по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        criterionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
