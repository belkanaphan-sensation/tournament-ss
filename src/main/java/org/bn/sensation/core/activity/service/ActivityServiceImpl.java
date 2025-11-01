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
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
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
    private final ActivityStateMachineService activityStateMachineService;

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
        log.debug("Поиск всех активностей с пагинацией: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ActivityDto> result = activityRepository.findAll(pageable).map(this::enrichActivityDtoWithStatistics);
        log.debug("Найдено {} активностей на странице", result.getContent().size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDto> findByOccasionId(Long id) {
        log.debug("Поиск активностей для мероприятия={}", id);
        Preconditions.checkArgument(id != null, "ID мероприятия не может быть null");
        List<ActivityDto> result = activityRepository.findByOccasionId(id).stream()
                .map(this::enrichActivityDtoWithStatistics)
                .toList();
        log.debug("Найдено {} активностей для мероприятия={}", result.size(), id);
        return result;
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
        log.debug("Поиск активности по id={}", id);
        Optional<ActivityDto> result = activityRepository.findById(id)
                .map(this::enrichActivityDtoWithStatistics);
        if (result.isPresent()) {
            log.debug("Активность найдена: id={}, название={}", id, result.get().getName());
        } else {
            log.debug("Активность не найдена: id={}", id);
        }
        return result;
    }

    @Override
    @Transactional
    public ActivityDto create(CreateActivityRequest request) {
        log.info("Создание активности: название={}, мероприятие={}", request.getName(), request.getOccasionId());
        OccasionEntity occasion = occasionRepository.getByIdOrThrow(request.getOccasionId());

        log.debug("Найдено мероприятие={} для создания активности", occasion.getId());
        ActivityEntity activity = createActivityRequestMapper.toEntity(request);
        activity.setState(ActivityState.DRAFT);
        activity.setOccasion(occasion);

        ActivityEntity saved = activityRepository.save(activity);
        log.info("Активность успешно создана с id={}", saved.getId());
        return enrichActivityDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public ActivityDto update(Long id, UpdateActivityRequest request) {
        log.info("Обновление активности: id={}, название={}", id, request.getName());
        Preconditions.checkArgument(id != null, "ID активности не может быть null");

        ActivityEntity activity = activityRepository.getByIdOrThrow(id);
        log.debug("Найдена активность={} для обновления", activity.getId());

        // Обновляем поля активности
        updateActivityRequestMapper.updateActivityFromRequest(request, activity);

        // Обновляем адрес
        if (request.getAddress() != null) {
            log.debug("Обновление адреса для активности={}", activity.getId());
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
        log.info("Активность успешно обновлена: id={}", saved.getId());
        return enrichActivityDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление активности: id={}", id);

        // Проверяем существование активности без загрузки связанных объектов
        if (!activityRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующей активности: id={}", id);
            throw new EntityNotFoundException("Активность не найдена с id: " + id);
        }

        log.debug("Удаление связанных назначений пользователей для активности={}", id);
        // Сначала удаляем все связанные назначения пользователей
        activityUserRepository.deleteByActivityId(id);

        log.debug("Удаление активности={}", id);
        // Затем удаляем саму активность
        activityRepository.deleteById(id);
        log.info("Активность успешно удалена: id={}", id);
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
    @Transactional
    public void draftActivity(Long id) {
        log.info("Перевод активности в черновик: id={}", id);
        ActivityEntity activity = activityRepository.getByIdOrThrow(id);
        activityStateMachineService.sendEvent(activity, ActivityEvent.DRAFT);
        log.info("Активность переведена в черновик: id={}", id);
    }

    @Override
    @Transactional
    public void planActivity(Long id) {
        log.info("Планирование активности: id={}", id);
        ActivityEntity activity = activityRepository.getByIdOrThrow(id);
        activityStateMachineService.sendEvent(activity, ActivityEvent.PLAN);
        log.info("Активность запланирована: id={}", id);
    }

    @Override
    @Transactional
    public void startActivity(Long id) {
        log.info("Запуск активности: id={}", id);
        ActivityEntity activity = activityRepository.getByIdOrThrow(id);
        activityStateMachineService.sendEvent(activity, ActivityEvent.START);
        log.info("Активность запущена: id={}", id);
    }

    @Override
    @Transactional
    public void closeRegistrationToActivity(Long id) {
        log.info("Закрытие регистрации в активность: id={}", id);
        ActivityEntity activity = activityRepository.getByIdOrThrow(id);
        activityStateMachineService.sendEvent(activity, ActivityEvent.CLOSE_REGISTRATION);
        log.info("Регистрация в активность закрыта: id={}", id);
    }

    @Override
    @Transactional
    public void completeActivity(Long id) {
        log.info("Завершение активности: id={}", id);
        ActivityEntity activity = activityRepository.getByIdOrThrow(id);
        activityStateMachineService.sendEvent(activity, ActivityEvent.COMPLETE);
        log.info("Активность завершена: id={}", id);
    }
}
