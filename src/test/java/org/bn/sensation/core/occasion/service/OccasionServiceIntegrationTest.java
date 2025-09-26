package org.bn.sensation.core.occasion.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class OccasionServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OccasionService occasionService;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private OrganizationEntity testOrganization;
    private UserEntity testUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
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
                    .person(Person.builder()
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

            return null;
        });
    }

    @Test
    void testCreateOccasion() {
        // Given
        CreateOccasionRequest request = CreateOccasionRequest.builder()
                .name("Test Occasion")
                .description("Test Description")
                .state(State.DRAFT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .organizationId(testOrganization.getId())
                .build();

        // When
        OccasionDto result = occasionService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Occasion", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(LocalDate.now(), result.getStartDate());
        assertEquals(LocalDate.now().plusDays(3), result.getEndDate());
        assertNotNull(result.getOrganization());
        assertEquals(testOrganization.getId(), result.getOrganization().getId());

        // Проверяем, что событие сохранено в БД
        Optional<OccasionEntity> savedOccasion = occasionRepository.findById(result.getId());
        assertTrue(savedOccasion.isPresent());
        assertEquals("Test Occasion", savedOccasion.get().getName());
        assertEquals(testOrganization.getId(), savedOccasion.get().getOrganization().getId());
    }

    @Test
    void testCreateOccasionWithNonExistentOrganization() {
        // Given
        CreateOccasionRequest request = CreateOccasionRequest.builder()
                .name("Test Occasion")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .organizationId(999L) // Несуществующая организация
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            occasionService.create(request);
        });
    }

    @Test
    void testFindAllOccasions() {
        // Given
        createTestOccasion("Occasion 1", "Description 1");
        createTestOccasion("Occasion 2", "Description 2");
        createTestOccasion("Occasion 3", "Description 3");

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<OccasionDto> result = occasionService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindOccasionById() {
        // Given
        OccasionEntity occasion = createTestOccasion("Test Occasion", "Test Description");

        // When
        Optional<OccasionDto> result = occasionService.findById(occasion.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Occasion", result.get().getName());
        assertEquals("Test Description", result.get().getDescription());
        assertEquals(testOrganization.getId(), result.get().getOrganization().getId());
    }

    @Test
    void testFindOccasionByIdNotFound() {
        // When
        Optional<OccasionDto> result = occasionService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateOccasion() {
        // Given
        OccasionEntity occasion = createTestOccasion("Original Name", "Original Description");

        UpdateOccasionRequest request = UpdateOccasionRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .build();

        // When
        OccasionDto result = occasionService.update(occasion.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(LocalDate.now().plusDays(1), result.getStartDate());
        assertEquals(LocalDate.now().plusDays(5), result.getEndDate());

        // Проверяем, что изменения сохранены в БД
        Optional<OccasionEntity> savedOccasion = occasionRepository.findById(occasion.getId());
        assertTrue(savedOccasion.isPresent());
        assertEquals("Updated Name", savedOccasion.get().getName());
        assertEquals("Updated Description", savedOccasion.get().getDescription());
    }

    @Test
    void testUpdateOccasionPartial() {
        // Given
        OccasionEntity occasion = createTestOccasion("Original Name", "Original Description");

        UpdateOccasionRequest request = UpdateOccasionRequest.builder()
                .name("Updated Name")
                .build();

        // When
        OccasionDto result = occasionService.update(occasion.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Original Description", result.getDescription()); // Не изменилось
        assertEquals(occasion.getStartDate(), result.getStartDate()); // Не изменилось
        assertEquals(occasion.getEndDate(), result.getEndDate()); // Не изменилось
    }

    @Test
    void testUpdateOccasionNotFound() {
        // Given
        UpdateOccasionRequest request = UpdateOccasionRequest.builder()
                .name("Updated Name")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            occasionService.update(999L, request);
        });
    }

    @Test
    void testDeleteOccasion() {
        // Given
        OccasionEntity occasion = createTestOccasion("Test Occasion", "Test Description");
        Long occasionId = occasion.getId();

        // When
        occasionService.deleteById(occasionId);

        // Then
        assertFalse(occasionRepository.existsById(occasionId));
    }

    @Test
    void testDeleteOccasionNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            occasionService.deleteById(999L);
        });
    }

    @Test
    void testOccasionStatusMapping() {
        // Given
        CreateOccasionRequest request = CreateOccasionRequest.builder()
                .name("Test Occasion")
                .description("Test Description")
                .state(State.DRAFT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .organizationId(testOrganization.getId())
                .build();

        // When
        OccasionDto result = occasionService.create(request);

        // Then
        assertNotNull(result);
        // Проверяем, что статус установлен по умолчанию (DRAFT)
        Optional<OccasionEntity> savedOccasion = occasionRepository.findById(result.getId());
        assertTrue(savedOccasion.isPresent());
        assertEquals(State.DRAFT, savedOccasion.get().getState());
    }

    @Test
    void testOccasionOrganizationMapping() {
        // Given
        CreateOccasionRequest request = CreateOccasionRequest.builder()
                .name("Test Occasion")
                .description("Test Description")
                .state(State.DRAFT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .organizationId(testOrganization.getId())
                .build();

        // When
        OccasionDto result = occasionService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getOrganization());
        assertEquals(testOrganization.getId(), result.getOrganization().getId());
        assertEquals(testOrganization.getName(), result.getOrganization().getValue());
    }

    @Test
    void testOccasionWithoutOrganization() {
        // Given
        CreateOccasionRequest request = CreateOccasionRequest.builder()
                .name("Test Occasion")
                .description("Test Description")
                .state(State.DRAFT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .organizationId(null) // Без организации
                .build();

        // When
        OccasionDto result = occasionService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getOrganization());

        // Проверяем, что в БД организация тоже null
        Optional<OccasionEntity> savedOccasion = occasionRepository.findById(result.getId());
        assertTrue(savedOccasion.isPresent());
        assertNull(savedOccasion.get().getOrganization());
    }

    // Вспомогательный метод для создания тестового события
    private OccasionEntity createTestOccasion(String name, String description) {
        return transactionTemplate.execute(status -> {
            OccasionEntity occasion = OccasionEntity.builder()
                    .name(name)
                    .description(description)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .state(State.DRAFT)
                    .organization(testOrganization)
                    .build();
            return occasionRepository.save(occasion);
        });
    }

    @Test
    void testOccasionWithActivityStatistics() {
        // Given
        OccasionEntity occasion = createTestOccasion("Test Occasion", "Test Description");

        // Создаем активности с разными статусами
        ActivityEntity completedActivity = createTestActivity(occasion, "Completed Activity", State.COMPLETED);
        ActivityEntity activeActivity = createTestActivity(occasion, "Active Activity", State.IN_PROGRESS);
        ActivityEntity readyActivity = createTestActivity(occasion, "Ready Activity", State.PLANNED);
        ActivityEntity draftActivity = createTestActivity(occasion, "Draft Activity", State.DRAFT);

        occasion.getActivities().addAll(Set.of(completedActivity, activeActivity, readyActivity, draftActivity));
        occasionRepository.save(occasion);

        // When
        Optional<OccasionDto> result = occasionService.findById(occasion.getId());

        // Then
        assertTrue(result.isPresent());
        OccasionDto occasionDto = result.get();
        assertNotNull(occasionDto.getCompletedActivitiesCount());
        assertNotNull(occasionDto.getActiveActivitiesCount());
        assertNotNull(occasionDto.getTotalActivitiesCount());
        assertEquals(1, occasionDto.getCompletedActivitiesCount());
        assertEquals(2, occasionDto.getActiveActivitiesCount()); // ACTIVE + READY
        assertEquals(4, occasionDto.getTotalActivitiesCount());
    }

    @Test
    void testOccasionWithNoActivities() {
        // Given
        OccasionEntity occasion = createTestOccasion("Test Occasion", "Test Description");

        // When
        Optional<OccasionDto> result = occasionService.findById(occasion.getId());

        // Then
        assertTrue(result.isPresent());
        OccasionDto occasionDto = result.get();
        assertNotNull(occasionDto.getCompletedActivitiesCount());
        assertNotNull(occasionDto.getActiveActivitiesCount());
        assertNotNull(occasionDto.getTotalActivitiesCount());
        assertEquals(0, occasionDto.getCompletedActivitiesCount());
        assertEquals(0, occasionDto.getActiveActivitiesCount());
        assertEquals(0, occasionDto.getTotalActivitiesCount());
    }

    @Test
    void testFindAllOccasionsWithStatistics() {
        // Given
        OccasionEntity occasion1 = createTestOccasion("Occasion 1", "Description 1");
        createTestOccasion("Occasion 2", "Description 2");

        // Добавляем активности к первому мероприятию
        ActivityEntity testActivity = createTestActivity(occasion1, "Activity 1", State.COMPLETED);
        ActivityEntity testActivity1 = createTestActivity(occasion1, "Activity 2", State.IN_PROGRESS);

        occasion1.getActivities().addAll(Set.of(testActivity, testActivity1));
        occasionRepository.save(occasion1);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<OccasionDto> result = occasionService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // Проверяем, что у всех мероприятий есть статистика
        for (OccasionDto occasionDto : result.getContent()) {
            assertNotNull(occasionDto.getCompletedActivitiesCount());
            assertNotNull(occasionDto.getActiveActivitiesCount());
            assertNotNull(occasionDto.getTotalActivitiesCount());
        }

        // Находим мероприятие с активностями и проверяем его статистику
        OccasionDto occasionWithActivities = result.getContent().stream()
                .filter(o -> o.getName().equals("Occasion 1"))
                .findFirst()
                .orElse(null);
        assertNotNull(occasionWithActivities);
        assertEquals(1, occasionWithActivities.getCompletedActivitiesCount());
        assertEquals(1, occasionWithActivities.getActiveActivitiesCount());
        assertEquals(2, occasionWithActivities.getTotalActivitiesCount());
    }

    private ActivityEntity createTestActivity(OccasionEntity occasion, String name, State state) {
        return transactionTemplate.execute(status1 -> {
            ActivityEntity activity = ActivityEntity.builder()
                    .name(name)
                    .description("Test Description")
                    .state(state)
                    .occasion(occasion)
                    .build();
            return activityRepository.save(activity);
        });
    }
}
