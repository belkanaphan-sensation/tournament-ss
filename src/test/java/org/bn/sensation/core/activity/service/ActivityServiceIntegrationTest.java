package org.bn.sensation.core.activity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.user.entity.*;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.security.CurrentUser;
import org.bn.sensation.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
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
    private UserActivityAssignmentRepository userActivityAssignmentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private CurrentUser currentUser;

    private TransactionTemplate transactionTemplate;

    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private UserEntity testUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Инициализация TransactionTemplate
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных
        userActivityAssignmentRepository.deleteAll();
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
                .state(State.DRAFT)
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Настройка мока CurrentUser
        SecurityUser mockSecurityUser = org.mockito.Mockito.mock(SecurityUser.class);
        when(mockSecurityUser.getId()).thenReturn(testUser.getId());
        when(currentUser.getSecurityUser()).thenReturn(mockSecurityUser);
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
                .state(State.DRAFT)
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
        assertEquals(State.DRAFT, result.getState());

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
                .state(State.DRAFT)
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
        assertEquals(State.DRAFT, result.get().getState());
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
                .state(State.PLANNED)
                .build();

        // When
        ActivityDto result = activityService.update(activity.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(State.PLANNED, result.getState());

        // Проверяем, что изменения сохранены в БД
        Optional<ActivityEntity> savedActivity = activityRepository.findById(activity.getId());
        assertTrue(savedActivity.isPresent());
        assertEquals("Updated Name", savedActivity.get().getName());
        assertEquals("Updated Description", savedActivity.get().getDescription());
        assertEquals(State.PLANNED, savedActivity.get().getState());
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
        assertEquals(State.DRAFT, result.getState()); // Не изменилось

        // Проверяем, что изменения сохранены в БД
        Optional<ActivityEntity> savedActivity = activityRepository.findById(activity.getId());
        assertTrue(savedActivity.isPresent());
        assertEquals("Updated Name", savedActivity.get().getName());
        assertEquals("Original Description", savedActivity.get().getDescription());
        assertEquals(State.DRAFT, savedActivity.get().getState());
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
                .state(State.IN_PROGRESS)
                .build();

        // When
        ActivityDto result = activityService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(State.IN_PROGRESS, result.getState());

        // Проверяем, что статус сохранен в БД
        Optional<ActivityEntity> savedActivity = activityRepository.findById(result.getId());
        assertTrue(savedActivity.isPresent());
        assertEquals(State.IN_PROGRESS, savedActivity.get().getState());
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
                .state(State.DRAFT)
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
                .state(State.DRAFT)
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
                .state(State.DRAFT)
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
        List<ActivityDto> result = activityService.findByOccasionId(testOccasion.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Проверяем, что все активности принадлежат правильному мероприятию
        result.forEach(activity -> {
            assertNotNull(activity.getOccasion());
            assertEquals(testOccasion.getId(), activity.getOccasion().getId());
        });

        // Проверяем, что все активности присутствуют в результате
        assertTrue(result.stream()
                .anyMatch(activity -> "Activity 1".equals(activity.getName())));
        assertTrue(result.stream()
                .anyMatch(activity -> "Activity 2".equals(activity.getName())));
        assertTrue(result.stream()
                .anyMatch(activity -> "Activity 3".equals(activity.getName())));
    }

    @Test
    void testFindByOccasionIdWithManyActivities() {
        // Given - создаем 5 активностей для тестового мероприятия
        for (int i = 1; i <= 5; i++) {
            createTestActivity("Activity " + i, "Description " + i);
        }

        // When
        List<ActivityDto> result = activityService.findByOccasionId(testOccasion.getId());

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());

        // Проверяем, что все активности принадлежат правильному мероприятию
        result.forEach(activity -> {
            assertNotNull(activity.getOccasion());
            assertEquals(testOccasion.getId(), activity.getOccasion().getId());
        });

        // Проверяем, что все активности присутствуют в результате
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            assertTrue(result.stream()
                    .anyMatch(activity -> ("Activity " + index).equals(activity.getName())));
        }
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
                    .state(State.DRAFT)
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
                    .state(State.DRAFT)
                    .occasion(secondOccasion)
                    .build();
            activity = activityRepository.save(activity);
            
            // Создаем назначение пользователя на активность через связь
            UserActivityAssignmentEntity assignment = UserActivityAssignmentEntity.builder()
                    .user(testUser)
                    .activity(activity)
                    .position(UserActivityPosition.PARTICIPANT)
                    .build();
            assignment = userActivityAssignmentRepository.save(assignment);
            
            return activity;
        });

        // When - ищем активности для первого мероприятия
        List<ActivityDto> resultForFirstOccasion = activityService.findByOccasionId(testOccasion.getId());

        // Then
        assertNotNull(resultForFirstOccasion);
        assertEquals(2, resultForFirstOccasion.size());

        // Проверяем, что все активности принадлежат первому мероприятию
        resultForFirstOccasion.forEach(activity -> {
            assertEquals(testOccasion.getId(), activity.getOccasion().getId());
            assertTrue(activity.getName().contains("Occasion 1"));
        });

        // When - ищем активности для второго мероприятия
        List<ActivityDto> resultForSecondOccasion = activityService.findByOccasionId(secondOccasion.getId());

        // Then
        assertNotNull(resultForSecondOccasion);
        assertEquals(1, resultForSecondOccasion.size());
        assertEquals(secondOccasion.getId(), resultForSecondOccasion.get(0).getOccasion().getId());
        assertEquals("Activity for Occasion 2", resultForSecondOccasion.get(0).getName());
    }

    @Test
    void testFindByOccasionIdWithNonExistentOccasion() {
        // Given
        Long nonExistentOccasionId = 999L;

        // When
        List<ActivityDto> result = activityService.findByOccasionId(nonExistentOccasionId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByOccasionIdWithNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> activityService.findByOccasionId(null));
    }

    @Test
    void testFindByOccasionIdInLifeStatesForCurrentUser() {
        // Given - создаем активности с разными состояниями
        createTestActivityWithState("Activity DRAFT", "Description 1", State.DRAFT);
        createTestActivityWithState("Activity PLANNED", "Description 2", State.PLANNED);
        createTestActivityWithState("Activity IN_PROGRESS", "Description 3", State.IN_PROGRESS);
        createTestActivityWithState("Activity COMPLETED", "Description 4", State.COMPLETED);

        // When
        List<ActivityDto> result = activityService.findByOccasionIdInLifeStatesForCurrentUser(testOccasion.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size()); // Только PLANNED, IN_PROGRESS, COMPLETED

        // Проверяем, что все активности принадлежат правильному мероприятию и имеют life states
        result.forEach(activity -> {
            assertNotNull(activity.getOccasion());
            assertEquals(testOccasion.getId(), activity.getOccasion().getId());
            assertTrue(State.LIFE_STATES.contains(activity.getState()));
        });

        // Проверяем, что DRAFT активности нет в результате
        boolean hasDraftActivity = result.stream()
                .anyMatch(activity -> activity.getState() == State.DRAFT);
        assertFalse(hasDraftActivity);
    }

    @Test
    void testFindByOccasionIdInLifeStatesForCurrentUserWithManyActivities() {
        // Given - создаем 5 активностей с life states
        for (int i = 1; i <= 5; i++) {
            createTestActivityWithState("Activity " + i, "Description " + i, State.PLANNED);
        }

        // When
        List<ActivityDto> result = activityService.findByOccasionIdInLifeStatesForCurrentUser(testOccasion.getId());

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());

        // Проверяем, что все активности принадлежат правильному мероприятию и имеют life states
        result.forEach(activity -> {
            assertNotNull(activity.getOccasion());
            assertEquals(testOccasion.getId(), activity.getOccasion().getId());
            assertTrue(State.LIFE_STATES.contains(activity.getState()));
        });

        // Проверяем, что все активности присутствуют в результате
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            assertTrue(result.stream()
                    .anyMatch(activity -> ("Activity " + index).equals(activity.getName())));
        }
    }

    @Test
    void testFindByOccasionIdInLifeStatesWithNonExistentOccasionForCurrentUser() {
        // Given
        Long nonExistentOccasionId = 999L;

        // When
        List<ActivityDto> result = activityService.findByOccasionIdInLifeStatesForCurrentUser(nonExistentOccasionId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByOccasionIdInLifeStatesWithNullIdForCurrentUser() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> activityService.findByOccasionIdInLifeStatesForCurrentUser(null));
    }

    // Вспомогательный метод для создания тестовой активности
    private ActivityEntity createTestActivity(String name, String description) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            ActivityEntity activity = ActivityEntity.builder()
                    .name(name)
                    .description(description)
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now().plusHours(2))
                    .state(State.DRAFT)
                    .occasion(testOccasion)
                    .build();
            activity = activityRepository.save(activity);
            
            // Создаем назначение пользователя на активность через связь
            UserActivityAssignmentEntity assignment = UserActivityAssignmentEntity.builder()
                    .user(testUser)
                    .activity(activity)
                    .position(UserActivityPosition.PARTICIPANT)
                    .build();
            assignment = userActivityAssignmentRepository.save(assignment);
            
            return activity;
        });
    }

    // Вспомогательный метод для создания тестовой активности с определенным состоянием
    private ActivityEntity createTestActivityWithState(String name, String description, State state) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            ActivityEntity activity = ActivityEntity.builder()
                    .name(name)
                    .description(description)
                    .startDateTime(LocalDateTime.now())
                    .endDateTime(LocalDateTime.now().plusHours(2))
                    .state(state)
                    .occasion(testOccasion)
                    .build();
            activity = activityRepository.save(activity);
            
            // Создаем назначение пользователя на активность через связь
            UserActivityAssignmentEntity assignment = UserActivityAssignmentEntity.builder()
                    .user(testUser)
                    .activity(activity)
                    .position(UserActivityPosition.PARTICIPANT)
                    .build();
            assignment = userActivityAssignmentRepository.save(assignment);
            
            return activity;
        });
    }

    @Test
    void testActivityWithMilestoneStatistics() {
        // Given
        ActivityEntity activity = createTestActivity("Test Activity", "Test Description");

        // Создаем этапы с разными статусами
        MilestoneEntity completedMilestone = createTestMilestone(activity, "Completed Milestone", State.COMPLETED);
        MilestoneEntity activeMilestone = createTestMilestone(activity, "Active Milestone", State.IN_PROGRESS);
        MilestoneEntity draftMilestone = createTestMilestone(activity, "Draft Milestone", State.DRAFT);

        activity.getMilestones().addAll(Set.of(completedMilestone, activeMilestone, draftMilestone));
        activityRepository.save(activity);

        // When
        Optional<ActivityDto> result = activityService.findById(activity.getId());

        // Then
        assertTrue(result.isPresent());
        ActivityDto activityDto = result.get();
        assertNotNull(activityDto.getCompletedMilestonesCount());
        assertNotNull(activityDto.getTotalMilestonesCount());
        assertEquals(1, activityDto.getCompletedMilestonesCount());
        assertEquals(3, activityDto.getTotalMilestonesCount());
    }

    @Test
    void testActivityWithNoMilestones() {
        // Given
        ActivityEntity activity = createTestActivity("Test Activity", "Test Description");

        // When
        Optional<ActivityDto> result = activityService.findById(activity.getId());

        // Then
        assertTrue(result.isPresent());
        ActivityDto activityDto = result.get();
        assertNotNull(activityDto.getCompletedMilestonesCount());
        assertNotNull(activityDto.getTotalMilestonesCount());
        assertEquals(0, activityDto.getCompletedMilestonesCount());
        assertEquals(0, activityDto.getTotalMilestonesCount());
    }

    @Test
    void testFindAllActivitiesWithStatistics() {
        // Given
        ActivityEntity activity1 = createTestActivity("Activity 1", "Description 1");
        createTestActivity("Activity 2", "Description 2");

        // Добавляем этапы к первой активности
        MilestoneEntity testMilestone = createTestMilestone(activity1, "Milestone 1", State.COMPLETED);
        MilestoneEntity testMilestone1 = createTestMilestone(activity1, "Milestone 2", State.IN_PROGRESS);
        activity1.getMilestones().addAll(Set.of(testMilestone, testMilestone1));

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityDto> result = activityService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // Проверяем, что у всех активностей есть статистика
        for (ActivityDto activityDto : result.getContent()) {
            assertNotNull(activityDto.getCompletedMilestonesCount());
            assertNotNull(activityDto.getTotalMilestonesCount());
        }

        // Находим активность с этапами и проверяем её статистику
        ActivityDto activityWithMilestones = result.getContent().stream()
                .filter(a -> a.getName().equals("Activity 1"))
                .findFirst()
                .orElse(null);
        assertNotNull(activityWithMilestones);
        assertEquals(1, activityWithMilestones.getCompletedMilestonesCount());
        assertEquals(2, activityWithMilestones.getTotalMilestonesCount());
    }

    private MilestoneEntity createTestMilestone(ActivityEntity activity, String name, State state) {
        return transactionTemplate.execute(status1 -> {
            MilestoneEntity milestone = MilestoneEntity.builder()
                    .name(name)
                    .description("Test Description")
                    .state(state)
                    .activity(activity)
                    .milestoneOrder(1)
                    .build();
            return milestoneRepository.save(milestone);
        });
    }
}
