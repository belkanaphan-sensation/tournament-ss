package org.bn.sensation.core.role.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.bn.sensation.core.role.entity.Role;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.repository.RoleRepository;
import org.bn.sensation.core.role.service.dto.CreateRoleRequest;
import org.bn.sensation.core.role.service.dto.RoleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleServiceIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    private RoleEntity testRole;

    @BeforeEach
    void setUp() {
        // Find existing USER role or create test role
        testRole = roleRepository.findByRole(Role.USER)
                .orElseGet(() -> {
                    RoleEntity role = RoleEntity.builder()
                            .role(Role.USER)
                            .build();
                    return roleRepository.save(role);
                });
    }

    @Test
    void testCreateRole() {
        // Given - try to create a role that already exists
        CreateRoleRequest request = CreateRoleRequest.builder()
                .role("ADMIN")
                .build();

        // When & Then - should throw exception since role already exists
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.create(request);
        });
    }

    @Test
    void testCreateRoleWithExistingRole() {
        // Given
        CreateRoleRequest request = CreateRoleRequest.builder()
                .role("USER") // Already exists in system
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.create(request);
        });
    }


    @Test
    void testCreateRoleWithInvalidEnumValue() {
        // Given
        CreateRoleRequest request = CreateRoleRequest.builder()
                .role("INVALID_ROLE") // Invalid enum value
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.create(request);
        });
    }


    @Test
    void testFindAllRoles() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RoleDto> result = roleService.findAll(pageable);

        // Then
        assertNotNull(result);
        // Should have at least the system roles (5)
        assertTrue(result.getTotalElements() >= 5);
        assertTrue(result.getContent().size() >= 5);
    }

    @Test
    void testFindRoleById() {
        // When
        Optional<RoleDto> result = roleService.findById(testRole.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRole.getId(), result.get().getId());
        assertEquals(testRole.getRole().name(), result.get().getRole());
    }

    @Test
    void testFindRoleByIdNotFound() {
        // When
        Optional<RoleDto> result = roleService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteRole() {
        // Given
        Long roleId = testRole.getId();

        // When
        roleService.deleteById(roleId);

        // Then
        assertFalse(roleRepository.existsById(roleId));
    }

    @Test
    void testDeleteRoleNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roleService.deleteById(999L);
        });
    }


}
