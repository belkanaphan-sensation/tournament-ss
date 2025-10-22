package org.bn.sensation.core.activity.service;

import java.util.List;
import java.util.Optional;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.activity.service.mapper.ActivityDtoMapper;
import org.bn.sensation.core.activity.service.mapper.CreateActivityRequestMapper;
import org.bn.sensation.core.activity.service.mapper.UpdateActivityRequestMapper;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDtoMapper activityDtoMapper;
    private final CreateActivityRequestMapper createActivityRequestMapper;
    private final UpdateActivityRequestMapper updateActivityRequestMapper;
    private final OccasionRepository occasionRepository;
    private final ActivityUserRepository activityUserRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<ActivityEntity> getRepository() {
        return activityRepository;
    }

    @Override
    public BaseDtoMapper<ActivityEntity, ActivityDto> getMapper() {
        return activityDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityDto> findAll(Pageable pageable) {
        return activityRepository.findAll(pageable).map(this::enrichActivityDtoWithStatistics);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDto> findByOccasionId(Long id) {
        Preconditions.checkArgument(id != null, "ID мероприятия не может быть null");
        return activityRepository.findByOccasionId(id).stream()
                .map(this::enrichActivityDtoWithStatistics)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDto> findByOccasionIdInLifeStatesForCurrentUser(Long id) {
        log.info("Поиск активностей в жизненных состояниях для мероприятия={}, пользователь={}",
                id, currentUser.getSecurityUser().getId());

        Preconditions.checkArgument(id != null, "ID мероприятия не может быть null");
        List<ActivityDto> result = activityRepository.findByOccasionIdAndUserIdAndStateIn(
                id, currentUser.getSecurityUser().getId(), ActivityState.LIFE_ACTIVITY_STATES).stream()
                .map(this::enrichActivityDtoWithStatistics)
                .toList();

        log.debug("Найдено {} активностей в жизненных состояниях для мероприятия={}", result.size(), id);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActivityDto> findById(Long id) {
        return activityRepository.findById(id)
                .map(this::enrichActivityDtoWithStatistics);
    }

    @Override
    @Transactional
    public ActivityDto create(CreateActivityRequest request) {
        log.info("Создание активности: название={}, мероприятие={}", request.getName(), request.getOccasionId());
        OccasionEntity occasion = occasionRepository.getByIdOrThrow(request.getOccasionId());

        log.debug("Найдено мероприятие={} для создания активности", occasion.getId());
        ActivityEntity activity = createActivityRequestMapper.toEntity(request);
        activity.setOccasion(occasion);

        ActivityEntity saved = activityRepository.save(activity);
        log.info("Активность успешно создана с id={}", saved.getId());
        return enrichActivityDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public ActivityDto update(Long id, UpdateActivityRequest request) {
        Preconditions.checkArgument(id != null, "ID активности не может быть null");

        ActivityEntity activity = activityRepository.getByIdOrThrow(id);

        // Обновляем поля активности
        updateActivityRequestMapper.updateActivityFromRequest(request, activity);

        // Обновляем адрес
        if (request.getAddress() != null) {
            Address address = activity.getAddress();
            if (address == null) {
                address = Address.builder().build();
            }
            updateActivityRequestMapper.updateAddressFromRequest(request.getAddress(), address);
        }

        // Обновляем событие (если метод getOccasionId() существует в UpdateActivityRequest)
        // if (request.getOccasionId() != null) {
        //     OccasionEntity occasion = occasionRepository.findById(request.getOccasionId())
        //             .orElseThrow(() -> new EntityNotFoundException("Событие не найдено с id: " + request.getOccasionId()));
        //     activity.setOccasion(occasion);
        // }

        ActivityEntity saved = activityRepository.save(activity);
        return enrichActivityDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // Проверяем существование активности без загрузки связанных объектов
        if (!activityRepository.existsById(id)) {
            throw new EntityNotFoundException("Активность не найдена с id: " + id);
        }

        // Сначала удаляем все связанные назначения пользователей
        activityUserRepository.deleteByActivityId(id);

        // Затем удаляем саму активность
        activityRepository.deleteById(id);
    }

    /**
     * Обогащает ActivityDto статистикой по этапам
     */
    private ActivityDto enrichActivityDtoWithStatistics(ActivityEntity activity) {
        log.debug("Обогащение статистикой активности={}", activity.getId());

        ActivityDto dto = activityDtoMapper.toDto(activity);

        int completedCount = (int) activity.getMilestones()
                .stream()
                .filter(ms -> ms.getState() == MilestoneState.COMPLETED)
                .count();
        int totalCount = activity.getMilestones().size();

        log.debug("Статистика этапов для активности={}: завершено={}, всего={}",
                activity.getId(), completedCount, totalCount);

        dto.setCompletedMilestonesCount(completedCount);
        dto.setTotalMilestonesCount(totalCount);
        return dto;
    }

    @Override
    public void saveTransition(ActivityEntity activity, ActivityState state) {
        activity.setState(state);
        activityRepository.save(activity);
    }

    @Override
    public boolean canTransition(ActivityEntity activity, ActivityEvent event) {
        // TODO: Implement business logic for activity transitions
        return true;
    }

    @Override
    public ActivityState getNextState(ActivityState currentState, ActivityEvent event) {
        return switch (currentState) {
            case DRAFT -> event == ActivityEvent.PLAN ? ActivityState.PLANNED : currentState;
            case PLANNED, COMPLETED -> event == ActivityEvent.START ? ActivityState.IN_PROGRESS : currentState;
            case IN_PROGRESS -> event == ActivityEvent.COMPLETE ? ActivityState.COMPLETED : currentState;
            default -> currentState;
        };
    }

    @Override
    public boolean isValidTransition(ActivityState currentState, ActivityEvent event) {
        return getNextState(currentState, event) != currentState;
    }
}
