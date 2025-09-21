package org.bn.sensation.core.activity.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class ActivityServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private UserEntity testUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных
        milestoneRepository.deleteAll();
        activityRepository.deleteAll();
        occasionRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

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
        testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .status(Status.DRAFT)
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);
    }

    @Test
    void testCreateActivity() {
        // Given
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("Test Activity")
                .description("Test Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .address(AddressDto.builder()
                        .country("Russia")
                        .city("Moscow")
                        .streetName("Activity Street")
                        .streetNumber("2")
                        .comment("Activity Address")
                        .build())
                .occasionId(testOccasion.getId())
                .status(Status.DRAFT)
                .build();

        // When
        ActivityDto result = activityService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Activity", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getOccasion());
        assertEquals(testOccasion.getId(), result.getOccasion().getId());
        assertEquals(Status.DRAFT, result.getStatus());

        // Проверяем, что активность сохранена в БД
        Optional<ActivityEntity> savedActivity = activityRepository.findById(result.getId());
        assertTrue(savedActivity.isPresent());
        assertEquals("Test Activity", savedActivity.get().getName());
        assertEquals(testOccasion.getId(), savedActivity.get().getOccasion().getId());
    }

    @Test
    void testCreateActivityWithNonExistentOccasion() {
        // Given
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("Test Activity")
                .description("Test Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .occasionId(999L) // Несуществующее мероприятие
                .status(Status.DRAFT)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> activityService.create(request));
    }

    @Test
    void testFindAllActivities() {
        // Given
        createTestActivity("Activity 1", "Description 1");
        createTestActivity("Activity 2", "Description 2");
        createTestActivity("Activity 3", "Description 3");

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityDto> result = activityService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindActivityById() {
        // Given
        ActivityEntity activity = createTestActivity("Test Activity", "Test Description");

        // When
        Optional<ActivityDto> result = activityService.findById(activity.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Activity", result.get().getName());
        assertEquals("Test Description", result.get().getDescription());
        assertEquals(testOccasion.getId(), result.get().getOccasion().getId());
        assertEquals(Status.DRAFT, result.get().getStatus());
    }

    @Test
    void testFindActivityByIdNotFound() {
        // When
        Optional<ActivityDto> result = activityService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateActivity() {
        // Given
        ActivityEntity activity = createTestActivity("Original Name", "Original Description");

        UpdateActivityRequest request = UpdateActivityRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .startDateTime(LocalDateTime.now().plusHours(1))
                .endDateTime(LocalDateTime.now().plusHours(3))
                .status(Status.READY)
                .occasionId(testOccasion.getId())
                .build();

        // When
        ActivityDto result = activityService.update(activity.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(Status.READY, result.getStatus());

        // Проверяем, что изменения сохранены в БД
        Optional<ActivityEntity> savedActivity = activityRepository.findById(activity.getId());
        assertTrue(savedActivity.isPresent());
        assertEquals("Updated Name", savedActivity.get().getName());
        assertEquals("Updated Description", savedActivity.get().getDescription());
        assertEquals(Status.READY, savedActivity.get().getStatus());
    }

    @Test
    void testUpdateActivityPartial() {
        // Given
        ActivityEntity activity = createTestActivity("Original Name", "Original Description");

        UpdateActivityRequest request = UpdateActivityRequest.builder()
                .name("Updated Name")
                .build();

        // When
        ActivityDto result = activityService.update(activity.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Original Description", result.getDescription()); // Не изменилось
        assertEquals(Status.DRAFT, result.getStatus()); // Не изменилось

        // Проверяем, что изменения сохранены в БД
        Optional<ActivityEntity> savedActivity = activityRepository.findById(activity.getId());
        assertTrue(savedActivity.isPresent());
        assertEquals("Updated Name", savedActivity.get().getName());
        assertEquals("Original Description", savedActivity.get().getDescription());
        assertEquals(Status.DRAFT, savedActivity.get().getStatus());
    }

    @Test
    void testUpdateActivityNotFound() {
        // Given
        UpdateActivityRequest request = UpdateActivityRequest.builder()
                .name("Updated Name")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> activityService.update(999L, request));
    }

    @Test
    void testUpdateActivityWithNonExistentOccasion() {
        // Given
        ActivityEntity activity = createTestActivity("Test Activity", "Test Description");

        UpdateActivityRequest request = UpdateActivityRequest.builder()
                .occasionId(999L) // Несуществующее мероприятие
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> activityService.update(activity.getId(), request));
    }

    @Test
    void testDeleteActivity() {
        // Given
        ActivityEntity activity = createTestActivity("Test Activity", "Test Description");
        Long activityId = activity.getId();

        // When
        activityService.deleteById(activityId);

        // Then
        assertFalse(activityRepository.existsById(activityId));
    }

    @Test
    void testDeleteActivityNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> activityService.deleteById(999L));
    }

    @Test
    void testActivityStatusMapping() {
        // Given
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("Test Activity")
                .description("Test Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .occasionId(testOccasion.getId())
                .status(Status.ACTIVE)
                .build();

        // When
        ActivityDto result = activityService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(Status.ACTIVE, result.getStatus());

        // Проверяем, что статус сохранен в БД
        Optional<ActivityEntity> savedActivity = activityRepository.findById(result.getId());
        assertTrue(savedActivity.isPresent());
        assertEquals(Status.ACTIVE, savedActivity.get().getStatus());
    }

    @Test
    void testActivityWithMilestones() {
        // Given - создаем activity и milestone в отдельной транзакции
        TransactionTemplate newTx = new TransactionTemplate(transactionManager);
        newTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        ActivityEntity activity = newTx.execute(status -> {
            // Создаем occasion в той же транзакции
            OccasionEntity occasion = OccasionEntity.builder()
                    .name("Test Occasion")
                    .description("Test Occasion Description")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(1))
                    .status(Status.DRAFT)
                    .build();
            OccasionEntity savedOccasion = occasionRepository.save(occasion);

            // Создаем activity с ссылкой на occasion
            ActivityEntity activityEntity = ActivityEntity.builder()
                    .name("Test Activity")
                    .description("Test Description")
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now().plusHours(2))
                    .status(Status.DRAFT)
                    .occasion(savedOccasion)
                    .build();
            ActivityEntity savedActivity = activityRepository.save(activityEntity);

            // Создаем milestone для этой активности
            MilestoneEntity milestone = MilestoneEntity.builder()
                    .name("Test Milestone")
                    .status(Status.DRAFT)
                    .activity(savedActivity)
                    .build();
            milestoneRepository.save(milestone);

            return savedActivity;
        });

        // When - загружаем activity через сервис в отдельной транзакции
        ActivityDto result = newTx.execute(status -> {
            return activityService.findById(activity.getId()).orElse(null);
        });

        // Then - проверяем результат
        assertNotNull(result);
        assertNotNull(result.getMilestones());
        assertEquals(1, result.getMilestones().size());

        // Проверяем содержимое milestone
        EntityLinkDto milestoneLink = result.getMilestones().iterator().next();
        assertEquals("Test Milestone", milestoneLink.getValue());
    }

    @Test
    void testActivityOccasionMapping() {
        // Given
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("Test Activity")
                .description("Test Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .occasionId(testOccasion.getId())
                .status(Status.DRAFT)
                .build();

        // When
        ActivityDto result = activityService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getOccasion());
        assertEquals(testOccasion.getId(), result.getOccasion().getId());
        assertEquals(testOccasion.getName(), result.getOccasion().getValue());
    }

    @Test
    void testActivityAddressMapping() {
        // Given
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("Test Activity")
                .description("Test Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .address(AddressDto.builder()
                        .country("Russia")
                        .city("Moscow")
                        .streetName("Test Street")
                        .streetNumber("123")
                        .comment("Test Address")
                        .build())
                .occasionId(testOccasion.getId())
                .status(Status.DRAFT)
                .build();

        // When
        ActivityDto result = activityService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAddress());
        assertEquals("Russia", result.getAddress().getCountry());
        assertEquals("Moscow", result.getAddress().getCity());
        assertEquals("Test Street", result.getAddress().getStreetName());
        assertEquals("123", result.getAddress().getStreetNumber());
        assertEquals("Test Address", result.getAddress().getComment());
    }

    @Test
    void testActivityWithoutAddress() {
        // Given
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("Test Activity")
                .description("Test Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .occasionId(testOccasion.getId())
                .status(Status.DRAFT)
                .build();

        // When
        ActivityDto result = activityService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getAddress());

        // Проверяем, что в БД адрес тоже null
        Optional<ActivityEntity> savedActivity = activityRepository.findById(result.getId());
        assertTrue(savedActivity.isPresent());
        assertNull(savedActivity.get().getAddress());
    }

    @Test
    void testFindByOccasionId() {
        // Given - создаем активности для тестового мероприятия
        createTestActivity("Activity 1", "Description 1");
        createTestActivity("Activity 2", "Description 2");
        createTestActivity("Activity 3", "Description 3");

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityDto> result = activityService.findByOccasionId(testOccasion.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());

        // Проверяем, что все активности принадлежат правильному мероприятию
        result.getContent().forEach(activity -> {
            assertNotNull(activity.getOccasion());
            assertEquals(testOccasion.getId(), activity.getOccasion().getId());
        });

        // Проверяем, что все активности присутствуют в результате
        assertTrue(result.getContent().stream()
                .anyMatch(activity -> "Activity 1".equals(activity.getName())));
        assertTrue(result.getContent().stream()
                .anyMatch(activity -> "Activity 2".equals(activity.getName())));
        assertTrue(result.getContent().stream()
                .anyMatch(activity -> "Activity 3".equals(activity.getName())));
    }

    @Test
    void testFindByOccasionIdWithPagination() {
        // Given - создаем 5 активностей для тестового мероприятия
        for (int i = 1; i <= 5; i++) {
            createTestActivity("Activity " + i, "Description " + i);
        }

        // When - запрашиваем первую страницу с размером 3
        Pageable pageable = PageRequest.of(0, 3);
        Page<ActivityDto> firstPage = activityService.findByOccasionId(testOccasion.getId(), pageable);

        // Then
        assertNotNull(firstPage);
        assertEquals(5, firstPage.getTotalElements());
        assertEquals(3, firstPage.getContent().size());
        assertEquals(0, firstPage.getNumber());
        assertEquals(3, firstPage.getSize());
        assertTrue(firstPage.hasNext());

        // When - запрашиваем вторую страницу
        Pageable secondPageable = PageRequest.of(1, 3);
        Page<ActivityDto> secondPage = activityService.findByOccasionId(testOccasion.getId(), secondPageable);

        // Then
        assertNotNull(secondPage);
        assertEquals(5, secondPage.getTotalElements());
        assertEquals(2, secondPage.getContent().size());
        assertEquals(1, secondPage.getNumber());
        assertEquals(3, secondPage.getSize());
        assertFalse(secondPage.hasNext());
    }

    @Test
    void testFindByOccasionIdWithDifferentOccasions() {
        // Given - создаем второе мероприятие
        OccasionEntity secondOccasion = new TransactionTemplate(transactionManager).execute(status -> {
            OccasionEntity occasion = OccasionEntity.builder()
                    .name("Second Test Occasion")
                    .description("Second Test Description")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(4))
                    .status(Status.DRAFT)
                    .organization(testOrganization)
                    .build();
            return occasionRepository.save(occasion);
        });

        // Создаем активности для первого мероприятия
        createTestActivity("Activity for Occasion 1", "Description 1");
        createTestActivity("Activity for Occasion 1 - 2", "Description 2");

        // Создаем активности для второго мероприятия
        new TransactionTemplate(transactionManager).execute(status -> {
            ActivityEntity activity = ActivityEntity.builder()
                    .name("Activity for Occasion 2")
                    .description("Description 3")
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now().plusHours(2))
                    .status(Status.DRAFT)
                    .occasion(secondOccasion)
                    .build();
            return activityRepository.save(activity);
        });

        // When - ищем активности для первого мероприятия
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityDto> resultForFirstOccasion = activityService.findByOccasionId(testOccasion.getId(), pageable);

        // Then
        assertNotNull(resultForFirstOccasion);
        assertEquals(2, resultForFirstOccasion.getTotalElements());
        assertEquals(2, resultForFirstOccasion.getContent().size());

        // Проверяем, что все активности принадлежат первому мероприятию
        resultForFirstOccasion.getContent().forEach(activity -> {
            assertEquals(testOccasion.getId(), activity.getOccasion().getId());
            assertTrue(activity.getName().contains("Occasion 1"));
        });

        // When - ищем активности для второго мероприятия
        Page<ActivityDto> resultForSecondOccasion = activityService.findByOccasionId(secondOccasion.getId(), pageable);

        // Then
        assertNotNull(resultForSecondOccasion);
        assertEquals(1, resultForSecondOccasion.getTotalElements());
        assertEquals(1, resultForSecondOccasion.getContent().size());
        assertEquals(secondOccasion.getId(), resultForSecondOccasion.getContent().getFirst().getOccasion().getId());
        assertEquals("Activity for Occasion 2", resultForSecondOccasion.getContent().getFirst().getName());
    }

    @Test
    void testFindByOccasionIdWithNonExistentOccasion() {
        // Given
        Long nonExistentOccasionId = 999L;

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityDto> result = activityService.findByOccasionId(nonExistentOccasionId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void testFindByOccasionIdWithNullId() {
        // When & Then
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(IllegalArgumentException.class, () -> activityService.findByOccasionId(null, pageable));
    }

    // Вспомогательный метод для создания тестовой активности
    private ActivityEntity createTestActivity(String name, String description) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            ActivityEntity activity = ActivityEntity.builder()
                    .name(name)
                    .description(description)
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now().plusHours(2))
                    .status(Status.DRAFT)
                    .occasion(testOccasion)
                    .build();
            return activityRepository.save(activity);
        });
    }
}
