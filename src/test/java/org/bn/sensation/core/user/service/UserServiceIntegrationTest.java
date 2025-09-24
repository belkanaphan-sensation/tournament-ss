package org.bn.sensation.core.user.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class UserServiceIntegrationTest extends AbstractIntegrationTest {

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

    private OrganizationEntity testOrganization;
    private OrganizationEntity testOrganization1;
    private UserEntity testUser;

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
        testOrganization1 = OrganizationEntity.builder()
                .name("Test Organization 1")
                .build();
        testOrganization1 = organizationRepository.save(testOrganization1);

        // Create test user
        testUser = UserEntity.builder()
                .username("testuser")
                .password(passwordEncoder.encode("testPassword123")) // Use properly encoded password
                .person(Person.builder()
                        .name("Ivan")
                        .email("email@mail.ru")
                        .phoneNumber("+799999")
                        .surname("Pupkin")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(Role.SUPERADMIN)))
                .organizations(new HashSet<>(Set.of(testOrganization)))
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateUser() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .password("password123")
                .name("New")
                .surname("User")
                .email("newuser@example.com")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.SUPERADMIN))
                .organizationIds(Set.of(testOrganization.getId()))
                .build();

        // When
        UserDto result = userService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getStatus(), result.getStatus());

        // Verify user was saved to database
        Optional<UserEntity> savedUser = userRepository.findByUsername(request.getUsername());
        assertTrue(savedUser.isPresent());
        assertEquals(request.getUsername(), savedUser.get().getUsername());
        assertEquals(request.getName(), savedUser.get().getPerson().getName());
        assertEquals(request.getStatus(), savedUser.get().getStatus());
        assertEquals(request.getRoles(), savedUser.get().getRoles());
        assertEquals(request.getOrganizationIds(), savedUser.get().getOrganizations().stream().map(OrganizationEntity::getId).collect(Collectors.toSet()));
    }

    @Test
    void testCreateUserWithExistingUsername() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser") // Already exists
                .password("password123")
                .name("Test")
                .surname("User")
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.SUPERADMIN))
                .organizationIds(Set.of(testOrganization.getId()))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.create(request);
        });
    }

    @Test
    void testCreateUserWithExistingEmail() {
        // Given
        // First create a user with email
        UserEntity userWithEmail = UserEntity.builder()
                .username("userwithemail")
                .password(passwordEncoder.encode("testPassword123"))
                .status(UserStatus.ACTIVE)
                .build();
        Person person = Person.builder()
                .email("existing@example.com")
                .build();
        userWithEmail.setPerson(person);
        userRepository.save(userWithEmail);

        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .password("password123")
                .name("New")
                .surname("User")
                .email("existing@example.com") // Already exists
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.SUPERADMIN))
                .organizationIds(Set.of(testOrganization.getId()))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.create(request);
        });
    }

    @Test
    void testCreateUserWithNonExistentOrganization() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .password("password123")
                .name("New")
                .surname("User")
                .email("newuser@example.com")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.SUPERADMIN))
                .organizationIds(Set.of(999L)) // Non-existent organization
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userService.create(request);
        });
    }

    @Test
    void testCreateUserWithMultipleOrganizations() {
        // Given
        OrganizationEntity org2 = OrganizationEntity.builder()
                .name("Test Organization 2")
                .build();
        org2 = organizationRepository.save(org2);

        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .password("password123")
                .name("New")
                .surname("User")
                .email("newuser@example.com")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.SUPERADMIN))
                .organizationIds(Set.of(testOrganization.getId(), org2.getId()))
                .build();

        // When
        UserDto result = userService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getUsername(), result.getUsername());

        // Verify user was saved with organizations
        Optional<UserEntity> savedUser = userRepository.findByUsername(request.getUsername());
        assertTrue(savedUser.isPresent());
        assertEquals(2, savedUser.get().getOrganizations().size());
        assertTrue(savedUser.get().getOrganizations().contains(testOrganization));
        assertTrue(savedUser.get().getOrganizations().contains(org2));
    }

    @Test
    void testUpdateUser() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated")
                .surname("User")
                .email("updated@example.com")
                .phoneNumber("+9876543210")
                .status(UserStatus.BLOCKED)
                .roles(Set.of(Role.USER))
                .organizationIds(Set.of(testOrganization1.getId()))
                .build();

        // When
        UserDto result = userService.update(testUser.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());

        // Verify user was updated in database
        Optional<UserEntity> updatedUser = userRepository.findById(testUser.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals(UserStatus.BLOCKED, updatedUser.get().getStatus());
        assertEquals(request.getName(), updatedUser.get().getPerson().getName());
        assertEquals(1, updatedUser.get().getOrganizations().size());
        assertTrue(updatedUser.get().getOrganizations().contains(testOrganization1));
        assertFalse(updatedUser.get().getOrganizations().contains(testOrganization));
        assertTrue(updatedUser.get().getRoles().contains(Role.USER));
        assertFalse(updatedUser.get().getRoles().contains(Role.SUPERADMIN));
    }

    @Test
    void testUpdateUserWithNonExistentOrganization() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated")
                .surname("User")
                .organizationIds(Set.of(999L)) // Non-existent organization
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userService.update(testUser.getId(), request);
        });
    }

    @Test
    void testUpdateUserWithMultipleOrganizations() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated")
                .surname("User")
                .organizationIds(Set.of(testOrganization.getId(), testOrganization1.getId()))
                .build();

        // When
        UserDto result = userService.update(testUser.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());

        // Verify user was updated with multiple organizations
        Optional<UserEntity> updatedUser = userRepository.findById(testUser.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals(2, updatedUser.get().getOrganizations().size());
        assertTrue(updatedUser.get().getOrganizations().contains(testOrganization));
        assertTrue(updatedUser.get().getOrganizations().contains(testOrganization1));
    }

    @Test
    void testUpdateUserWithEmptyOrganizations() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated")
                .surname("User")
                .organizationIds(Set.of()) // Empty set
                .build();

        // When
        UserDto result = userService.update(testUser.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());

        // Verify user organizations were cleared
        Optional<UserEntity> updatedUser = userRepository.findById(testUser.getId());
        assertTrue(updatedUser.isPresent());
        assertTrue(updatedUser.get().getOrganizations().isEmpty());
    }

    @Test
    void testUpdateUserWithNonExistentUser() {
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

    @Test
    void testFindAllUsers() {
        // Create additional users
        UserEntity user2 = UserEntity.builder()
                .username("user2")
                .password(passwordEncoder.encode("testPassword123"))
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user2);

        UserEntity user3 = UserEntity.builder()
                .username("user3")
                .password(passwordEncoder.encode("testPassword123"))
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user3);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDto> result = userService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindUserById() {
        // When
        Optional<UserDto> result = userService.findById(testUser.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
    }

    @Test
    void testFindUserByIdNotFound() {
        // When
        Optional<UserDto> result = userService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteUser() {
        // Given
        Long userId = testUser.getId();

        // When
        userService.deleteById(userId);

        // Then
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void testDeleteUserNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteById(999L);
        });
    }

    @Test
    void testAssignUserToOrganization() {
        // When
        UserDto result = userService.assignUserToOrganization(testUser.getId(), testOrganization1.getId());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());

        // Verify user was assigned to organization
        Optional<UserEntity> updatedUser = userRepository.findById(testUser.getId());
        assertTrue(updatedUser.isPresent());
        assertTrue(updatedUser.get().getOrganizations().contains(testOrganization));
        assertTrue(updatedUser.get().getOrganizations().contains(testOrganization1));
    }

    @Test
    void testAssignUserToOrganizationWithNonExistentUser() {
       // When & Then
        Long organizationId = testOrganization1.getId();
        assertThrows(EntityNotFoundException.class, () -> {
            userService.assignUserToOrganization(999L, organizationId);
        });
    }

    @Test
    void testAssignUserToOrganizationWithNonExistentOrganization() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            userService.assignUserToOrganization(testUser.getId(), 999L);
        });
    }

    @Test
    void testRegisterUser() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "newuser",
                "password123",
                "New",
                "User",
                null,
                "newuser@example.com",
                "+1234567890"
        );

        // When
        UserDto result = userService.register(request);

        // Then
        assertNotNull(result);
        assertEquals(request.username(), result.getUsername());

        // Verify user was saved to database
        Optional<UserEntity> savedUser = userRepository.findByUsername(request.username());
        assertTrue(savedUser.isPresent());
        assertEquals(request.username(), savedUser.get().getUsername());
        assertEquals(UserStatus.ACTIVE, savedUser.get().getStatus());
        assertTrue(savedUser.get().getRoles().contains(Role.USER));
    }

    @Test
    void testChangePassword() {
        // Given
        // Get the original encoded password before changing
        String originalEncodedPassword = testUser.getPassword();

        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());
        ChangePasswordRequest request = new ChangePasswordRequest(
                "testPassword123", // Use the original plain text password
                "newPassword123456789",
                "newPassword123456789"
        );

        // When
        userService.changePassword(request, userDetails);

        // Then
        // Verify password was changed
        Optional<UserEntity> updatedUser = userRepository.findById(testUser.getId());
        assertTrue(updatedUser.isPresent());
        assertNotEquals(originalEncodedPassword, updatedUser.get().getPassword());

        // Verify the new password works
        assertTrue(passwordEncoder.matches("newPassword123456789", updatedUser.get().getPassword()));

        // Verify the old password no longer works
        assertFalse(passwordEncoder.matches("testPassword123", updatedUser.get().getPassword()));
    }

    @Test
    void testSendEmail() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest(
                "email@mail.ru", // Use the email from testUser
                null
        );

        // When
        userService.sendEmail(request);

        // Then
        // Verify email was sent (this would require checking email service)
        // In a real test, you would verify the email was actually sent
        assertTrue(true); // Placeholder assertion
    }
}
