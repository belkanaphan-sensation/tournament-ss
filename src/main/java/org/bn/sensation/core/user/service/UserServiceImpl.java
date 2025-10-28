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
        log.debug("Поиск пользователя по id={}", id);
        Optional<UserDto> result = UserService.super.findById(id)
                .map(userDto -> {
                    userDto.setPassword(passwordEncoder.decrypt(userDto.getPassword()));
                    return userDto;
                });
        if (result.isPresent()) {
            log.debug("Пользователь найден: id={}, username={}", id, result.get().getUsername());
        } else {
            log.debug("Пользователь не найден: id={}", id);
        }
        return result;
    }

    @Override
    public Optional<UserDto> getCurrentUser() {
        log.debug("Получение текущего пользователя: id={}", currentUser.getSecurityUser().getId());
        Optional<UserDto> result = Optional.of(userDtoMapper.toDto(currentUser.getSecurityUser()));
        log.debug("Текущий пользователь: id={}, username={}", result.get().getId(), result.get().getUsername());
        return result;
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, UserDetails userDetails) {
        log.info("Смена пароля для пользователя={}", userDetails.getUsername());

        if (!passwordEncoder.matches(request.currentPassword(), userDetails.getPassword())) {
            log.warn("Неверный текущий пароль для пользователя={}", userDetails.getUsername());
            throw new IllegalArgumentException("Неверный пароль");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            log.warn("Пароли не совпадают для пользователя={}", userDetails.getUsername());
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
        log.info("Пароль успешно изменен для пользователя={}", userDetails.getUsername());
    }

    @Override
    @Async
    public void sendEmail(ForgotPasswordRequest request) {
        log.info("Запрос восстановления пароля: username={}, email={}", request.username(), request.email());

        if ((request.username() == null || request.username().isBlank())
                && (request.email() == null || request.email().isBlank())) {
            log.warn("Не указаны имя пользователя или email для восстановления пароля");
            throw new IllegalArgumentException("Необходимо указать имя пользователя или email");
        }

        UserEntity user = null;
        if (request.username() != null && !request.username().isBlank()) {
            log.debug("Поиск пользователя по username={}", request.username());
            user = userRepository.findByUsername(request.username()).orElse(null);
        }
        if (user == null && request.email() != null && !request.email().isBlank()) {
            log.debug("Поиск пользователя по email={}", request.email());
            user = userRepository.findByPersonEmail(request.email()).orElse(null);
        }
        if (user == null) {
            log.warn("Пользователь не найден по username={} или email={}", request.username(), request.email());
            throw new EntityNotFoundException("Пользователь не найден по указанному имени пользователя или email");
        }

        log.debug("Найден пользователь={} для восстановления пароля", user.getId());
        String tempPassword = generateTempPassword(14);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // Здесь должен быть вызов реального почтового сервиса.
        // Пока логируем, чтобы не ломать окружение.
        log.info("Восстановление пароля инициировано для пользователя '{}'. Временный пароль: '{}'. Email: {}",
                user.getUsername(), tempPassword, user.getPerson() != null ? user.getPerson().getEmail() : null);
    }

    @Override
    @Transactional
    public UserDto register(RegistrationRequest request) {
        log.info("Регистрация нового пользователя: username={}, email={}", request.username(), request.email());

        userRepository.findByUsername(request.username())
                .ifPresent(u -> {
                    log.warn("Попытка регистрации с занятым username={}", request.username());
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
        log.info("Пользователь успешно зарегистрирован с id={}, username={}", saved.getId(), saved.getUsername());
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
        log.debug("Поиск всех пользователей с пагинацией: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserDto> result = userRepository.findAll(pageable).map(userDtoMapper::toDto);
        log.debug("Найдено {} пользователей на странице", result.getContent().size());
        return result;
    }

    @Override
    @Transactional
    public UserDto create(CreateUserRequest request) {
        log.info("Создание пользователя: username={}, email={}, роли={}",
                request.getUsername(), request.getEmail(), request.getRoles());

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    log.warn("Попытка создания пользователя с занятым username={}", request.getUsername());
                    throw new IllegalArgumentException("Имя пользователя уже занято: " + request.getUsername());
                });

        validateEmailUniqueness(request.getEmail(), null);
        validatePhoneNumberUniqueness(request.getPhoneNumber(), null);

        Set<Role> roles = request.getRoles() != null ? request.getRoles() : Set.of(Role.USER);
        log.debug("Установлены роли для пользователя: {}", roles);

        UserEntity user = createUserRequestMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);

        assignUserToOrganizations(user, request.getOrganizationIds());

        UserEntity saved = userRepository.save(user);
        log.info("Пользователь успешно создан с id={}, username={}", saved.getId(), saved.getUsername());
        return userDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        log.info("Обновление пользователя: id={}, имя={}", id, request.getName());
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + id));

        log.debug("Найден пользователь={} для обновления", user.getId());

        if(request.getName()!=null) {
            Preconditions.checkArgument(!request.getName().trim().isEmpty(), "Имя пользователя не может быть пустым");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getPerson().getEmail())) {
            log.debug("Проверка уникальности email={} для пользователя={}", request.getEmail(), user.getId());
            validateEmailUniqueness(request.getEmail(), user.getId());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(user.getPerson().getPhoneNumber())) {
            log.debug("Проверка уникальности номера телефона={} для пользователя={}", request.getPhoneNumber(), user.getId());
            validatePhoneNumberUniqueness(request.getPhoneNumber(), user.getId());
        }

        updateUserRequestMapper.updateUserFromRequest(request, user);

        UserEntity saved = userRepository.save(user);
        log.info("Пользователь успешно обновлен: id={}", saved.getId());
        return userDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление пользователя: id={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующего пользователя: id={}", id);
            throw new EntityNotFoundException("Пользователь не найден с id: " + id);
        }
        userRepository.deleteById(id);
        log.info("Пользователь успешно удален: id={}", id);
    }

    @Override
    @Transactional
    public UserDto assignUserToOrganization(Long userId, Long organizationId) {
        log.info("Назначение пользователя на организацию: пользователь={}, организация={}", userId, organizationId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        OrganizationEntity org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Организация не найдена"));

        log.debug("Найдены пользователь={} и организация={} для назначения", user.getId(), org.getId());
        user.getOrganizations().add(org);
        UserEntity saved = userRepository.save(user);
        log.info("Пользователь успешно назначен на организацию: пользователь={}, организация={}", saved.getId(), org.getId());
        return userDtoMapper.toDto(saved);
    }

    private static String generateTempPassword(int length) {
        log.debug("Генерация временного пароля длиной={} символов", length);
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        String password = sb.toString();
        log.debug("Сгенерирован временный пароль длиной={}", password.length());
        return password;
    }

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

    private OrganizationEntity findOrganizationById(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Организация не найдена с id: " + organizationId));
    }

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
