package org.bn.sensation.core.user.service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.user.service.dto.*;
import org.bn.sensation.core.user.service.mapper.CreateUserRequestMapper;
import org.bn.sensation.core.user.service.mapper.UpdateUserRequestMapper;
import org.bn.sensation.core.user.service.mapper.UserDtoMapper;
import org.bn.sensation.security.AesPasswordEncoder;
import org.bn.sensation.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final static String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*";

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserDtoMapper userDtoMapper;
    private final CreateUserRequestMapper createUserRequestMapper;
    private final UpdateUserRequestMapper updateUserRequestMapper;
    private final CurrentUser currentUser;

    //    private final PasswordEncoder passwordEncoder;
    private final AesPasswordEncoder passwordEncoder;

    //todo: Удалить после перехода на нормальный продовый энкодер паролья
    @Override
    public Optional<UserDto> findById(Long id) {
        return UserService.super.findById(id)
                .map(userDto -> {
                    userDto.setPassword(passwordEncoder.decrypt(userDto.getPassword()));
                    return userDto;
                });
    }

    @Override
    public Optional<UserDto> getCurrentUser() {
        return Optional.of(userDtoMapper.toDto(currentUser.getSecurityUser()));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, UserDetails userDetails) {
        if (!passwordEncoder.matches(request.currentPassword(), userDetails.getPassword())) {
            throw new IllegalArgumentException("Неверный пароль");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }
        UserEntity user =
                userRepository
                        .findByUsername(userDetails.getUsername())
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "User with username not found: " + userDetails.getUsername()));
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
    }

    @Override
    @Async
    public void sendEmail(ForgotPasswordRequest request) {
        if ((request.username() == null || request.username().isBlank())
                && (request.email() == null || request.email().isBlank())) {
            throw new IllegalArgumentException("Необходимо указать имя пользователя или email");
        }

        UserEntity user = null;
        if (request.username() != null && !request.username().isBlank()) {
            user = userRepository.findByUsername(request.username()).orElse(null);
        }
        if (user == null && request.email() != null && !request.email().isBlank()) {
            user = userRepository.findByPersonEmail(request.email()).orElse(null);
        }
        if (user == null) {
            throw new EntityNotFoundException("Пользователь не найден по указанному имени пользователя или email");
        }

        String tempPassword = generateTempPassword(14);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // Здесь должен быть вызов реального почтового сервиса.
        // Пока логируем, чтобы не ломать окружение.
        log.info("Password recovery initiated for user '{}'. Temporary password: '{}'. Email: {}",
                user.getUsername(), tempPassword, user.getPerson() != null ? user.getPerson().getEmail() : null);
    }

    @Override
    @Transactional
    public UserDto register(RegistrationRequest request) {
        userRepository.findByUsername(request.username())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Имя пользователя уже занято: " + request.username());
                });
        validateEmailUniqueness(request.email(), null);
        validatePhoneNumberUniqueness(request.phoneNumber(), null);

        UserEntity user = UserEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .person(Person.builder()
                        .name(request.name())
                        .surname(request.surname())
                        .secondName(request.secondName())
                        .email(request.email())
                        .phoneNumber(request.phoneNumber())
                        .build())
                .build();

        UserEntity saved = userRepository.save(user);
        return userDtoMapper.toDto(saved);
    }

    @Override
    public BaseRepository<UserEntity> getRepository() {
        return userRepository;
    }

    @Override
    public BaseDtoMapper<UserEntity, UserDto> getMapper() {
        return userDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userDtoMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto create(CreateUserRequest request) {
        // Проверяем, существует ли имя пользователя уже
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Имя пользователя уже занято: " + request.getUsername());
                });

        validateEmailUniqueness(request.getEmail(), null);
        validatePhoneNumberUniqueness(request.getPhoneNumber(), null);

        // Получаем роли напрямую из запроса
        Set<Role> roles = request.getRoles() != null ? request.getRoles() : Set.of(Role.USER);

        // Создаем сущность пользователя
        UserEntity user = createUserRequestMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);

        // Назначаем пользователя в организации
        assignUserToOrganizations(user, request.getOrganizationIds());

        UserEntity saved = userRepository.save(user);
        return userDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + id));


        if(request.getName()!=null) {
            Preconditions.checkArgument(!request.getName().trim().isEmpty(), "Имя пользователя не может быть пустым");
        }

        // Проверяем, существует ли email уже (если изменился)
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getPerson().getEmail())) {
            validateEmailUniqueness(request.getEmail(), user.getId());
        }

        // Проверяем, существует ли номер телефона уже (если изменился)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(user.getPerson().getPhoneNumber())) {
            validatePhoneNumberUniqueness(request.getPhoneNumber(), user.getId());
        }

        // Обновляем поля пользователя
        updateUserRequestMapper.updateUserFromRequest(request, user);

        // Обновляем роли
        if (request.getRoles() != null) {
            user.setRoles(new HashSet<>(request.getRoles()));
        }

        // Обновляем организации
        if (request.getOrganizationIds() != null) {
            assignUserToOrganizations(user, request.getOrganizationIds());
        }

        UserEntity saved = userRepository.save(user);
        return userDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Пользователь не найден с id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDto assignUserToOrganization(Long userId, Long organizationId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        OrganizationEntity org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Организация не найдена"));

        user.getOrganizations().add(org);
        return userDtoMapper.toDto(userRepository.save(user));
    }

    private static String generateTempPassword(int length) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    /**
     * Проверяет, не используется ли email другим пользователем
     *
     * @param email         проверяемый email
     * @param excludeUserId ID пользователя, который исключается из проверки (для обновления)
     */
    private void validateEmailUniqueness(String email, Long excludeUserId) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByPersonEmail(email)
                .ifPresent(user -> {
                    if (excludeUserId == null || !user.getId().equals(excludeUserId)) {
                        throw new IllegalArgumentException("Email уже используется: " + email);
                    }
                });
    }

    /**
     * Проверяет, не используется ли номер телефона другим пользователем
     *
     * @param phoneNumber   проверяемый номер телефона
     * @param excludeUserId ID пользователя, который исключается из проверки (для обновления)
     */
    private void validatePhoneNumberUniqueness(String phoneNumber, Long excludeUserId) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }

        userRepository.findByPersonPhoneNumber(phoneNumber)
                .ifPresent(user -> {
                    if (excludeUserId == null || !user.getId().equals(excludeUserId)) {
                        throw new IllegalArgumentException("Номер телефона уже используется: " + phoneNumber);
                    }
                });
    }

    /**
     * Находит организацию по ID и проверяет её существование
     *
     * @param organizationId ID организации
     * @return OrganizationEntity
     * @throws EntityNotFoundException если организация не найдена
     */
    private OrganizationEntity findOrganizationById(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Организация не найдена с id: " + organizationId));
    }

    /**
     * Привязывает пользователя к организациям
     *
     * @param user            пользователь
     * @param organizationIds ID организаций
     */
    private void assignUserToOrganizations(UserEntity user, Set<Long> organizationIds) {
        if (organizationIds == null) {
            return;
        }

        if (organizationIds.isEmpty()) {
            user.setOrganizations(new HashSet<>());
            return;
        }

        Set<OrganizationEntity> organizations = organizationIds.stream()
                .map(this::findOrganizationById)
                .collect(Collectors.toSet());

        user.setOrganizations(new HashSet<>(organizations));
    }
}
