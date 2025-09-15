package org.bn.sensation.core.user.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.entity.UserActivityRole;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.user.service.dto.CreateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UpdateUserActivityAssignmentRequest;
import org.bn.sensation.core.user.service.dto.UserActivityAssignmentDto;
import org.bn.sensation.core.user.service.mapper.CreateUserActivityAssignmentRequestMapper;
import org.bn.sensation.core.user.service.mapper.UpdateUserActivityAssignmentRequestMapper;
import org.bn.sensation.core.user.service.mapper.UserActivityAssignmentDtoMapper;
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
    private final UpdateUserActivityAssignmentRequestMapper updateUserActivityAssignmentRequestMapper;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

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
        Preconditions.checkArgument(request.getRole() != null, "Role не может быть null");

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
        if (request.getRole() == UserActivityRole.JUDGE_CHIEF) {
            long chiefCount = userActivityAssignmentRepository.countByActivityIdAndRole(request.getActivityId(), UserActivityRole.JUDGE_CHIEF);
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

        // Обновляем пользователя если указан
        if (request.getUserId() != null) {
            UserEntity user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + request.getUserId()));
            assignment.setUser(user);
        }

        // Обновляем активность если указана
        if (request.getActivityId() != null) {
            ActivityEntity activity = activityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new EntityNotFoundException("Активность не найдена с id: " + request.getActivityId()));
            assignment.setActivity(activity);
        }

        // Обновляем роль если указана
        if (request.getRole() != null) {
            // Бизнес-правило: только один главный судья на активность
            if (request.getRole() == UserActivityRole.JUDGE_CHIEF) {
                long chiefCount = userActivityAssignmentRepository.countByActivityIdAndRole(assignment.getActivity().getId(), UserActivityRole.JUDGE_CHIEF);
                if (chiefCount > 0 && !assignment.getActivityRole().equals(UserActivityRole.JUDGE_CHIEF)) {
                    throw new IllegalArgumentException("В активности уже есть главный судья");
                }
            }
            assignment.setActivityRole(request.getRole());
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
        Preconditions.checkArgument(userId != null, "User ID не может быть null");
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");

        UserActivityAssignmentEntity assignment = userActivityAssignmentRepository.findByUserIdAndActivityId(userId, activityId)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено для пользователя " + userId + " и активности " + activityId));

        return userActivityAssignmentDtoMapper.toDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserActivityAssignmentDto> findByUserId(Long userId, Pageable pageable) {
        Preconditions.checkArgument(userId != null, "User ID не может быть null");

        return userActivityAssignmentRepository.findByUserId(userId, pageable).map(userActivityAssignmentDtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserActivityAssignmentDto> findByActivityId(Long activityId, Pageable pageable) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");

        return userActivityAssignmentRepository.findByActivityId(activityId, pageable).map(userActivityAssignmentDtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserActivityAssignmentDto> findByRole(UserActivityRole role, Pageable pageable) {
        Preconditions.checkArgument(role != null, "Role не может быть null");

        return userActivityAssignmentRepository.findByRole(role, pageable).map(userActivityAssignmentDtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserActivityAssignmentDto> findByActivityIdAndRole(Long activityId, UserActivityRole role, Pageable pageable) {
        Preconditions.checkArgument(activityId != null, "Activity ID не может быть null");
        Preconditions.checkArgument(role != null, "Role не может быть null");

        return userActivityAssignmentRepository.findByActivityIdAndRole(activityId, role, pageable).map(userActivityAssignmentDtoMapper::toDto);
    }
}
