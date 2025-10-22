package org.bn.sensation.core.activityuser.service;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.activityuser.service.dto.CreateActivityUserRequest;
import org.bn.sensation.core.activityuser.service.dto.UpdateActivityUserRequest;
import org.bn.sensation.core.activityuser.service.dto.ActivityUserDto;
import org.bn.sensation.core.activityuser.service.mapper.CreateActivityUserRequestMapper;
import org.bn.sensation.core.activityuser.service.mapper.ActivityUserDtoMapper;
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
public class ActivityUserServiceImpl implements ActivityUserService {

    private final ActivityUserRepository activityUserRepository;
    private final ActivityUserDtoMapper activityUserDtoMapper;
    private final CreateActivityUserRequestMapper createActivityUserRequestMapper;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<ActivityUserEntity> getRepository() {
        return activityUserRepository;
    }

    @Override
    public BaseDtoMapper<ActivityUserEntity, ActivityUserDto> getMapper() {
        return activityUserDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityUserDto> findAll(Pageable pageable) {
        return activityUserRepository.findAll(pageable).map(activityUserDtoMapper::toDto);
    }

    @Override
    @Transactional
    public ActivityUserDto create(CreateActivityUserRequest request) {
        log.info("Создание назначения пользователя: пользователь={}, активность={}, роль={}",
                request.getUserId(), request.getActivityId(), request.getPosition());

        Preconditions.checkArgument(request.getUserId() != null, "User ID не может быть null");
        Preconditions.checkArgument(request.getActivityId() != null, "Activity ID не может быть null");
        Preconditions.checkArgument(request.getPosition() != null, "Role не может быть null");

        // Проверяем, что назначение еще не существует
        if (activityUserRepository.existsByUserIdAndActivityId(request.getUserId(), request.getActivityId())) {
            log.warn("Попытка создания дублирующего назначения: пользователь={}, активность={}",
                    request.getUserId(), request.getActivityId());
            throw new IllegalArgumentException("Пользователь уже назначен на эту активность");
        }

        // Проверяем существование пользователя
        UserEntity user = userRepository.getByIdOrThrow(request.getUserId());

        // Проверяем существование активности
        ActivityEntity activity = activityRepository.getByIdOrThrow(request.getActivityId());

        log.debug("Найдены пользователь={} и активность={} для создания назначения",
                user.getId(), activity.getId());

        // Бизнес-правило: только один главный судья на активность
        if (request.getPosition() == UserActivityPosition.JUDGE_CHIEF) {
            long chiefCount = activityUserRepository.countByActivityIdAndPosition(request.getActivityId(), UserActivityPosition.JUDGE_CHIEF);
            log.debug("Проверка главного судьи: найдено={} главных судей в активности={}", chiefCount, request.getActivityId());
            if (chiefCount > 0) {
                log.warn("Попытка назначить второго главного судью в активности={}", request.getActivityId());
                throw new IllegalArgumentException("В активности уже есть главный судья");
            }
        }

        // Создаем сущность назначения
        ActivityUserEntity assignment = createActivityUserRequestMapper.toEntity(request);
        assignment.setUser(user);
        assignment.setActivity(activity);

        ActivityUserEntity saved = activityUserRepository.save(assignment);
        log.info("Назначение пользователя успешно создано с id={}", saved.getId());
        return activityUserDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ActivityUserDto update(Long id, UpdateActivityUserRequest request) {
        Preconditions.checkArgument(id != null, "ID назначения не может быть null");

        ActivityUserEntity assignment = activityUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено с id: " + id));

        // Обновляем роль если указана
        if (request.getPosition() != null) {
            // Бизнес-правило: только один главный судья на активность
            if (request.getPosition() == UserActivityPosition.JUDGE_CHIEF) {
                long chiefCount = activityUserRepository.countByActivityIdAndPosition(assignment.getActivity().getId(), UserActivityPosition.JUDGE_CHIEF);
                if (chiefCount > 0 && !assignment.getPosition().equals(UserActivityPosition.JUDGE_CHIEF)) {
                    throw new IllegalArgumentException("В активности уже есть главный судья");
                }
            }
            assignment.setPosition(request.getPosition());
        }

        ActivityUserEntity saved = activityUserRepository.save(assignment);
        return activityUserDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!activityUserRepository.existsById(id)) {
            throw new IllegalArgumentException("Назначение не найдено с id: " + id);
        }
        activityUserRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityUserDto findByUserIdAndActivityId(Long userId, Long activityId) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");
        Preconditions.checkArgument(userId != null, "User ID не может быть null");
        return getByUserIdAndActivityId(userId, activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityUserDto findByActivityIdForCurrentUser(Long activityId) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");
        return getByUserIdAndActivityId(currentUser.getSecurityUser().getId(), activityId);
    }

    @Override
    public List<ActivityUserDto> findByOccasionIdForCurrentUser(Long occasionId) {
        Preconditions.checkArgument(occasionId != null, "Occasion ID не может быть null");
        Long userId = currentUser.getSecurityUser().getId();
        return activityUserRepository
                .findByUserIdAndOccasionId(userId, occasionId)
                .stream()
                .map(activityUserDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityUserDto> findByUserId(Long userId) {
        Preconditions.checkArgument(userId != null, "User ID не может быть null");

        return activityUserRepository.findByUserId(userId).stream()
                .map(activityUserDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityUserDto> findByActivityId(Long activityId) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");

        return activityUserRepository.findByActivityId(activityId).stream()
                .map(activityUserDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityUserDto> findByPosition(UserActivityPosition activityRole) {
        Preconditions.checkArgument(activityRole != null, "ActivityRole не может быть null");

        return activityUserRepository.findByPosition(activityRole).stream()
                .map(activityUserDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityUserDto> findByActivityIdAndPosition(Long activityId, UserActivityPosition activityRole) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");
        Preconditions.checkArgument(activityRole != null, "ActivityRole не может быть null");

        return activityUserRepository.findByActivityIdAndPosition(activityId, activityRole).stream()
                .map(activityUserDtoMapper::toDto)
                .toList();
    }

    private ActivityUserDto getByUserIdAndActivityId(Long userId, Long activityId) {
        ActivityUserEntity assignment = activityUserRepository.getByUserIdAndActivityIdOrThrow(userId, activityId);

        return activityUserDtoMapper.toDto(assignment);
    }
}
