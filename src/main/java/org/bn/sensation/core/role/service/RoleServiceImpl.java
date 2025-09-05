package org.bn.sensation.core.role.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.role.entity.Role;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.repository.RoleRepository;
import org.bn.sensation.core.role.service.dto.CreateRoleRequest;
import org.bn.sensation.core.role.service.dto.RoleDto;
import org.bn.sensation.core.role.service.dto.UpdateRoleRequest;
import org.bn.sensation.core.role.service.mapper.RoleDtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleDtoMapper roleDtoMapper;

    @Override
    public BaseRepository<RoleEntity> getRepository() {
        return roleRepository;
    }

    @Override
    public BaseDtoMapper<RoleEntity, RoleDto> getMapper() {
        return roleDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDto> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(roleDtoMapper::toDto);
    }

    @Override
    @Transactional
    public RoleDto create(CreateRoleRequest request) {
        // Check if role already exists
        RoleEntity existingRole = roleRepository.findByRole(Role.valueOf(request.getRole()));
        if (existingRole != null) {
            throw new IllegalArgumentException("Role already exists: " + request.getRole());
        }

        // Create role entity
        RoleEntity role = RoleEntity.builder()
                .role(Role.valueOf(request.getRole()))
                .build();

        RoleEntity saved = roleRepository.save(role);
        return roleDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoleDto update(Long id, UpdateRoleRequest request) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        // Check if new role value already exists (if changed)
        if (request.getRole() != null && !request.getRole().equals(role.getRole().name())) {
            RoleEntity existingRole = roleRepository.findByRole(Role.valueOf(request.getRole()));
            if (existingRole != null) {
                throw new IllegalArgumentException("Role already exists: " + request.getRole());
            }
        }

        // Update role
        if (request.getRole() != null) {
            role.setRole(Role.valueOf(request.getRole()));
        }

        RoleEntity saved = roleRepository.save(role);
        return roleDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }
}
