package org.bn.sensation.core.milestone.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.repository.CriterionRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.PrepareRoundsRequest;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.repository.MilestoneCriterionRepository;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.security.CurrentUser;
import org.bn.sensation.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class MilestoneServiceIntegrationTest extends AbstractIntegrationTest {

    @Mock
    private CurrentUser mockCurrentUser;

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
    private CriterionRepository criterionRepository;

    @Autowired
    private MilestoneCriterionRepository milestoneCriterionRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ActivityEntity testActivity;
    private OrganizationEntity testOrganization;
    private UserEntity testUser;
    private CriterionEntity testCriterion;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            milestoneCriterionRepository.deleteAll();
            milestoneRuleRepository.deleteAll();
            milestoneRepository.deleteAll();
            criterionRepository.deleteAll();
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
                    .roles(Set.of(Role.SUPERADMIN))
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
            testCriterion = CriterionEntity.builder()
                    .name("Прохождение")
                    .build();
            testCriterion = criterionRepository.save(testCriterion);


            return null;
        });

        // Set up security context with judge user
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock CurrentUser to return the test judge
        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);
    }

    @Test
    void testCreateMilestone() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(testActivity.getId())
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
                .build();

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
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
                .build();

        // When
        MilestoneDto result = milestoneService.update(milestone.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(MilestoneState.DRAFT, result.getState());

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(milestone.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals("Updated Name", savedMilestone.get().getName());
        assertEquals("Updated Description", savedMilestone.get().getDescription());
        assertEquals(MilestoneState.DRAFT, savedMilestone.get().getState());
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
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
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
        assertThrows(EntityNotFoundException.class, () -> {
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
                .build();

        // When
        MilestoneDto result = milestoneService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(MilestoneState.DRAFT, result.getState());

        // Проверяем, что статус сохранен в БД
        Optional<MilestoneEntity> savedMilestone = milestoneRepository.findById(result.getId());
        assertTrue(savedMilestone.isPresent());
        assertEquals(MilestoneState.DRAFT, savedMilestone.get().getState());
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
                    .state(state)
                    .milestone(milestone)
                    .roundOrder(0)
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
                .build();

        // When and then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.create(request);
        });
    }

    @Test
    void testCreateMilestoneWithOrder() {
        // Given
        CreateMilestoneRequest request = CreateMilestoneRequest.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activityId(testActivity.getId())
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
                .build();

        CreateMilestoneRequest request2 = CreateMilestoneRequest.builder()
                .name("Second Milestone")
                .activityId(testActivity.getId())
                .build();

        CreateMilestoneRequest request3 = CreateMilestoneRequest.builder()
                .name("Third Milestone")
                .activityId(testActivity.getId())
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
                .milestoneOrder(0)
                .build();

        CreateMilestoneRequest request2 = CreateMilestoneRequest.builder()
                .name("Second Milestone")
                .activityId(testActivity.getId())
                .milestoneOrder(1)
                .build();

        CreateMilestoneRequest request3 = CreateMilestoneRequest.builder()
                .name("Third Milestone")
                .activityId(testActivity.getId())
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

        List<MilestoneEntity> milestones = milestoneRepository.findByActivityIdOrderByMilestoneOrderDesc(testActivity.getId());
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
            List<MilestoneEntity> mstns = milestoneRepository.findByActivityIdOrderByMilestoneOrderDesc(testActivity.getId());
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
            List<MilestoneEntity> mstns = milestoneRepository.findByActivityIdOrderByMilestoneOrderDesc(testActivity.getId());
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

    // ========== Tests for State Transition Methods ==========

    @Test
    void testDraftMilestone_FromPlanned_ShouldChangeStateToDraft() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.PLANNED);

        // When
        milestoneService.draftMilestone(milestone.getId());

        // Then
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.DRAFT, savedMilestone.getState());
    }

    @Test
    void testDraftMilestone_FromInvalidState_ShouldThrowException() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.IN_PROGRESS);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            milestoneService.draftMilestone(milestone.getId());
        });
    }

    @Test
    void testPlanMilestone_FromDraft_ShouldChangeStateToPlanned() {
        // Given
        testActivity.setState(ActivityState.IN_PROGRESS);
        activityRepository.save(testActivity);
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.DRAFT);
        createMilestoneRuleForMilestone(milestone);

        // When
        milestoneService.planMilestone(milestone.getId());

        // Then
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.PLANNED, savedMilestone.getState());
    }

    @Test
    void testPlanMilestone_FromInvalidState_ShouldThrowException() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.COMPLETED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            milestoneService.planMilestone(milestone.getId());
        });
    }

    @Test
    void testPrepareRounds_FromPlanned_ShouldGenerateRoundsAndChangeStateToPending() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.PLANNED);
        createMilestoneRuleForMilestone(milestone);

        PrepareRoundsRequest request = PrepareRoundsRequest.builder()
                .reGenerate(false)
                .build();

        // When
        List<RoundDto> rounds = milestoneService.prepareRounds(milestone.getId(), request);

        // Then
        assertNotNull(rounds);
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.PENDING, savedMilestone.getState());
    }

    @Test
    void testPrepareRounds_FromPending_ShouldRegenerateRounds() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.PENDING);
        createMilestoneRuleForMilestone(milestone);

        // Create existing rounds
        RoundEntity existingRound = createTestRound(milestone, "Existing Round", RoundState.PLANNED);
        milestone.getRounds().add(existingRound);
        milestoneRepository.save(milestone);

        PrepareRoundsRequest request = PrepareRoundsRequest.builder()
                .reGenerate(true)
                .build();

        // When
        List<RoundDto> rounds = milestoneService.prepareRounds(milestone.getId(), request);

        // Then
        assertNotNull(rounds);
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.PENDING, savedMilestone.getState());
    }

    @Test
    void testPrepareRounds_FromInvalidState_ShouldThrowException() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.COMPLETED);
        createMilestoneRuleForMilestone(milestone);

        PrepareRoundsRequest request = PrepareRoundsRequest.builder()
                .reGenerate(false)
                .build();

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            milestoneService.prepareRounds(milestone.getId(), request);
        });
    }

    @Test
    void testStartMilestone_FromPending_ShouldChangeStateToInProgress() {
        // Given
        testActivity.setState(ActivityState.IN_PROGRESS);
        activityRepository.save(testActivity);
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.PENDING);
        RoundEntity round1 = createTestRound(milestone, "Round 1", RoundState.PLANNED);
        milestone.getRounds().add(round1);
        milestoneRepository.save(milestone);

        // Create participants and add them to milestone and round
        ParticipantEntity participant1 = createTestParticipant("001", PartnerSide.LEADER);
        ParticipantEntity participant2 = createTestParticipant("002", PartnerSide.FOLLOWER);
        
        milestone.getParticipants().add(participant1);
        milestone.getParticipants().add(participant2);
        round1.getParticipants().add(participant1);
        round1.getParticipants().add(participant2);
        
        participant1.getMilestones().add(milestone);
        participant1.getRounds().add(round1);
        participant2.getMilestones().add(milestone);
        participant2.getRounds().add(round1);
        
        milestoneRepository.save(milestone);
        roundRepository.save(round1);
        participantRepository.save(participant1);
        participantRepository.save(participant2);

        // When
        milestoneService.startMilestone(milestone.getId());

        // Then
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.IN_PROGRESS, savedMilestone.getState());
    }

    @Test
    void testStartMilestone_FromInvalidState_ShouldThrowException() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.COMPLETED);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            milestoneService.startMilestone(milestone.getId());
        });
    }

    @Test
    void testSumUpMilestone_FromInProgress_ShouldCompleteRoundsAndChangeStateToSummarizing() {
        // Given
        testActivity.setState(ActivityState.IN_PROGRESS);
        activityRepository.save(testActivity);
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.IN_PROGRESS);
        RoundEntity round1 = createTestRound(milestone, "Round 1", RoundState.READY);
        RoundEntity round2 = createTestRound(milestone, "Round 2", RoundState.READY);
        RoundEntity completedRound = createTestRound(milestone, "Completed Round", RoundState.COMPLETED);
        
        milestone.getRounds().addAll(Set.of(round1, round2, completedRound));
        milestoneRepository.save(milestone);
        createMilestoneRuleForMilestone(milestone);
        // When
        List<MilestoneResultDto> results = milestoneService.sumUpMilestone(milestone.getId());

        // Then
        assertNotNull(results);
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.SUMMARIZING, savedMilestone.getState());
        
        // Verify that non-completed rounds are completed
        RoundEntity savedRound1 = roundRepository.findById(round1.getId()).orElseThrow();
        RoundEntity savedRound2 = roundRepository.findById(round2.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, savedRound1.getState());
        assertEquals(RoundState.COMPLETED, savedRound2.getState());
        
        // Completed round should remain completed
        RoundEntity savedCompletedRound = roundRepository.findById(completedRound.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, savedCompletedRound.getState());
    }

    @Test
    void testSumUpMilestone_FromInvalidState_ShouldThrowException() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.DRAFT);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            milestoneService.sumUpMilestone(milestone.getId());
        });
    }

    @Test
    void testCompleteMilestone_FromSummarizing_ShouldCompleteAllRoundsAndChangeStateToCompleted() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.SUMMARIZING);
        RoundEntity round1 = createTestRound(milestone, "Round 1", RoundState.READY);
        RoundEntity round2 = createTestRound(milestone, "Round 2", RoundState.READY);
        
        milestone.getRounds().addAll(Set.of(round1, round2));
        milestoneRepository.save(milestone);

        // When
        milestoneService.completeMilestone(milestone.getId(), List.of());

        // Then
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.COMPLETED, savedMilestone.getState());
        
        // Verify that all rounds are completed
        RoundEntity savedRound1 = roundRepository.findById(round1.getId()).orElseThrow();
        RoundEntity savedRound2 = roundRepository.findById(round2.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, savedRound1.getState());
        assertEquals(RoundState.COMPLETED, savedRound2.getState());
    }

    @Test
    void testCompleteMilestone_WithNoRounds_ShouldChangeStateToCompleted() {
        // Given
        MilestoneEntity milestone = createTestMilestoneWithState("Test Milestone", MilestoneState.SUMMARIZING);

        // When
        milestoneService.completeMilestone(milestone.getId(), List.of());

        // Then
        MilestoneEntity savedMilestone = milestoneRepository.findById(milestone.getId()).orElseThrow();
        assertEquals(MilestoneState.COMPLETED, savedMilestone.getState());
    }


    // ========== Helper Methods for State Transition Tests ==========

    private ParticipantEntity createTestParticipant(String number, PartnerSide partnerSide) {
        return transactionTemplate.execute(status -> {
            ParticipantEntity participant = ParticipantEntity.builder()
                    .person(Person.builder()
                            .name("Test")
                            .surname("Participant")
                            .email("participant" + number + "@test.com")
                            .phoneNumber("+1234567890")
                            .build())
                    .number(number)
                    .isRegistered(true)
                    .partnerSide(partnerSide)
                    .activity(testActivity)
                    .rounds(new HashSet<>())
                    .milestones(new HashSet<>())
                    .build();
            return participantRepository.save(participant);
        });
    }

    private MilestoneRuleEntity createMilestoneRuleForMilestone(MilestoneEntity milestone) {
        return transactionTemplate.execute(status -> {
            MilestoneRuleEntity rule = MilestoneRuleEntity.builder()
                    .milestone(milestone)
                    .assessmentMode(AssessmentMode.SCORE)
                    .participantLimit(10)
                    .roundParticipantLimit(3)
                    .strictPassMode(false)
                    .build();
            rule = milestoneRuleRepository.save(rule);
            MilestoneCriterionEntity milestoneCriterion = MilestoneCriterionEntity.builder()
                    .milestoneRule(rule)
                    .criterion(testCriterion)
                    .weight(BigDecimal.ONE)
                    .scale(2)
                    .build();
            milestoneCriterion = milestoneCriterionRepository.save(milestoneCriterion);
            rule.getMilestoneCriteria().add(milestoneCriterion);
            milestoneRuleRepository.save(rule);
            milestone.setMilestoneRule(rule);
            milestoneRepository.save(milestone);
            
            return rule;
        });
    }

}
