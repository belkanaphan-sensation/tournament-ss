package org.bn.sensation.core.criteria.presentation;

import org.bn.sensation.core.criteria.service.CriteriaService;
import org.bn.sensation.core.criteria.service.dto.CriteriaRequest;
import org.bn.sensation.core.criteria.service.dto.CriteriaDto;
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
@RequestMapping("/api/v1/criteria")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Criteria", description = "The Criteria API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'OCCASION_ADMIN', 'USER')")
public class CriteriaController {

    private final CriteriaService criteriaService;

    @Operation(summary = "Получить критерий по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<CriteriaDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return criteriaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить все критерии с пагинацией")
    @GetMapping
    public ResponseEntity<Page<CriteriaDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(criteriaService.findAll(pageable));
    }


    @Operation(summary = "Создать новый критерий")
    @PostMapping
    public ResponseEntity<CriteriaDto> create(@Valid @RequestBody CriteriaRequest request) {
        CriteriaDto created = criteriaService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить критерий по ID")
    @PutMapping("/{id}")
    public ResponseEntity<CriteriaDto> update(@PathVariable("id") @NotNull Long id,
                                             @Valid @RequestBody CriteriaRequest request) {
        CriteriaDto updated = criteriaService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить критерий по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        criteriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
