package org.bn.sensation.core.occasion.presentation;

import org.bn.sensation.core.occasion.service.OccasionService;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/occasion")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Occasion", description = "The Occasion API")
public class OccasionController {

    private final OccasionService occasionService;

    @Operation(summary = "Get occasion by id")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return occasionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Get all occasions with pagination")
    @GetMapping
    public ResponseEntity<Page<OccasionDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(occasionService.findAll(pageable));
    }

    @Operation(summary = "Create new occasion")
    @PostMapping
    public ResponseEntity<OccasionDto> create(@Valid @RequestBody CreateOccasionRequest request) {
        OccasionDto created = occasionService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Update occasion by id")
    @PutMapping("/{id}")
    public ResponseEntity<OccasionDto> update(@PathVariable("id") @NotNull Long id,
                                            @Valid @RequestBody UpdateOccasionRequest request) {
        OccasionDto updated = occasionService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete occasion by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        occasionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
