package org.bn.sensation.core.role.presentation;

import org.bn.sensation.core.role.service.RoleService;
import org.bn.sensation.core.role.service.dto.CreateRoleRequest;
import org.bn.sensation.core.role.service.dto.RoleDto;
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
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Role", description = "The Role API")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get role by id")
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return roleService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Get all roles with pagination")
    @GetMapping
    public ResponseEntity<Page<RoleDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(roleService.findAll(pageable));
    }

    @Operation(summary = "Create new role")
    @PostMapping
    public ResponseEntity<RoleDto> create(@Valid @RequestBody CreateRoleRequest request) {
        RoleDto created = roleService.create(request);
        return ResponseEntity.ok(created);
    }


    @Operation(summary = "Delete role by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        roleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
