package org.bn.sensation.core.occasion.service;

import java.util.Optional;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.bn.sensation.core.occasion.service.mapper.CreateOccasionRequestMapper;
import org.bn.sensation.core.occasion.service.mapper.OccasionDtoMapper;
import org.bn.sensation.core.occasion.service.mapper.UpdateOccasionRequestMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OccasionServiceImpl implements OccasionService {

    private final OccasionRepository occasionRepository;
    private final OccasionDtoMapper occasionDtoMapper;
    private final CreateOccasionRequestMapper createOccasionRequestMapper;
    private final UpdateOccasionRequestMapper updateOccasionRequestMapper;
    private final OrganizationRepository organizationRepository;

    @Override
    public BaseRepository<OccasionEntity> getRepository() {
        return occasionRepository;
    }

    @Override
    public BaseDtoMapper<OccasionEntity, OccasionDto> getMapper() {
        return occasionDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OccasionDto> findAll(Pageable pageable) {
        return occasionRepository.findAll(pageable).map(this::enrichOccasionDtoWithStatistics);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OccasionDto> findById(Long id) {
        return occasionRepository.findById(id)
                .map(this::enrichOccasionDtoWithStatistics);
    }

    @Override
    @Transactional
    public OccasionDto create(CreateOccasionRequest request) {
        // Проверяем, что организация существует
        OrganizationEntity organization = findOrganizationById(request.getOrganizationId());

        // Создаем сущность мероприятия
        OccasionEntity occasion = createOccasionRequestMapper.toEntity(request);
        occasion.setOrganization(organization);

        OccasionEntity saved = occasionRepository.save(occasion);
        return enrichOccasionDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public OccasionDto update(Long id, UpdateOccasionRequest request) {
        OccasionEntity occasion = occasionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено с id: " + id));

        // Обновляем поля мероприятия
        updateOccasionRequestMapper.updateOccasionFromRequest(request, occasion);

        OccasionEntity saved = occasionRepository.save(occasion);
        return enrichOccasionDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!occasionRepository.existsById(id)) {
            throw new EntityNotFoundException("Событие не найдено с id: " + id);
        }
        occasionRepository.deleteById(id);
    }

    /**
     * Обогащает OccasionDto статистикой по активностям
     */
    private OccasionDto enrichOccasionDtoWithStatistics(OccasionEntity occasion) {
        OccasionDto dto = occasionDtoMapper.toDto(occasion);
        dto.setCompletedActivitiesCount((int) occasion.getActivities().stream()
                .filter(activity -> activity.getState() == ActivityState.COMPLETED)
                .count());
        dto.setActiveActivitiesCount((int) occasion.getActivities().stream()
                .filter(activity -> activity.getState() == ActivityState.PLANNED || activity.getState() == ActivityState.IN_PROGRESS)
                .count());
        dto.setTotalActivitiesCount(occasion.getActivities().size());

        return dto;
    }

    private OrganizationEntity findOrganizationById(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Организация не найдена с id: " + organizationId));
    }

    @Override
    public void saveTransition(OccasionEntity occasion, OccasionState state) {
        occasion.setState(state);
        occasionRepository.save(occasion);
    }

    @Override
    public boolean canTransition(OccasionEntity occasion, OccasionEvent event) {
        // TODO: Implement business logic for occasion transitions
        return true;
    }

    @Override
    public OccasionState getNextState(OccasionState currentState, OccasionEvent event) {
        return switch (currentState) {
            case DRAFT -> event == OccasionEvent.PLAN ? OccasionState.PLANNED : currentState;
            case PLANNED -> event == OccasionEvent.START ? OccasionState.IN_PROGRESS : currentState;
            case IN_PROGRESS -> event == OccasionEvent.COMPLETE ? OccasionState.COMPLETED : currentState;
            case COMPLETED -> currentState;
        };
    }

    @Override
    public boolean isValidTransition(OccasionState currentState, OccasionEvent event) {
        return getNextState(currentState, event) != currentState;
    }
}
