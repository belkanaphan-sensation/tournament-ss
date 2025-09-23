package org.bn.sensation.core.milestone.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.milestone.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class MilestoneServiceIntegrationTest extends AbstractIntegrationTest {

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
    private CriteriaRepository criteriaRepository;

    @Autowired
    private MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ActivityEntity testActivity;
    private OrganizationEntity testOrganization;
    private UserEntity testUser;
    private CriteriaEntity testCriteria;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            milestoneCriteriaAssignmentRepository.deleteAll();
            milestoneRepository.deleteAll();
            criteriaRepository.deleteAll();
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
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .state(State.DRAFT)
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
                    .state(State.DRAFT)
                    .occasion(testOccasion)
                    .build();
            testActivity = activityRepository.save(testActivity);

            // Создание тестового критерия
            testCriteria = CriteriaEntity.builder()
                    .name("Прохождение")
                    .build();
            testCriteria = criteriaRepository.save(testCriteria);

            return null;
        });
    }

    @Test
    void testCreateMilestone() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Milestone", result.getName());
        assertEquals("Test Milestone Description", result.getDescription());
        assertNotNull(result.getActivity());
        assertEquals(testActivity.getId(), result.getActivity().getId());
        assertEquals(State.DRAFT, result.getState());

        // Проверяем, что веха сохранена в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Test Milestone", savedMilestone.get().getName());
        assertEquals("Test Milestone Description", savedMilestone.get().getDescription());
        assertEquals(testActivity.getId(), savedMilestone.get().getActivity().getId());
    }

    @Test
    void testCreateMilestoneWithNonExistentActivity() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(999L) // Несуществующая активность
                .state(State.DRAFT)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneService.create(request);
        });
    }

    @Test
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
    void testFindMilestoneById() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // When
        Optional<MilestoneDto> result = milestoneService.findById(milestone.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Milestone", result.get().getName());
        assertEquals(testActivity.getId(), result.get().getActivity().getId());
        assertEquals(State.DRAFT, result.get().getState());
    }

    @Test
    void testFindMilestoneByIdNotFound() {
        // When
        Optional<MilestoneDto> result = milestoneService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateMilestone() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Original Name");

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .state(State.PLANNED)
                .build();

        // When
        MilestoneDto result = milestoneService.update(milestone.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(State.PLANNED, result.getState());

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(milestone.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Updated Name", savedMilestone.get().getName());
        assertEquals("Updated Description", savedMilestone.get().getDescription());
        assertEquals(State.PLANNED, savedMilestone.get().getState());
    }

    @Test
    void testUpdateMilestonePartial() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Original Name");

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        // When
        MilestoneDto result = milestoneService.update(milestone.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(State.DRAFT, result.getState()); // Не изменилось

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(milestone.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Updated Name", savedMilestone.get().getName());
        assertEquals("Updated Description", savedMilestone.get().getDescription());
        assertEquals(State.DRAFT, savedMilestone.get().getState());
    }

    @Test
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
    void testMilestoneStatusMapping() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(testActivity.getId())
                .state(State.IN_PROGRESS)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(State.IN_PROGRESS, result.getState());

        // Проверяем, что статус сохранен в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals(State.IN_PROGRESS, savedMilestone.get().getState());
    }

    @Test
    void testMilestoneWithRoundStatistics() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");
        
        // Создаем раунды с разными статусами
        createTestRound(milestone, "Completed Round", State.COMPLETED);
        createTestRound(milestone, "Active Round", State.IN_PROGRESS);
        createTestRound(milestone, "Draft Round", State.DRAFT);

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCompletedRoundsCount());
        assertNotNull(result.getTotalRoundsCount());
        assertEquals(1L, result.getCompletedRoundsCount());
        assertEquals(3L, result.getTotalRoundsCount());
    }

    @Test
    void testMilestoneWithNoRounds() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCompletedRoundsCount());
        assertNotNull(result.getTotalRoundsCount());
        assertEquals(0L, result.getCompletedRoundsCount());
        assertEquals(0L, result.getTotalRoundsCount());
    }

    @Test
    void testFindAllMilestonesWithStatistics() {
        // Given
        MilestoneEntity milestone1 = createTestMilestone("Milestone 1");
        createTestMilestone("Milestone 2"); // This milestone is intentionally left without rounds for testing

        // Добавляем раунды к первому этапу
        createTestRound(milestone1, "Round 1", State.COMPLETED);
        createTestRound(milestone1, "Round 2", State.IN_PROGRESS);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> result = milestoneService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // Проверяем, что у всех этапов есть статистика
        for (MilestoneDto milestoneDto : result.getContent()) {
            assertNotNull(milestoneDto.getCompletedRoundsCount());
            assertNotNull(milestoneDto.getTotalRoundsCount());
        }

        // Находим этап с раундами и проверяем его статистику
        MilestoneDto milestoneWithRounds = result.getContent().stream()
                .filter(m -> m.getName().equals("Milestone 1"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestoneWithRounds);
        assertEquals(1L, milestoneWithRounds.getCompletedRoundsCount());
        assertEquals(2L, milestoneWithRounds.getTotalRoundsCount());

        // Проверяем этап без раундов
        MilestoneDto milestoneWithoutRounds = result.getContent().stream()
                .filter(m -> m.getName().equals("Milestone 2"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestoneWithoutRounds);
        assertEquals(0L, milestoneWithoutRounds.getCompletedRoundsCount());
        assertEquals(0L, milestoneWithoutRounds.getTotalRoundsCount());
    }

    private RoundEntity createTestRound(MilestoneEntity milestone, String name, State state) {
        return transactionTemplate.execute(status1 -> {
            RoundEntity round = RoundEntity.builder()
                    .name(name)
                    .description("Test Description")
                    .state(state)
                    .milestone(milestone)
                    .build();
            return roundRepository.save(round);
        });
    }

    @Test
    void testMilestoneActivityMapping() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
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
    void testMilestoneWithoutActivity() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(null) // Без активности
                .state(State.DRAFT)
                .build();

        // When and then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.create(request);
        });
    }

    @Test
    @Transactional
    void testCreateMilestoneWithDefaultCriteria() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals("Test Milestone", result.getName());

        // Проверяем, что критерий по умолчанию был добавлен через репозиторий назначений
        long assignmentCount = milestoneCriteriaAssignmentRepository.countByMilestoneId(result.getId());
        assertEquals(1, assignmentCount);
        
        // Проверяем, что назначение создано с правильным критерием
        Optional<MilestoneCriteriaAssignmentEntity> assignment = milestoneCriteriaAssignmentRepository.findByMilestoneIdAndCriteriaId(result.getId(), testCriteria.getId());
        assertTrue(assignment.isPresent());
        assertEquals("Прохождение", assignment.get().getCriteria().getName());
        assertNull(assignment.get().getCompetitionRole()); // Критерий по умолчанию не привязан к роли
    }

    @Test
    @Transactional
    void testCreateMilestoneWithExistingCriteriaAssignments() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");
        
        // Создаем назначение критерия вручную
        MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(testCriteria)
                .competitionRole(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment);

        // When - создаем новый этап
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Another Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .build();

        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals("Another Milestone", result.getName());

        // Проверяем, что критерий по умолчанию был добавлен к новому этапу
        long assignmentCount = milestoneCriteriaAssignmentRepository.countByMilestoneId(result.getId());
        assertEquals(1, assignmentCount);
    }

    @Test
    void testCreateMilestoneWithNonExistentDefaultCriteria() {
        // Given - удаляем критерий "Прохождение"
        criteriaRepository.deleteAll();

        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneService.create(request);
        });
    }

    @Test
    @Transactional
    void testMilestoneCriteriaAssignmentsMapping() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");
        
        // Создаем назначение критерия
        MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(testCriteria)
                .competitionRole(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment);

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        
        // Проверяем, что этап имеет связи с критериями в БД через репозиторий
        long assignmentCount = milestoneCriteriaAssignmentRepository.countByMilestoneId(milestone.getId());
        assertEquals(1, assignmentCount);
    }

    @Test
    @Transactional
    void testMilestoneWithMultipleCriteriaAssignments() {
        // Given
        // Создаем дополнительные критерии
        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Техника")
                .build();
        criteriaRepository.save(criteria2);

        CriteriaEntity criteria3 = CriteriaEntity.builder()
                .name("Стилистика")
                .build();
        criteriaRepository.save(criteria3);

        MilestoneEntity milestone = createTestMilestone("Test Milestone");
        
        // Создаем несколько назначений критериев
        MilestoneCriteriaAssignmentEntity assignment1 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(testCriteria)
                .competitionRole(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment1);

        MilestoneCriteriaAssignmentEntity assignment2 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(criteria2)
                .competitionRole(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment2);

        MilestoneCriteriaAssignmentEntity assignment3 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(criteria3)
                .competitionRole(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment3);

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        
        // Проверяем, что этап имеет все назначения критериев в БД через репозиторий
        long assignmentCount = milestoneCriteriaAssignmentRepository.countByMilestoneId(milestone.getId());
        assertEquals(3, assignmentCount);
    }


    @Test
    void testCreateMilestoneWithOrder() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .milestoneOrder(0)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(0), result.getMilestoneOrder());

        // Проверяем, что порядок сохранен в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals(Integer.valueOf(0), savedMilestone.get().getMilestoneOrder());
    }

    @Test
    void testCreateMilestoneWithoutOrder() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .milestoneOrder(null) // Не указываем порядок
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(0), result.getMilestoneOrder()); // Должен быть установлен автоматически

        // Проверяем, что порядок сохранен в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals(Integer.valueOf(0), savedMilestone.get().getMilestoneOrder());
    }

    @Test
    void testCreateMultipleMilestonesOrder() {
        // Given
        CreateMilestoneRequest request1 = CreateMilestoneRequest.builder()
                .name("First Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .build();

        CreateMilestoneRequest request2 = CreateMilestoneRequest.builder()
                .name("Second Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .build();

        CreateMilestoneRequest request3 = CreateMilestoneRequest.builder()
                .name("Third Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .build();

        // When
        MilestoneDto result1 = milestoneService.create(request1);
        MilestoneDto result2 = milestoneService.create(request2);
        MilestoneDto result3 = milestoneService.create(request3);

        // Then
        assertEquals(Integer.valueOf(0), result1.getMilestoneOrder());
        assertEquals(Integer.valueOf(1), result2.getMilestoneOrder());
        assertEquals(Integer.valueOf(2), result3.getMilestoneOrder());
    }

    @Test
    void testCreateMultipleMilestonesReOrder() {
        // Given
        CreateMilestoneRequest request1 = CreateMilestoneRequest.builder()
                .name("First Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .milestoneOrder(0)
                .build();

        CreateMilestoneRequest request2 = CreateMilestoneRequest.builder()
                .name("Second Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .milestoneOrder(1)
                .build();

        CreateMilestoneRequest request3 = CreateMilestoneRequest.builder()
                .name("Third Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .milestoneOrder(1)
                .build();

        // When
        MilestoneDto result1 = milestoneService.create(request1);
        MilestoneDto result2 = milestoneService.create(request2);
        MilestoneDto result3 = milestoneService.create(request3);

        // Then
        assertEquals(Integer.valueOf(0), result1.getMilestoneOrder());
        assertEquals(Integer.valueOf(1), result2.getMilestoneOrder());
        assertEquals(Integer.valueOf(1), result3.getMilestoneOrder());

        List<MilestoneEntity> milestones = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(testActivity.getId());
        assertEquals(3, milestones.size());
        assertEquals(Integer.valueOf(0), milestones.get(0).getMilestoneOrder());
        assertEquals("First Milestone", milestones.get(0).getName());
        assertEquals(Integer.valueOf(1), milestones.get(1).getMilestoneOrder());
        assertEquals("Third Milestone", milestones.get(1).getName());
        assertEquals(Integer.valueOf(2), milestones.get(2).getMilestoneOrder());
        assertEquals("Second Milestone", milestones.get(2).getName());
    }

    @Test
    void testFindMilestonesByActivityIdOrdered() {
        // Given
        createTestMilestoneWithOrder("First", 2);
        createTestMilestoneWithOrder("Second", 0);
        createTestMilestoneWithOrder("Third", 1);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> result = milestoneService.findByActivityId(testActivity.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        
        // Проверяем, что этапы отсортированы по порядку
        assertEquals("Second", result.getContent().get(0).getName());
        assertEquals("Third", result.getContent().get(1).getName());
        assertEquals("First", result.getContent().get(2).getName());
    }

    @Test
    void testFindByActivityIdInLifeStates() {
        // Given - создаем этапы с разными состояниями
        createTestMilestoneWithState("Milestone DRAFT", State.DRAFT);
        createTestMilestoneWithState("Milestone PLANNED", State.PLANNED);
        createTestMilestoneWithState("Milestone IN_PROGRESS", State.IN_PROGRESS);
        createTestMilestoneWithState("Milestone COMPLETED", State.COMPLETED);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> result = milestoneService.findByActivityIdInLifeStates(testActivity.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements()); // Только PLANNED, IN_PROGRESS, COMPLETED
        assertEquals(3, result.getContent().size());

        // Проверяем, что все этапы принадлежат правильной активности и имеют life states
        result.getContent().forEach(milestone -> {
            assertNotNull(milestone.getActivity());
            assertEquals(testActivity.getId(), milestone.getActivity().getId());
            assertTrue(State.LIFE_STATES.contains(milestone.getState()));
        });

        // Проверяем, что DRAFT этапа нет в результате
        boolean hasDraftMilestone = result.getContent().stream()
                .anyMatch(milestone -> milestone.getState() == State.DRAFT);
        assertFalse(hasDraftMilestone);
    }

    @Test
    void testFindByActivityIdInLifeStatesWithPagination() {
        // Given - создаем 5 этапов с life states
        for (int i = 1; i <= 5; i++) {
            createTestMilestoneWithState("Milestone " + i, State.PLANNED);
        }

        // When - запрашиваем первую страницу с размером 3
        Pageable pageable = PageRequest.of(0, 3);
        Page<MilestoneDto> firstPage = milestoneService.findByActivityIdInLifeStates(testActivity.getId(), pageable);

        // Then
        assertNotNull(firstPage);
        assertEquals(5, firstPage.getTotalElements());
        assertEquals(3, firstPage.getContent().size());
        assertEquals(0, firstPage.getNumber());
        assertEquals(3, firstPage.getSize());
        assertTrue(firstPage.hasNext());

        // When - запрашиваем вторую страницу
        Pageable secondPageable = PageRequest.of(1, 3);
        Page<MilestoneDto> secondPage = milestoneService.findByActivityIdInLifeStates(testActivity.getId(), secondPageable);

        // Then
        assertNotNull(secondPage);
        assertEquals(5, secondPage.getTotalElements());
        assertEquals(2, secondPage.getContent().size());
        assertEquals(1, secondPage.getNumber());
        assertEquals(3, secondPage.getSize());
        assertFalse(secondPage.hasNext());
    }

    @Test
    void testFindByActivityIdInLifeStatesWithNonExistentActivity() {
        // Given
        Long nonExistentActivityId = 999L;

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> result = milestoneService.findByActivityIdInLifeStates(nonExistentActivityId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void testFindByActivityIdInLifeStatesWithNullId() {
        // When & Then
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(IllegalArgumentException.class, () -> milestoneService.findByActivityIdInLifeStates(null, pageable));
    }

    @Test
    void testMoveMilestoneToFirst() {
        // Given
        createTestMilestoneWithOrder("First", 0);
        MilestoneEntity second = createTestMilestoneWithOrder("Second", 1);
        createTestMilestoneWithOrder("Third", 2);

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .milestoneOrder(0) // Перемещаем в начало
                .build();

        // When
        MilestoneDto result = milestoneService.update(second.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(0), result.getMilestoneOrder()); // Должен быть меньше первого

        // Проверяем, что порядок обновлен в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(second.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals(Integer.valueOf(0), savedMilestone.get().getMilestoneOrder());
    }

    @Test
    void testReorderMilestonesOnCreate() {
        // Given - создаем этапы с порядком 0, 1, 2
        createTestMilestoneWithOrder("First", 0);
        createTestMilestoneWithOrder("Second", 1);
        createTestMilestoneWithOrder("Third", 2);

        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("New Milestone")
                .activityId(testActivity.getId())
                .state(State.DRAFT)
                .milestoneOrder(1) // Вставляем в позицию 1
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getMilestoneOrder());

        // Проверяем, что порядки других этапов пересчитались
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> milestones = milestoneService.findByActivityId(testActivity.getId(), pageable);
        
        assertEquals(4, milestones.getTotalElements());
        
        // Проверяем порядок этапов
        List<MilestoneDto> content = milestones.getContent();
        assertEquals("First", content.get(0).getName());
        assertEquals(Integer.valueOf(0), content.get(0).getMilestoneOrder());
        
        assertEquals("New Milestone", content.get(1).getName());
        assertEquals(Integer.valueOf(1), content.get(1).getMilestoneOrder());
        
        assertEquals("Second", content.get(2).getName());
        assertEquals(Integer.valueOf(2), content.get(2).getMilestoneOrder());
        
        assertEquals("Third", content.get(3).getName());
        assertEquals(Integer.valueOf(3), content.get(3).getMilestoneOrder());
    }

    @Test
    void testReorderMilestonesOnUpdate() {
        // Given - создаем этапы с порядком 0, 1, 2, 3
        MilestoneEntity first = createTestMilestoneWithOrder("First", 0);
        createTestMilestoneWithOrder("Second", 1);
        createTestMilestoneWithOrder("Third", 2);
        createTestMilestoneWithOrder("Fourth", 3);

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .milestoneOrder(2) // Перемещаем первый этап в позицию 2
                .build();

        // When
        MilestoneDto result = milestoneService.update(first.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(2), result.getMilestoneOrder());

        // Проверяем, что порядки других этапов пересчитались
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> milestones = milestoneService.findByActivityId(testActivity.getId(), pageable);
        
        assertEquals(4, milestones.getTotalElements());
        
        // Проверяем порядок этапов
        List<MilestoneDto> content = milestones.getContent();
        assertEquals("Second", content.get(0).getName());
        assertEquals(Integer.valueOf(0), content.get(0).getMilestoneOrder());

        assertEquals("Third", content.get(1).getName());
        assertEquals(Integer.valueOf(1), content.get(1).getMilestoneOrder());

        assertEquals("First", content.get(2).getName());
        assertEquals(Integer.valueOf(2), content.get(2).getMilestoneOrder());

        assertEquals("Fourth", content.get(3).getName());
        assertEquals(Integer.valueOf(3), content.get(3).getMilestoneOrder());
    }

    @Test
    void testReorderMilestonesOnUpdateToLast() {
        // Given - создаем этапы с порядком 0, 1, 2
        MilestoneEntity first = createTestMilestoneWithOrder("First", 0);
        createTestMilestoneWithOrder("Second", 1);
        createTestMilestoneWithOrder("Third", 2);

        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .milestoneOrder(2)
                .build();

        // When
        MilestoneDto result = milestoneService.update(first.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(2), result.getMilestoneOrder());

        // Проверяем, что порядки других этапов пересчитались
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> milestones = milestoneService.findByActivityId(testActivity.getId(), pageable);
        
        assertEquals(3, milestones.getTotalElements());
        
        // Проверяем порядок этапов
        List<MilestoneDto> content = milestones.getContent();
        assertEquals("Second", content.get(0).getName());
        assertEquals(Integer.valueOf(0), content.get(0).getMilestoneOrder());
        
        assertEquals("Third", content.get(1).getName());
        assertEquals(Integer.valueOf(1), content.get(1).getMilestoneOrder());
        
        assertEquals("First", content.get(2).getName());
        assertEquals(Integer.valueOf(2), content.get(2).getMilestoneOrder());
    }


    // Вспомогательный метод для создания тестовой вехи
    private MilestoneEntity createTestMilestone(String name) {
        return transactionTemplate.execute(status -> {
            // Получаем максимальный порядок для данной активности
            List<MilestoneEntity> mstns = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(testActivity.getId());
            Integer maxOrder = mstns.isEmpty() ? null : mstns.get(mstns.size() - 1).getMilestoneOrder();
            Integer nextOrder = (maxOrder != null) ? maxOrder + 1 : 0;
            
            MilestoneEntity milestone = MilestoneEntity.builder()
                    .name(name)
                    .description("Test Milestone Description for " + name)
                    .state(State.DRAFT)
                    .activity(testActivity)
                    .milestoneOrder(nextOrder)
                    .build();
            return milestoneRepository.save(milestone);
        });
    }

    // Вспомогательный метод для создания тестовой вехи с порядком
    private MilestoneEntity createTestMilestoneWithOrder(String name, Integer order) {
        return transactionTemplate.execute(status -> {
            MilestoneEntity milestone = MilestoneEntity.builder()
                    .name(name)
                    .description("Test Milestone Description for " + name)
                    .state(State.DRAFT)
                    .activity(testActivity)
                    .milestoneOrder(order)
                    .build();
            return milestoneRepository.save(milestone);
        });
    }

    // Вспомогательный метод для создания тестовой вехи с определенным состоянием
    private MilestoneEntity createTestMilestoneWithState(String name, State state) {
        return transactionTemplate.execute(status -> {
            // Получаем максимальный порядок для данной активности
            List<MilestoneEntity> mstns = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(testActivity.getId());
            Integer maxOrder = mstns.isEmpty() ? null : mstns.get(mstns.size() - 1).getMilestoneOrder();
            Integer nextOrder = (maxOrder != null) ? maxOrder + 1 : 0;
            
            MilestoneEntity milestone = MilestoneEntity.builder()
                    .name(name)
                    .description("Test Milestone Description for " + name)
                    .state(state)
                    .activity(testActivity)
                    .milestoneOrder(nextOrder)
                    .build();
            return milestoneRepository.save(milestone);
        });
    }

}
