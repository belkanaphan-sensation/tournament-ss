package org.bn.sensation.core.assistant.presentation;

import org.bn.sensation.core.assistant.service.AssistantService;
import org.bn.sensation.core.assistant.service.dto.AssistantDto;
import org.bn.sensation.core.assistant.service.dto.CreateAssistantRequest;
import org.bn.sensation.core.assistant.service.dto.UpdateAssistantRequest;
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
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Assistant", description = "The Assistant API")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'USER', 'ADMINISTRATOR', 'ANNOUNCER')")
public class AssistantController {

    private final AssistantService assistantService;

    @Operation(summary = "Получить ассистента по ID")
    @GetMapping(path = "/{id}")
    public ResponseEntity<AssistantDto> getById(@Parameter @PathVariable("id") @NotNull Long id) {
        return assistantService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @Operation(summary = "Получить всех ассистентов")
    @GetMapping
    public ResponseEntity<Page<AssistantDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(assistantService.findAll(pageable));
    }

    @Operation(summary = "Создать нового ассистента")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<AssistantDto> create(@Valid @RequestBody CreateAssistantRequest request) {
        AssistantDto created = assistantService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Обновить ассистента по ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<AssistantDto> update(@PathVariable("id") @NotNull Long id,
                                               @Valid @RequestBody UpdateAssistantRequest request) {
        AssistantDto updated = assistantService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить ассистента по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NotNull Long id) {
        assistantService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
