package org.bn.sensation.core.milestone.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
class MilestoneServiceIntegrationTest {

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ActivityEntity testActivity;
    private OrganizationEntity testOrganization;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            milestoneRepository.deleteAll();
            activityRepository.deleteAll();
            occasionRepository.deleteAll();
            organizationRepository.deleteAll();
            userRepository.deleteAll();
            return null;
        });

        // Создание тестовых данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            // Создание тестового пользователя
            testUser = UserEntity.builder()
                    .username("testuser" + System.currentTimeMillis())
                    .password("password123")
                    .person(org.bn.sensation.core.common.entity.Person.builder()
                            .name("Test")
                            .surname("User")
                            .email("test@example.com")
                            .phoneNumber("+1234567890")
                            .build())
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(Role.USER))
                    .build();
            testUser = userRepository.save(testUser);

            // Создание тестовой организации
            testOrganization = OrganizationEntity.builder()
                    .name("Test Organization")
                    .description("Test Description")
                    .address(Address.builder()
                            .country("Russia")
                            .city("Moscow")
                            .streetName("Test Street")
                            .streetNumber("1")
                            .comment("Test Address")
                            .build())
                    .build();
            testOrganization = organizationRepository.save(testOrganization);

            // Создание тестового мероприятия
            OccasionEntity testOccasion = OccasionEntity.builder()
                    .name("Test Occasion")
                    .description("Test Description")
                    .startDate(java.time.LocalDate.now())
                    .endDate(java.time.LocalDate.now().plusDays(3))
                    .status(Status.DRAFT)
                    .organization(testOrganization)
                    .build();
            testOccasion = occasionRepository.save(testOccasion);

            // Создание тестовой активности
            testActivity = ActivityEntity.builder()
                    .name("Test Activity")
                    .description("Test Description")
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now().plusHours(2))
                    .address(Address.builder()
                            .country("Russia")
                            .city("Moscow")
                            .streetName("Activity Street")
                            .streetNumber("2")
                            .comment("Activity Address")
                            .build())
                    .status(Status.DRAFT)
                    .occasion(testOccasion)
                    .build();
            testActivity = activityRepository.save(testActivity);

            return null;
        });
    }

    @Test
    @Transactional
    void testCreateMilestone() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(testActivity.getId())
                .status(Status.DRAFT)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Milestone", result.getName());
        assertNotNull(result.getActivity());
        assertEquals(testActivity.getId(), result.getActivity().getId());
        assertEquals(Status.DRAFT, result.getStatus());

        // Проверяем, что веха сохранена в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Test Milestone", savedMilestone.get().getName());
        assertEquals(testActivity.getId(), savedMilestone.get().getActivity().getId());
    }

    @Test
    @Transactional
    void testCreateMilestoneWithNonExistentActivity() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(999L) // Несуществующая активность
                .status(Status.DRAFT)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneService.create(request);
        });
    }

    @Test
    @Transactional
    void testFindAllMilestones() {
        // Given
        createTestMilestone("Milestone 1");
        createTestMilestone("Milestone 2");
        createTestMilestone("Milestone 3");

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> result = milestoneService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    @Transactional
    void testFindMilestoneById() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // When
        Optional<MilestoneDto> result = milestoneService.findById(milestone.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Milestone", result.get().getName());
        assertEquals(testActivity.getId(), result.get().getActivity().getId());
        assertEquals(Status.DRAFT, result.get().getStatus());
    }

    @Test
    @Transactional
    void testFindMilestoneByIdNotFound() {
        // When
        Optional<MilestoneDto> result = milestoneService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testUpdateMilestone() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Original Name");

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .name("Updated Name")
                .status(Status.READY)
                .activityId(testActivity.getId())
                .build();

        // When
        MilestoneDto result = milestoneService.update(milestone.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals(Status.READY, result.getStatus());

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(milestone.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Updated Name", savedMilestone.get().getName());
        assertEquals(Status.READY, savedMilestone.get().getStatus());
    }

    @Test
    @Transactional
    void testUpdateMilestonePartial() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Original Name");

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .name("Updated Name")
                .build();

        // When
        MilestoneDto result = milestoneService.update(milestone.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals(Status.DRAFT, result.getStatus()); // Не изменилось

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(milestone.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Updated Name", savedMilestone.get().getName());
        assertEquals(Status.DRAFT, savedMilestone.get().getStatus());
    }

    @Test
    @Transactional
    void testUpdateMilestoneNotFound() {
        // Given
        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .name("Updated Name")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneService.update(999L, request);
        });
    }

    @Test
    @Transactional
    void testUpdateMilestoneWithNonExistentActivity() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .activityId(999L) // Несуществующая активность
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneService.update(milestone.getId(), request);
        });
    }

    @Test
    @Transactional
    void testDeleteMilestone() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");
        Long milestoneId = milestone.getId();

        // When
        milestoneService.deleteById(milestoneId);

        // Then
        assertFalse(milestoneRepository.existsById(milestoneId));
    }

    @Test
    void testDeleteMilestoneNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.deleteById(999L);
        });
    }

    @Test
    @Transactional
    void testMilestoneStatusMapping() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(testActivity.getId())
                .status(Status.ACTIVE)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(Status.ACTIVE, result.getStatus());

        // Проверяем, что статус сохранен в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals(Status.ACTIVE, savedMilestone.get().getStatus());
    }

    @Test
    @Transactional
    void testMilestoneWithRounds() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRounds());
        // Начально список раундов пуст
        assertTrue(result.getRounds().isEmpty());
    }

    @Test
    @Transactional
    void testMilestoneActivityMapping() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(testActivity.getId())
                .status(Status.DRAFT)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getActivity());
        assertEquals(testActivity.getId(), result.getActivity().getId());
        assertEquals(testActivity.getName(), result.getActivity().getValue());
    }

    @Test
    @Transactional
    void testMilestoneWithoutActivity() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(null) // Без активности
                .status(Status.DRAFT)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getActivity());

        // Проверяем, что в БД активность тоже null
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertNull(savedMilestone.get().getActivity());
    }

    // Вспомогательный метод для создания тестовой вехи
    private MilestoneEntity createTestMilestone(String name) {
        return transactionTemplate.execute(status -> {
            MilestoneEntity milestone = MilestoneEntity.builder()
                    .name(name)
                    .status(Status.DRAFT)
                    .activity(testActivity)
                    .build();
            return milestoneRepository.save(milestone);
        });
    }
}
