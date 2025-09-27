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
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDtoMapper activityDtoMapper;
    private final CreateActivityRequestMapper createActivityRequestMapper;
    private final UpdateActivityRequestMapper updateActivityRequestMapper;
    private final OccasionRepository occasionRepository;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
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
        Preconditions.checkArgument(id != null, "ID мероприятия не может быть null");
        return activityRepository.findByOccasionIdAndUserIdAndStateIn(id, currentUser.getSecurityUser().getId(), State.LIFE_STATES).stream()
                .map(this::enrichActivityDtoWithStatistics)
                .toList();
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
        // Проверяем существование события
        OccasionEntity occasion = occasionRepository.findById(request.getOccasionId())
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено с id: " + request.getOccasionId()));

        // Создаем сущность активности
        ActivityEntity activity = createActivityRequestMapper.toEntity(request);
        activity.setOccasion(occasion);

        ActivityEntity saved = activityRepository.save(activity);
        return enrichActivityDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public ActivityDto update(Long id, UpdateActivityRequest request) {
        Preconditions.checkArgument(id != null, "ID активности не может быть null");

        ActivityEntity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Активность не найдена с id: " + id));

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

        ActivityEntity saved = activityRepository.save(activity);
        return enrichActivityDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new IllegalArgumentException("Активность не найдена с id: " + id);
        }

        // Сначала удаляем все связанные назначения пользователей
        userActivityAssignmentRepository.deleteByActivityId(id);
        // Затем удаляем саму активность
        activityRepository.deleteById(id);
    }

    /**
     * Обогащает ActivityDto статистикой по этапам
     */
    private ActivityDto enrichActivityDtoWithStatistics(ActivityEntity activity) {
        ActivityDto dto = activityDtoMapper.toDto(activity);
        dto.setCompletedMilestonesCount((int) activity.getMilestones()
                .stream()
                .filter(ms -> ms.getState() == State.COMPLETED)
                .count());
        dto.setTotalMilestonesCount(activity.getMilestones().size());
        return dto;
    }
}
