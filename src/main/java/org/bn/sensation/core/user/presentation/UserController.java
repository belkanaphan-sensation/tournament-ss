package org.bn.sensation.core.user.presentation;

import org.bn.sensation.core.user.service.UserService;
import org.bn.sensation.core.user.service.dto.CreateUserRequest;
import org.bn.sensation.core.user.service.dto.UpdateUserRequest;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "User", description = "The User API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Получить данные о текущем залогиненом пользователе")
    @GetMapping(path = "/currentUser")
    public ResponseEntity<UserDto> getCurrentUser() {
        return userService.getCurrentUser()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Получить всех пользователей с пагинацией")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<UserDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @Operation(summary = "Создать нового пользователя")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        UserDto created = userService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить пользователя по ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserDto> update(@PathVariable("id") @NotNull Long id,
                                        @Valid @RequestBody UpdateUserRequest request) {
        UserDto updated = userService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить пользователя по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Привязать организацию к пользователю")
    @PostMapping("/{userId}/organizations/{orgId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserDto> addUserToOrganization(@PathVariable Long userId,
                                                      @PathVariable Long orgId) {
        UserDto updated = userService.assignUserToOrganization(userId, orgId);
        return ResponseEntity.ok(updated);
    }
}
