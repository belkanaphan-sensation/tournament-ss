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
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.milestone.service.dto.AssessmentMode;
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
                    .state(OccasionState.DRAFT)
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
                    .state(ActivityState.DRAFT)
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
                .state(MilestoneState.DRAFT)
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
        assertEquals(MilestoneState.DRAFT, result.getState());

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
                .state(MilestoneState.DRAFT)
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
        assertEquals(MilestoneState.DRAFT, result.get().getState());
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
                .state(MilestoneState.PLANNED)
                .build();

        // When
        MilestoneDto result = milestoneService.update(milestone.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(MilestoneState.PLANNED, result.getState());

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(milestone.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Updated Name", savedMilestone.get().getName());
        assertEquals("Updated Description", savedMilestone.get().getDescription());
        assertEquals(MilestoneState.PLANNED, savedMilestone.get().getState());
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
        assertEquals(MilestoneState.DRAFT, result.getState()); // Не изменилось

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(milestone.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Updated Name", savedMilestone.get().getName());
        assertEquals("Updated Description", savedMilestone.get().getDescription());
        assertEquals(MilestoneState.DRAFT, savedMilestone.get().getState());
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
                .state(MilestoneState.IN_PROGRESS)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(MilestoneState.IN_PROGRESS, result.getState());

        // Проверяем, что статус сохранен в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals(MilestoneState.IN_PROGRESS, savedMilestone.get().getState());
    }

    @Test
    void testMilestoneWithRoundStatistics() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // Создаем раунды с разными статусами
        RoundEntity completedRound = createTestRound(milestone, "Completed Round", RoundState.COMPLETED);
        RoundEntity activeRound = createTestRound(milestone, "Active Round", RoundState.IN_PROGRESS);
        RoundEntity draftRound = createTestRound(milestone, "Draft Round", RoundState.DRAFT);

        milestone.getRounds().addAll(Set.of(completedRound, activeRound, draftRound));
        milestoneRepository.save(milestone);
        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCompletedRoundsCount());
        assertNotNull(result.getTotalRoundsCount());
        assertEquals(1, result.getCompletedRoundsCount());
        assertEquals(3, result.getTotalRoundsCount());
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
        assertEquals(0, result.getCompletedRoundsCount());
        assertEquals(0, result.getTotalRoundsCount());
    }

    @Test
    void testFindAllMilestonesWithStatistics() {
        // Given
        MilestoneEntity milestone1 = createTestMilestone("Milestone 1");
        createTestMilestone("Milestone 2"); // This milestone is intentionally left without rounds for testing

        // Добавляем раунды к первому этапу
        RoundEntity testRound = createTestRound(milestone1, "Round 1", RoundState.COMPLETED);
        RoundEntity testRound1 = createTestRound(milestone1, "Round 2", RoundState.IN_PROGRESS);

        milestone1.getRounds().addAll(Set.of(testRound, testRound1));
        milestoneRepository.save(milestone1);

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
        assertEquals(1, milestoneWithRounds.getCompletedRoundsCount());
        assertEquals(2, milestoneWithRounds.getTotalRoundsCount());

        // Проверяем этап без раундов
        MilestoneDto milestoneWithoutRounds = result.getContent().stream()
                .filter(m -> m.getName().equals("Milestone 2"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestoneWithoutRounds);
        assertEquals(0, milestoneWithoutRounds.getCompletedRoundsCount());
        assertEquals(0, milestoneWithoutRounds.getTotalRoundsCount());
    }

    private RoundEntity createTestRound(MilestoneEntity milestone, String name, RoundState state) {
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
                .state(MilestoneState.DRAFT)
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
                .state(MilestoneState.DRAFT)
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
                .state(MilestoneState.DRAFT)
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
        assertNull(assignment.get().getPartnerSide()); // Критерий по умолчанию не привязан к роли
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
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment);

        // When - создаем новый этап
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Another Milestone")
                .activityId(testActivity.getId())
                .state(MilestoneState.DRAFT)
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
                .state(MilestoneState.DRAFT)
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
                .partnerSide(null)
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
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment1);

        MilestoneCriteriaAssignmentEntity assignment2 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(criteria2)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment2);

        MilestoneCriteriaAssignmentEntity assignment3 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(criteria3)
                .partnerSide(null)
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
                .state(MilestoneState.DRAFT)
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
                .state(MilestoneState.DRAFT)
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
                .state(MilestoneState.DRAFT)
                .build();

        CreateMilestoneRequest request2 = CreateMilestoneRequest.builder()
                .name("Second Milestone")
                .activityId(testActivity.getId())
                .state(MilestoneState.DRAFT)
                .build();

        CreateMilestoneRequest request3 = CreateMilestoneRequest.builder()
                .name("Third Milestone")
                .activityId(testActivity.getId())
                .state(MilestoneState.DRAFT)
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
                .state(MilestoneState.DRAFT)
                .milestoneOrder(0)
                .build();

        CreateMilestoneRequest request2 = CreateMilestoneRequest.builder()
                .name("Second Milestone")
                .activityId(testActivity.getId())
                .state(MilestoneState.DRAFT)
                .milestoneOrder(1)
                .build();

        CreateMilestoneRequest request3 = CreateMilestoneRequest.builder()
                .name("Third Milestone")
                .activityId(testActivity.getId())
                .state(MilestoneState.DRAFT)
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
        List<MilestoneDto> result = milestoneService.findByActivityId(testActivity.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Проверяем, что этапы отсортированы по порядку
        assertEquals("Second", result.get(0).getName());
        assertEquals("Third", result.get(1).getName());
        assertEquals("First", result.get(2).getName());
    }

    @Test
    void testFindByActivityIdInLifeStates() {
        // Given - создаем этапы с разными состояниями
        createTestMilestoneWithState("Milestone DRAFT", MilestoneState.DRAFT);
        createTestMilestoneWithState("Milestone PLANNED", MilestoneState.PLANNED);
        createTestMilestoneWithState("Milestone IN_PROGRESS", MilestoneState.IN_PROGRESS);
        createTestMilestoneWithState("Milestone COMPLETED", MilestoneState.COMPLETED);

        // When
        List<MilestoneDto> result = milestoneService.findByActivityIdInLifeStates(testActivity.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size()); // Только PLANNED, IN_PROGRESS, COMPLETED

        // Проверяем, что все этапы принадлежат правильной активности и имеют life states
        result.forEach(milestone -> {
            assertNotNull(milestone.getActivity());
            assertEquals(testActivity.getId(), milestone.getActivity().getId());
            assertTrue(MilestoneState.LIFE_MILESTONE_STATES.contains(milestone.getState()));
        });

        // Проверяем, что DRAFT этапа нет в результате
        boolean hasDraftMilestone = result.stream()
                .anyMatch(milestone -> milestone.getState() == MilestoneState.DRAFT);
        assertFalse(hasDraftMilestone);
    }

    @Test
    void testFindByActivityIdInLifeStatesWithManyMilestones() {
        // Given - создаем 5 этапов с life states
        for (int i = 1; i <= 5; i++) {
            createTestMilestoneWithState("Milestone " + i, MilestoneState.PLANNED);
        }

        // When
        List<MilestoneDto> result = milestoneService.findByActivityIdInLifeStates(testActivity.getId());

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());

        // Проверяем, что все этапы принадлежат правильной активности и имеют life states
        result.forEach(milestone -> {
            assertNotNull(milestone.getActivity());
            assertEquals(testActivity.getId(), milestone.getActivity().getId());
            assertTrue(MilestoneState.LIFE_MILESTONE_STATES.contains(milestone.getState()));
        });

        // Проверяем, что все этапы присутствуют в результате
        for (int i = 1; i <= 5; i++) {
            final int index = i;
            assertTrue(result.stream()
                    .anyMatch(milestone -> ("Milestone " + index).equals(milestone.getName())));
        }
    }

    @Test
    void testFindByActivityIdInLifeStatesWithNonExistentActivity() {
        // Given
        Long nonExistentActivityId = 999L;

        // When
        List<MilestoneDto> result = milestoneService.findByActivityIdInLifeStates(nonExistentActivityId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByActivityIdInLifeStatesWithNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> milestoneService.findByActivityIdInLifeStates(null));
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
                .state(MilestoneState.DRAFT)
                .milestoneOrder(1) // Вставляем в позицию 1
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getMilestoneOrder());

        // Проверяем, что порядки других этапов пересчитались
        List<MilestoneDto> milestones = milestoneService.findByActivityId(testActivity.getId());

        assertEquals(4, milestones.size());

        // Проверяем порядок этапов
        assertEquals("First", milestones.get(0).getName());
        assertEquals(Integer.valueOf(0), milestones.get(0).getMilestoneOrder());

        assertEquals("New Milestone", milestones.get(1).getName());
        assertEquals(Integer.valueOf(1), milestones.get(1).getMilestoneOrder());

        assertEquals("Second", milestones.get(2).getName());
        assertEquals(Integer.valueOf(2), milestones.get(2).getMilestoneOrder());

        assertEquals("Third", milestones.get(3).getName());
        assertEquals(Integer.valueOf(3), milestones.get(3).getMilestoneOrder());
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
        List<MilestoneDto> milestones = milestoneService.findByActivityId(testActivity.getId());

        assertEquals(4, milestones.size());

        // Проверяем порядок этапов
        assertEquals("Second", milestones.get(0).getName());
        assertEquals(Integer.valueOf(0), milestones.get(0).getMilestoneOrder());

        assertEquals("Third", milestones.get(1).getName());
        assertEquals(Integer.valueOf(1), milestones.get(1).getMilestoneOrder());

        assertEquals("First", milestones.get(2).getName());
        assertEquals(Integer.valueOf(2), milestones.get(2).getMilestoneOrder());

        assertEquals("Fourth", milestones.get(3).getName());
        assertEquals(Integer.valueOf(3), milestones.get(3).getMilestoneOrder());
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
        List<MilestoneDto> milestones = milestoneService.findByActivityId(testActivity.getId());

        assertEquals(3, milestones.size());

        // Проверяем порядок этапов
        assertEquals("Second", milestones.get(0).getName());
        assertEquals(Integer.valueOf(0), milestones.get(0).getMilestoneOrder());

        assertEquals("Third", milestones.get(1).getName());
        assertEquals(Integer.valueOf(1), milestones.get(1).getMilestoneOrder());

        assertEquals("First", milestones.get(2).getName());
        assertEquals(Integer.valueOf(2), milestones.get(2).getMilestoneOrder());
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
                    .state(MilestoneState.DRAFT)
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
                    .state(MilestoneState.DRAFT)
                    .activity(testActivity)
                    .milestoneOrder(order)
                    .build();
            return milestoneRepository.save(milestone);
        });
    }

    // Вспомогательный метод для создания тестовой вехи с определенным состоянием
    private MilestoneEntity createTestMilestoneWithState(String name, MilestoneState state) {
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

    // ==================== ТЕСТЫ ДЛЯ AssessmentMode ====================

    @Test
    @Transactional
    void testAssessmentModePassWithSingleCriteriaScale1() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // Создаем одно назначение критерия с scale = 1
        MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment);

        // Добавляем назначение в коллекцию milestone'а
        milestone.getCriteriaAssignments().add(assignment);
        milestoneRepository.save(milestone);

        // Проверяем, что назначение создано
        long assignmentCount = milestoneCriteriaAssignmentRepository.countByMilestoneId(milestone.getId());
        assertEquals(1, assignmentCount);

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertEquals(AssessmentMode.PASS, result.getAssessmentMode());
    }

    @Test
    @Transactional
    void testAssessmentModeCriteriaWithSingleCriteriaScaleNot1() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // Создаем одно назначение критерия с scale = 5
        MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(5)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment);

        // Добавляем назначение в коллекцию milestone'а
        milestone.getCriteriaAssignments().add(assignment);
        milestoneRepository.save(milestone);

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertEquals(AssessmentMode.CRITERIA, result.getAssessmentMode());
    }

    @Test
    @Transactional
    void testAssessmentModeCriteriaWithMultipleCriteria() {
        // Given
        // Создаем дополнительные критерии
        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Техника")
                .build();
        criteriaRepository.save(criteria2);

        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // Создаем два назначения критериев
        MilestoneCriteriaAssignmentEntity assignment1 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment1);

        MilestoneCriteriaAssignmentEntity assignment2 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(criteria2)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment2);

        // Добавляем назначения в коллекцию milestone'а
        milestone.getCriteriaAssignments().add(assignment1);
        milestone.getCriteriaAssignments().add(assignment2);
        milestoneRepository.save(milestone);

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertEquals(AssessmentMode.CRITERIA, result.getAssessmentMode());
    }

    @Test
    @Transactional
    void testAssessmentModeCriteriaWithNoCriteriaAssignments() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");
        // Не создаем никаких назначений критериев

        // When
        MilestoneDto result = milestoneService.findById(milestone.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertEquals(AssessmentMode.CRITERIA, result.getAssessmentMode());
    }

    @Test
    @Transactional
    void testAssessmentModeWithDefaultCriteria() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .activityId(testActivity.getId())
                .state(MilestoneState.DRAFT)
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(AssessmentMode.PASS, result.getAssessmentMode()); // Критерий "Прохождение" имеет scale = 1

        // Проверяем, что назначение критерия создано
        long assignmentCount = milestoneCriteriaAssignmentRepository.countByMilestoneId(result.getId());
        assertEquals(1, assignmentCount);
    }

    @Test
    @Transactional
    void testAssessmentModeInFindAll() {
        // Given
        MilestoneEntity milestone1 = createTestMilestone("Milestone 1");
        MilestoneEntity milestone2 = createTestMilestone("Milestone 2");

        // Создаем назначения критериев
        MilestoneCriteriaAssignmentEntity assignment1 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone1)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment1);

        MilestoneCriteriaAssignmentEntity assignment2 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone2)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(5)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment2);

        // Добавляем назначения в коллекции milestone'ов
        milestone1.getCriteriaAssignments().add(assignment1);
        milestone2.getCriteriaAssignments().add(assignment2);
        milestoneRepository.save(milestone1);
        milestoneRepository.save(milestone2);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilestoneDto> result = milestoneService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        // Проверяем AssessmentMode для каждого milestone
        MilestoneDto milestone1Dto = result.getContent().stream()
                .filter(m -> m.getName().equals("Milestone 1"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestone1Dto);
        assertEquals(AssessmentMode.PASS, milestone1Dto.getAssessmentMode());

        MilestoneDto milestone2Dto = result.getContent().stream()
                .filter(m -> m.getName().equals("Milestone 2"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestone2Dto);
        assertEquals(AssessmentMode.CRITERIA, milestone2Dto.getAssessmentMode());
    }

    @Test
    @Transactional
    void testAssessmentModeInFindByActivityId() {
        // Given
        MilestoneEntity milestone1 = createTestMilestone("Milestone 1");
        MilestoneEntity milestone2 = createTestMilestone("Milestone 2");

        // Создаем назначения критериев
        MilestoneCriteriaAssignmentEntity assignment1 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone1)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment1);

        MilestoneCriteriaAssignmentEntity assignment2 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone2)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(3)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment2);

        // Добавляем назначения в коллекции milestone'ов
        milestone1.getCriteriaAssignments().add(assignment1);
        milestone2.getCriteriaAssignments().add(assignment2);
        milestoneRepository.save(milestone1);
        milestoneRepository.save(milestone2);

        // When
        List<MilestoneDto> result = milestoneService.findByActivityId(testActivity.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Проверяем AssessmentMode для каждого milestone
        MilestoneDto milestone1Dto = result.stream()
                .filter(m -> m.getName().equals("Milestone 1"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestone1Dto);
        assertEquals(AssessmentMode.PASS, milestone1Dto.getAssessmentMode());

        MilestoneDto milestone2Dto = result.stream()
                .filter(m -> m.getName().equals("Milestone 2"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestone2Dto);
        assertEquals(AssessmentMode.CRITERIA, milestone2Dto.getAssessmentMode());
    }

    @Test
    @Transactional
    void testAssessmentModeInFindByActivityIdInLifeStates() {
        // Given
        MilestoneEntity milestone1 = createTestMilestoneWithState("Milestone 1", MilestoneState.PLANNED);
        MilestoneEntity milestone2 = createTestMilestoneWithState("Milestone 2", MilestoneState.IN_PROGRESS);

        // Создаем назначения критериев
        MilestoneCriteriaAssignmentEntity assignment1 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone1)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment1);

        MilestoneCriteriaAssignmentEntity assignment2 = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone2)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(2)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment2);

        // Добавляем назначения в коллекции milestone'ов
        milestone1.getCriteriaAssignments().add(assignment1);
        milestone2.getCriteriaAssignments().add(assignment2);
        milestoneRepository.save(milestone1);
        milestoneRepository.save(milestone2);

        // When
        List<MilestoneDto> result = milestoneService.findByActivityIdInLifeStates(testActivity.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Проверяем AssessmentMode для каждого milestone
        MilestoneDto milestone1Dto = result.stream()
                .filter(m -> m.getName().equals("Milestone 1"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestone1Dto);
        assertEquals(AssessmentMode.PASS, milestone1Dto.getAssessmentMode());

        MilestoneDto milestone2Dto = result.stream()
                .filter(m -> m.getName().equals("Milestone 2"))
                .findFirst()
                .orElse(null);
        assertNotNull(milestone2Dto);
        assertEquals(AssessmentMode.CRITERIA, milestone2Dto.getAssessmentMode());
    }

    @Test
    @Transactional
    void testAssessmentModeAfterUpdate() {
        // Given
        MilestoneEntity milestone = createTestMilestone("Test Milestone");

        // Создаем назначение критерия с scale = 1
        MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(milestone)
                .criteria(testCriteria)
                .partnerSide(null)
                .scale(1)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment);

        // Добавляем назначение в коллекцию milestone'а
        milestone.getCriteriaAssignments().add(assignment);
        milestoneRepository.save(milestone);

        // Проверяем, что изначально AssessmentMode = PASS
        MilestoneDto initialResult = milestoneService.findById(milestone.getId()).orElse(null);
        assertNotNull(initialResult);
        assertEquals(AssessmentMode.PASS, initialResult.getAssessmentMode());

        // When - обновляем milestone
        UpdateMilestoneRequest request = UpdateMilestoneRequest.builder()
                .name("Updated Milestone")
                .build();

        MilestoneDto result = milestoneService.update(milestone.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Milestone", result.getName());
        assertEquals(AssessmentMode.PASS, result.getAssessmentMode()); // AssessmentMode должен остаться тем же
    }

}
