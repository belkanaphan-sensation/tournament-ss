package org.bn.sensation.core.organization.service;

import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.dto.UpdateOrganizationRequest;
import org.bn.sensation.core.organization.service.mapper.CreateOrganizationRequestMapper;
import org.bn.sensation.core.organization.service.mapper.OrganizationDtoMapper;
import org.bn.sensation.core.organization.service.mapper.UpdateOrganizationRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationDtoMapper organizationDtoMapper;
    private final CreateOrganizationRequestMapper createOrganizationRequestMapper;
    private final UpdateOrganizationRequestMapper updateOrganizationRequestMapper;

    @Override
    public BaseRepository<OrganizationEntity> getRepository() {
        return organizationRepository;
    }

    @Override
    public BaseDtoMapper<OrganizationEntity, OrganizationDto> getMapper() {
        return organizationDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationDto> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(organizationDtoMapper::toDto);
    }

    @Override
    @Transactional
    public OrganizationDto create(CreateOrganizationRequest request) {
        // Проверяем, существует ли организация с таким названием уже
        organizationRepository.findByName(request.getName())
                .ifPresent(org -> {
                    throw new IllegalArgumentException("Организация с таким названием уже существует: " + request.getName());
                });

        // Проверяем, существует ли email уже
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            organizationRepository.findByEmail(request.getEmail())
                    .ifPresent(org -> {
                        throw new IllegalArgumentException("Организация с таким email уже существует: " + request.getEmail());
                    });
        }

        // Создаем сущность организации
        OrganizationEntity organization = createOrganizationRequestMapper.toEntity(request);

        OrganizationEntity saved = organizationRepository.save(organization);
        return organizationDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrganizationDto update(Long id, UpdateOrganizationRequest request) {
        OrganizationEntity organization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Организация не найдена с id: " + id));

        // Проверяем, существует ли название уже (если изменилось)
        if (request.getName() != null && !request.getName().isBlank()
                && !request.getName().equals(organization.getName())) {
            organizationRepository.findByName(request.getName())
                    .ifPresent(org -> {
                        throw new IllegalArgumentException("Организация с таким названием уже существует: " + request.getName());
                    });
        }

        // Проверяем, существует ли email уже (если изменился)
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(organization.getEmail())) {
            organizationRepository.findByEmail(request.getEmail())
                    .ifPresent(org -> {
                        throw new IllegalArgumentException("Организация с таким email уже существует: " + request.getEmail());
                    });
        }

        // Обновляем поля организации
        updateOrganizationRequestMapper.updateOrganizationFromRequest(request, organization);

        // Обновляем адрес
        if (request.getAddress() != null) {
            Address address = organization.getAddress();
            if (address == null) {
                address = Address.builder().build();
            }
            updateOrganizationRequestMapper.updateAddressFromRequest(request.getAddress(), address);
        }

        OrganizationEntity saved = organizationRepository.save(organization);
        return organizationDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        OrganizationEntity organization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Организация не найдена с id: " + id));

        // Проверяем, можно ли удалить организацию
        validateOrganizationCanBeDeleted(organization);

        // Удаляем организацию (Hibernate автоматически удалит связи из промежуточной таблицы)
        organizationRepository.deleteById(id);
    }

    private void validateOrganizationCanBeDeleted(OrganizationEntity organization) {
        log.debug("Проверка возможности удаления организации={}", organization.getId());

        // Проверяем статус связанных мероприятий
        if (organization.getOccasions() != null) {
            log.debug("Найдено {} мероприятий для организации={}", organization.getOccasions().size(), organization.getId());

            for (OccasionEntity occasion : organization.getOccasions()) {
                log.debug("Проверка мероприятия={} со статусом={}", occasion.getId(), occasion.getState());

                if (occasion.getState() != OccasionState.DRAFT && occasion.getState() != OccasionState.COMPLETED) {
                    log.warn("Нельзя удалить организацию={}, мероприятие={} имеет активный статус={}",
                            organization.getId(), occasion.getId(), occasion.getState());
                    throw new IllegalArgumentException("Нельзя удалить организацию, у которой есть активные мероприятия. " +
                            "Мероприятие '" + occasion.getName() + "' имеет статус: " + occasion.getState());
                }
            }
        }

        log.debug("Организация={} может быть удалена", organization.getId());
    }
}
