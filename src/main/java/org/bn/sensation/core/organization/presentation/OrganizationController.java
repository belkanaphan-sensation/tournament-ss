package org.bn.sensation.core.organization.presentation;

import org.bn.sensation.core.organization.service.OrganizationService;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.dto.UpdateOrganizationRequest;
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
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Organization", description = "The Organization API")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Get organization by id")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return organizationService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Get all organizations with pagination")
    @GetMapping
    public ResponseEntity<Page<OrganizationDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(organizationService.findAll(pageable));
    }

    @Operation(summary = "Create new organization")
    @PostMapping
    public ResponseEntity<OrganizationDto> create(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDto created = organizationService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Update organization by id")
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationDto> update(@PathVariable("id") @NotNull Long id,
                                                @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationDto updated = organizationService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete organization by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        organizationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
