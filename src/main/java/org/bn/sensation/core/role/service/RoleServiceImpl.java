package org.bn.sensation.core.role.service;

import java.util.Optional;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.role.entity.Role;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.repository.RoleRepository;
import org.bn.sensation.core.role.service.dto.CreateRoleRequest;
import org.bn.sensation.core.role.service.dto.RoleDto;
import org.bn.sensation.core.role.service.mapper.RoleDtoMapper;
import org.bn.sensation.core.role.service.mapper.CreateRoleRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleDtoMapper roleDtoMapper;
    private final CreateRoleRequestMapper createRoleRequestMapper;

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
        // Проверяем, существует ли роль уже
        Optional<RoleEntity> existingRole = roleRepository.findByRole(Role.valueOf(request.getRole()));
        if (existingRole.isPresent()) {
            throw new IllegalArgumentException("Роль уже существует: " + request.getRole());
        }

        // Создаем сущность роли
        RoleEntity role = createRoleRequestMapper.toEntity(request);

        RoleEntity saved = roleRepository.save(role);
        return roleDtoMapper.toDto(saved);
    }


    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Роль не найдена с id: " + id);
        }
        roleRepository.deleteById(id);
    }
}
