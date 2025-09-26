package org.bn.sensation.core.user.service;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.entity.UserActivityPosition;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.user.service.dto.CreateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UpdateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UserActivityAssignmentDto;
import org.bn.sensation.core.user.service.mapper.CreateUserActivityAssignmentRequestMapper;
import org.bn.sensation.core.user.service.mapper.UserActivityAssignmentDtoMapper;
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
public class UserActivityAssignmentServiceImpl implements UserActivityAssignmentService {

    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
    private final UserActivityAssignmentDtoMapper userActivityAssignmentDtoMapper;
    private final CreateUserActivityAssignmentRequestMapper createUserActivityAssignmentRequestMapper;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<UserActivityAssignmentEntity> getRepository() {
        return userActivityAssignmentRepository;
    }

    @Override
    public BaseDtoMapper<UserActivityAssignmentEntity, UserActivityAssignmentDto> getMapper() {
        return userActivityAssignmentDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserActivityAssignmentDto> findAll(Pageable pageable) {
        return userActivityAssignmentRepository.findAll(pageable).map(userActivityAssignmentDtoMapper::toDto);
    }

    @Override
    @Transactional
    public UserActivityAssignmentDto create(CreateUserActivityAssignmentRequest request) {
        Preconditions.checkArgument(request.getUserId() != null, "User ID не может быть null");
        Preconditions.checkArgument(request.getActivityId() != null, "Activity ID не может быть null");
        Preconditions.checkArgument(request.getPosition() != null, "Role не может быть null");

        // Проверяем, что назначение еще не существует
        if (userActivityAssignmentRepository.existsByUserIdAndActivityId(request.getUserId(), request.getActivityId())) {
            throw new IllegalArgumentException("Пользователь уже назначен на эту активность");
        }

        // Проверяем существование пользователя
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + request.getUserId()));

        // Проверяем существование активности
        ActivityEntity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new EntityNotFoundException("Активность не найдена с id: " + request.getActivityId()));

        // Бизнес-правило: только один главный судья на активность
        if (request.getPosition() == UserActivityPosition.JUDGE_CHIEF) {
            long chiefCount = userActivityAssignmentRepository.countByActivityIdAndPosition(request.getActivityId(), UserActivityPosition.JUDGE_CHIEF);
            if (chiefCount > 0) {
                throw new IllegalArgumentException("В активности уже есть главный судья");
            }
        }

        // Создаем сущность назначения
        UserActivityAssignmentEntity assignment = createUserActivityAssignmentRequestMapper.toEntity(request);
        assignment.setUser(user);
        assignment.setActivity(activity);

        UserActivityAssignmentEntity saved = userActivityAssignmentRepository.save(assignment);
        return userActivityAssignmentDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserActivityAssignmentDto update(Long id, UpdateUserActivityAssignmentRequest request) {
        Preconditions.checkArgument(id != null, "ID назначения не может быть null");

        UserActivityAssignmentEntity assignment = userActivityAssignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено с id: " + id));

        // Обновляем роль если указана
        if (request.getPosition() != null) {
            // Бизнес-правило: только один главный судья на активность
            if (request.getPosition() == UserActivityPosition.JUDGE_CHIEF) {
                long chiefCount = userActivityAssignmentRepository.countByActivityIdAndPosition(assignment.getActivity().getId(), UserActivityPosition.JUDGE_CHIEF);
                if (chiefCount > 0 && !assignment.getPosition().equals(UserActivityPosition.JUDGE_CHIEF)) {
                    throw new IllegalArgumentException("В активности уже есть главный судья");
                }
            }
            assignment.setPosition(request.getPosition());
        }

        UserActivityAssignmentEntity saved = userActivityAssignmentRepository.save(assignment);
        return userActivityAssignmentDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userActivityAssignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Назначение не найдено с id: " + id);
        }
        userActivityAssignmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserActivityAssignmentDto findByUserIdAndActivityId(Long userId, Long activityId) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");
        Preconditions.checkArgument(userId != null, "User ID не может быть null");
        return getByUserIdAndActivityId(userId, activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserActivityAssignmentDto findByActivityIdForCurrentUser(Long activityId) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");
        return getByUserIdAndActivityId(currentUser.getSecurityUser().getId(), activityId);
    }

    @Override
    public List<UserActivityAssignmentDto> findByOccasionIdForCurrentUser(Long occasionId) {
        Preconditions.checkArgument(occasionId != null, "Occasion ID не может быть null");
        Long userId = currentUser.getSecurityUser().getId();
        return userActivityAssignmentRepository
                .findByUserIdAndOccasionId(userId, occasionId)
                .stream()
                .map(userActivityAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityAssignmentDto> findByUserId(Long userId) {
        Preconditions.checkArgument(userId != null, "User ID не может быть null");

        return userActivityAssignmentRepository.findByUserId(userId).stream()
                .map(userActivityAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityAssignmentDto> findByActivityId(Long activityId) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");

        return userActivityAssignmentRepository.findByActivityId(activityId).stream()
                .map(userActivityAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityAssignmentDto> findByPosition(UserActivityPosition activityRole) {
        Preconditions.checkArgument(activityRole != null, "ActivityRole не может быть null");

        return userActivityAssignmentRepository.findByPosition(activityRole).stream()
                .map(userActivityAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityAssignmentDto> findByActivityIdAndPosition(Long activityId, UserActivityPosition activityRole) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");
        Preconditions.checkArgument(activityRole != null, "ActivityRole не может быть null");

        return userActivityAssignmentRepository.findByActivityIdAndPosition(activityId, activityRole).stream()
                .map(userActivityAssignmentDtoMapper::toDto)
                .toList();
    }

    private UserActivityAssignmentDto getByUserIdAndActivityId(Long userId, Long activityId) {
        UserActivityAssignmentEntity assignment = userActivityAssignmentRepository.findByUserIdAndActivityId(userId, activityId)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено для пользователя " + userId + " и активности " + activityId));

        return userActivityAssignmentDtoMapper.toDto(assignment);
    }

}
