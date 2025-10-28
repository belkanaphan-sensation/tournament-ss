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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.debug("Поиск всех мероприятий с пагинацией: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<OccasionDto> result = occasionRepository.findAll(pageable).map(this::enrichOccasionDtoWithStatistics);
        log.debug("Найдено {} мероприятий на странице", result.getContent().size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OccasionDto> findById(Long id) {
        log.debug("Поиск мероприятия по id={}", id);
        Optional<OccasionDto> result = occasionRepository.findById(id)
                .map(this::enrichOccasionDtoWithStatistics);
        if (result.isPresent()) {
            log.debug("Мероприятие найдено: id={}, название={}", id, result.get().getName());
        } else {
            log.debug("Мероприятие не найдено: id={}", id);
        }
        return result;
    }

    @Override
    @Transactional
    public OccasionDto create(CreateOccasionRequest request) {
        log.info("Создание мероприятия: название={}, организация={}", request.getName(), request.getOrganizationId());
        // Проверяем, что организация существует
        OrganizationEntity organization = findOrganizationById(request.getOrganizationId());
        log.debug("Найдена организация={} для создания мероприятия", organization.getId());

        // Создаем сущность мероприятия
        OccasionEntity occasion = createOccasionRequestMapper.toEntity(request);
        occasion.setOrganization(organization);

        OccasionEntity saved = occasionRepository.save(occasion);
        log.info("Мероприятие успешно создано: id={}, название={}", saved.getId(), saved.getName());
        return enrichOccasionDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public OccasionDto update(Long id, UpdateOccasionRequest request) {
        log.info("Обновление мероприятия: id={}, название={}", id, request.getName());
        OccasionEntity occasion = occasionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено с id: " + id));
        log.debug("Найдено мероприятие={} для обновления", occasion.getId());

        // Обновляем поля мероприятия
        updateOccasionRequestMapper.updateOccasionFromRequest(request, occasion);

        OccasionEntity saved = occasionRepository.save(occasion);
        log.info("Мероприятие успешно обновлено: id={}", saved.getId());
        return enrichOccasionDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление мероприятия: id={}", id);
        if (!occasionRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующего мероприятия: id={}", id);
            throw new EntityNotFoundException("Событие не найдено с id: " + id);
        }
        occasionRepository.deleteById(id);
        log.info("Мероприятие успешно удалено: id={}", id);
    }

    /**
     * Обогащает OccasionDto статистикой по активностям
     */
    private OccasionDto enrichOccasionDtoWithStatistics(OccasionEntity occasion) {
        log.debug("Обогащение статистикой мероприятия={}", occasion.getId());

        OccasionDto dto = occasionDtoMapper.toDto(occasion);

        int completedCount = (int) occasion.getActivities().stream()
                .filter(activity -> activity.getState() == ActivityState.COMPLETED)
                .count();
        int activeCount = (int) occasion.getActivities().stream()
                .filter(activity -> activity.getState() == ActivityState.PLANNED || activity.getState() == ActivityState.IN_PROGRESS)
                .count();
        int totalCount = occasion.getActivities().size();

        log.debug("Статистика активностей для мероприятия={}: завершено={}, активных={}, всего={}",
                occasion.getId(), completedCount, activeCount, totalCount);

        dto.setCompletedActivitiesCount(completedCount);
        dto.setActiveActivitiesCount(activeCount);
        dto.setTotalActivitiesCount(totalCount);

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
        log.debug("Сохранение перехода состояния мероприятия: id={}, новое состояние={}", occasion.getId(), state);
        occasion.setState(state);
        occasionRepository.save(occasion);
        log.debug("Переход состояния мероприятия сохранен: id={}, состояние={}", occasion.getId(), state);
    }

    @Override
    public boolean canTransition(OccasionEntity occasion, OccasionEvent event) {
        log.debug("Проверка возможности перехода мероприятия: id={}, событие={}, текущее состояние={}", 
                occasion.getId(), event, occasion.getState());
        // TODO: Implement business logic for occasion transitions
        boolean canTransition = true;
        log.debug("Результат проверки перехода мероприятия: id={}, может перейти={}", occasion.getId(), canTransition);
        return canTransition;
    }

    @Override
    public OccasionState getNextState(OccasionState currentState, OccasionEvent event) {
        log.debug("Определение следующего состояния мероприятия: текущее состояние={}, событие={}", currentState, event);
        OccasionState nextState = switch (currentState) {
            case DRAFT -> event == OccasionEvent.PLAN ? OccasionState.PLANNED : currentState;
            case PLANNED -> event == OccasionEvent.START ? OccasionState.IN_PROGRESS : currentState;
            case IN_PROGRESS -> event == OccasionEvent.COMPLETE ? OccasionState.COMPLETED : currentState;
            case COMPLETED -> currentState;
        };
        log.debug("Следующее состояние мероприятия: текущее={}, событие={}, следующее={}", currentState, event, nextState);
        return nextState;
    }

    @Override
    public boolean isValidTransition(OccasionState currentState, OccasionEvent event) {
        log.debug("Проверка валидности перехода мероприятия: текущее состояние={}, событие={}", currentState, event);
        boolean isValid = getNextState(currentState, event) != currentState;
        log.debug("Результат проверки валидности перехода: текущее состояние={}, событие={}, валиден={}", currentState, event, isValid);
        return isValid;
    }
}
