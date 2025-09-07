package org.bn.sensation.core.user.service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.role.entity.Role;
import org.bn.sensation.core.role.entity.RoleEntity;
import org.bn.sensation.core.role.repository.RoleRepository;
import org.bn.sensation.core.user.entity.Status;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.user.service.dto.*;
import org.bn.sensation.core.user.service.mapper.UserDtoMapper;
import org.bn.sensation.core.user.service.mapper.CreateUserRequestMapper;
import org.bn.sensation.core.user.service.mapper.UpdateUserRequestMapper;
import org.bn.sensation.security.AesPasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final static String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final UserDtoMapper userDtoMapper;
    private final CreateUserRequestMapper createUserRequestMapper;
    private final UpdateUserRequestMapper updateUserRequestMapper;

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
    @Transactional
    public void changePassword(ChangePasswordRequest request, UserDetails userDetails) {
        if (!passwordEncoder.matches(request.currentPassword(), userDetails.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Password are not the same");
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
            throw new IllegalArgumentException("Username or email must be provided");
        }

        UserEntity user = null;
        if (request.username() != null && !request.username().isBlank()) {
            user = userRepository.findByUsername(request.username()).orElse(null);
        }
        if (user == null && request.email() != null && !request.email().isBlank()) {
            user = userRepository.findByPersonEmail(request.email()).orElse(null);
        }
        if (user == null) {
            throw new EntityNotFoundException("User not found by provided username or email");
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
                    throw new IllegalArgumentException("Username already taken: " + request.username());
                });
        if (request.email() != null && !request.email().isBlank()) {
            userRepository.findByPersonEmail(request.email())
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Email already in use: " + request.email());
                    });
        }

        //todo изменить механизм выставления ролей. Вероятно рассмотреть кэш
        RoleEntity byRole = roleRepository.findByRole(Role.SUPERADMIN);
        UserEntity user = UserEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .status(Status.ACTIVE)
                .roles(Set.of(byRole))
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
        // Check if username already exists
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Username already taken: " + request.getUsername());
                });

        // Check if email already exists
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            userRepository.findByPersonEmail(request.getEmail())
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Email already in use: " + request.getEmail());
                    });
        }

        // Get roles
        Set<RoleEntity> roles = findRolesByIds(request.getRoleIds());

        // Create user entity
        UserEntity user = createUserRequestMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);

        UserEntity saved = userRepository.save(user);
        return userDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Check if email already exists (if changed)
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getPerson().getEmail())) {
            userRepository.findByPersonEmail(request.getEmail())
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Email already in use: " + request.getEmail());
                    });
        }

        // Update user fields
        updateUserRequestMapper.updateUserFromRequest(request, user);

        // Update roles
        if (request.getRoleIds() != null) {
            Set<RoleEntity> roles = findRolesByIds(request.getRoleIds());
            user.setRoles(roles);
        }

        UserEntity saved = userRepository.save(user);
        return userDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDto assignUserToOrganization(Long userId, Long organizationId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        OrganizationEntity org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

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

    private Set<RoleEntity> findRolesByIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
    }
}
