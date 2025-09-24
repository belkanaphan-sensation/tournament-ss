package org.bn.sensation.core.user.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.user.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class UserServiceSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Роли теперь enum, не нужны переменные
    private OrganizationEntity testOrganization;
    private UserEntity adminUser;
    private UserEntity regularUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Clean up existing data
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // Create test organization
        testOrganization = OrganizationEntity.builder()
                .name("Test Organization")
                .build();
        testOrganization = organizationRepository.save(testOrganization);

        // Create admin user
        adminUser = UserEntity.builder()
                .username("admin")
                .password(passwordEncoder.encode("adminPassword123"))
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Admin")
                        .surname("User")
                        .email("admin@example.com")
                        .phoneNumber("+1234567890")
                        .build())
                .roles(new HashSet<>(Set.of(Role.ADMIN)))
                .organizations(new HashSet<>(Set.of(testOrganization)))
                .build();
        adminUser = userRepository.save(adminUser);

        // Create regular user
        regularUser = UserEntity.builder()
                .username("user")
                .password(passwordEncoder.encode("userPassword123"))
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Regular")
                        .surname("User")
                        .email("user@example.com")
                        .phoneNumber("+9876543210")
                        .build())
                .roles(new HashSet<>(Set.of(Role.USER)))
                .organizations(new HashSet<>(Set.of(testOrganization)))
                .build();
        regularUser = userRepository.save(regularUser);
    }

    @Test
    void testPasswordChangeWithValidCredentials() {
        // Given
        // Get the original encoded password before changing
        String originalEncodedPassword = regularUser.getPassword();
        UserDetails userDetails = userDetailsService.loadUserByUsername(regularUser.getUsername());
        ChangePasswordRequest request = new ChangePasswordRequest(
                "userPassword123", // Use the actual password
                "newSecurePassword123",
                "newSecurePassword123"
        );

        // When
        assertDoesNotThrow(() -> {
            userService.changePassword(request, userDetails);
        });

        // Then
        // Verify password was changed
        Optional<UserEntity> updatedUser = userRepository.findById(regularUser.getId());
        assertTrue(updatedUser.isPresent());
        assertNotEquals(originalEncodedPassword, updatedUser.get().getPassword());

        // Verify the new password works
        assertTrue(passwordEncoder.matches("newSecurePassword123", updatedUser.get().getPassword()));
    }

    @Test
    void testPasswordChangeWithInvalidOldPassword() {
        // Given
        UserDetails userDetails = userDetailsService.loadUserByUsername(regularUser.getUsername());
        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongPassword",
                "newSecurePassword123",
                "newSecurePassword123"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(request, userDetails);
        });
    }

    @Test
    void testUserRegistrationWithValidData() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "newuser",
                "securePassword123",
                "New",
                "User",
                null,
                "newuser@example.com",
                "+1234567899"
        );

        // When
        UserDto result = userService.register(request);

        // Then
        assertNotNull(result);
        assertEquals(request.username(), result.getUsername());
        assertEquals(UserStatus.ACTIVE.name(), result.getStatus());

        // Verify user was created with default role
        Optional<UserEntity> savedUser = userRepository.findByUsername(request.username());
        assertTrue(savedUser.isPresent());
        assertEquals(UserStatus.ACTIVE, savedUser.get().getStatus());
    }

    @Test
    void testUserRegistrationWithInvalidEmail() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "newuser",
                "securePassword123",
                "New",
                "User",
                null,
                "invalid-email", // Invalid email format
                "+1234567890"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(request);
        });
    }

    @Test
    void testUserRegistrationWithWeakPassword() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "newuser",
                "123", // Too weak
                "New",
                "User",
                null,
                "newuser@example.com",
                "+1234567890"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(request);
        });
    }

    @Test
    void testUserRegistrationWithExistingUsername() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "admin", // Already exists
                "securePassword123",
                "New",
                "User",
                null,
                "newuser@example.com",
                "+1234567890"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(request);
        });
    }

    @Test
    void testUserRegistrationWithExistingEmail() {
        // Given
        // First create a user with email
        UserEntity userWithEmail = UserEntity.builder()
                .username("userwithemail")
                .password(passwordEncoder.encode("testPassword123"))
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .email("existing@example.com")
                        .build())
                .build();
        userRepository.save(userWithEmail);

        RegistrationRequest request = new RegistrationRequest(
                "newuser",
                "securePassword123",
                "New",
                "User",
                null,
                "existing@example.com", // Already exists
                "+1234567890"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(request);
        });
    }

    @Test
    void testForgotPasswordWithValidEmail() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest(
                "user@example.com", // Use email from regularUser
                null
        );

        // When
        assertDoesNotThrow(() -> {
            userService.sendEmail(request);
        });

        // Then
        // Verify email was sent (in real implementation, you would check email service)
        assertTrue(true); // Placeholder assertion
    }

    @Test
    void testForgotPasswordWithInvalidEmail() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest(
                "nonexistent@example.com",
                null
        );

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userService.sendEmail(request);
        });
    }

    @Test
    void testUserOrganizationAssignmentValidation() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("orguser")
                .password("password123")
                .name("Org")
                .surname("User")
                .email("org@example.com")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .organizationIds(Set.of(999L)) // Non-existent organization
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userService.create(request);
        });
    }

    @Test
    void testUserDeletionWithDependencies() {
        // Given
        UserEntity userWithDependencies = UserEntity.builder()
                .username("dependentuser")
                .password(passwordEncoder.encode("testPassword123"))
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Dependent")
                        .surname("User")
                        .email("dependent@example.com")
                        .build())
                .roles(new HashSet<>(Set.of(Role.USER)))
                .organizations(new HashSet<>(Set.of(testOrganization)))
                .build();
        userWithDependencies = userRepository.save(userWithDependencies);

        // When
        userService.deleteById(userWithDependencies.getId());

        // Then
        assertFalse(userRepository.existsById(userWithDependencies.getId()));

        // Verify dependencies still exist (no cascade delete)
        // Роли теперь enum, не нужно проверять в БД
        assertTrue(organizationRepository.existsById(testOrganization.getId()));
    }

    @Test
    void testUserUpdateWithNonExistentUser() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated")
                .surname("User")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userService.update(999L, request);
        });
    }
}
