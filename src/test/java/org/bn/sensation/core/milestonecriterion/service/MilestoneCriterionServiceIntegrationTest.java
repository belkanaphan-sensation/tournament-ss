package org.bn.sensation.core.milestonecriterion.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.repository.CriterionRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.repository.MilestoneCriterionRepository;
import org.bn.sensation.core.milestonecriterion.service.dto.CreateMilestoneCriterionRequest;
import org.bn.sensation.core.milestonecriterion.service.dto.MilestoneCriterionDto;
import org.bn.sensation.core.milestonecriterion.service.dto.UpdateMilestoneCriterionRequest;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class MilestoneCriterionServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MilestoneCriterionService milestoneCriterionService;

    @Autowired
    private MilestoneCriterionRepository milestoneCriterionRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private CriterionRepository criterionRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityUserRepository activityUserRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private MilestoneEntity testMilestone;
    private MilestoneRuleEntity testMilestoneRule;
    private CriterionEntity testCriteria;
    private OrganizationEntity testOrganization;
    private UserEntity testUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            milestoneCriterionRepository.deleteAll();
            milestoneRepository.deleteAll();
            milestoneRuleRepository.deleteAll();
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
                    .state(OccasionState.PLANNED)
                    .organization(testOrganization)
                    .build();
            testOccasion = occasionRepository.save(testOccasion);

            // Создание тестовой активности
            ActivityEntity testActivity = ActivityEntity.builder()
                    .name("Test Activity")
                    .description("Test Description")
                    .startDateTime(java.time.LocalDateTime.now())
                    .endDateTime(java.time.LocalDateTime.now().plusHours(2))
                    .address(Address.builder()
                            .country("Russia")
                            .city("Moscow")
                            .streetName("Activity Street")
                            .streetNumber("2")
                            .comment("Activity Address")
                            .build())
                    .state(ActivityState.PLANNED)
                    .occasion(testOccasion)
                    .build();
            testActivity = activityRepository.save(testActivity);

            // Создание тестового этапа
            testMilestone = MilestoneEntity.builder()
                    .name("Test Milestone")
                    .state(MilestoneState.DRAFT)
                    .activity(testActivity)
                    .milestoneOrder(1)
                    .build();
            testMilestone = milestoneRepository.save(testMilestone);

            // Создание тестового правила этапа
            testMilestoneRule = MilestoneRuleEntity.builder()
                    .assessmentMode(AssessmentMode.SCORE)
                    .participantLimit(10)
                    .roundParticipantLimit(10)
                    .milestone(testMilestone)
                    .build();
            testMilestoneRule = milestoneRuleRepository.save(testMilestoneRule);

            // Связываем этап с правилом
            testMilestone.setMilestoneRule(testMilestoneRule);
            testMilestone = milestoneRepository.save(testMilestone);

            // Создание тестового критерия
            testCriteria = CriterionEntity.builder()
                    .name("Техника")
                    .build();
            testCriteria = criterionRepository.save(testCriteria);

            return null;
        });
    }

    @Test
    void testCreateMilestoneCriteriaAssignment() {
        // Given
        CreateMilestoneCriterionRequest request = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(testMilestoneRule.getId())
                .criterionId(testCriteria.getId())
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();

        // When
        MilestoneCriterionDto result = milestoneCriterionService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testMilestoneRule.getId(), result.getMilestoneRule().getId());
        assertEquals(testCriteria.getId(), result.getCriterion().getId());
        assertEquals(PartnerSide.LEADER, result.getPartnerSide());

        // Проверяем, что назначение сохранено в БД
        Optional<MilestoneCriterionEntity> savedAssignment = milestoneCriterionRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertEquals(testMilestoneRule.getId(), savedAssignment.get().getMilestoneRule().getId());
        assertEquals(testCriteria.getId(), savedAssignment.get().getCriterion().getId());
        assertEquals(PartnerSide.LEADER, savedAssignment.get().getPartnerSide());
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNullCompetitionRole() {
        // Given
        CreateMilestoneCriterionRequest request = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(testMilestoneRule.getId())
                .criterionId(testCriteria.getId())
                .partnerSide(null)
                .scale(10)
                .build();

        // When
        MilestoneCriterionDto result = milestoneCriterionService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testMilestoneRule.getId(), result.getMilestoneRule().getId());
        assertEquals(testCriteria.getId(), result.getCriterion().getId());
        assertNull(result.getPartnerSide());

        // Проверяем, что назначение сохранено в БД
        Optional<MilestoneCriterionEntity> savedAssignment = milestoneCriterionRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertNull(savedAssignment.get().getPartnerSide());
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNonExistentMilestoneRule() {
        // Given
        CreateMilestoneCriterionRequest request = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(999L) // Несуществующее правило этапа
                .criterionId(testCriteria.getId())
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            milestoneCriterionService.create(request);
        });
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNonExistentCriteria() {
        // Given
        CreateMilestoneCriterionRequest request = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(testMilestoneRule.getId())
                .criterionId(999L) // Несуществующий критерий
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            milestoneCriterionService.create(request);
        });
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithExistingAssignment() {
        // Given
        // Создаем первое назначение
        CreateMilestoneCriterionRequest request1 = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(testMilestoneRule.getId())
                .criterionId(testCriteria.getId())
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();
        milestoneCriterionService.create(request1);

        // Создаем второе назначение с теми же правилом этапа и критерием
        CreateMilestoneCriterionRequest request2 = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(testMilestoneRule.getId())
                .criterionId(testCriteria.getId())
                .partnerSide(PartnerSide.FOLLOWER)
                .scale(10)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneCriterionService.create(request2);
        });
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentPartial() {
        // Given
        MilestoneCriterionEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        UpdateMilestoneCriterionRequest request = UpdateMilestoneCriterionRequest.builder()
                .partnerSide(PartnerSide.FOLLOWER)
                .build();

        // When
        MilestoneCriterionDto result = milestoneCriterionService.update(assignment.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestoneRule.getId(), result.getMilestoneRule().getId());
        assertEquals(testCriteria.getId(), result.getCriterion().getId()); // Не изменилось
        assertEquals(PartnerSide.FOLLOWER, result.getPartnerSide());

        // Проверяем, что назначение обновлено в БД
        Optional<MilestoneCriterionEntity> updatedAssignment = milestoneCriterionRepository.findById(assignment.getId());
        assertTrue(updatedAssignment.isPresent());
        assertEquals(testCriteria.getId(), updatedAssignment.get().getCriterion().getId());
        assertEquals(PartnerSide.FOLLOWER, updatedAssignment.get().getPartnerSide());
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentNotFound() {
        // Given
        UpdateMilestoneCriterionRequest request = UpdateMilestoneCriterionRequest.builder()
                .partnerSide(PartnerSide.FOLLOWER)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriterionService.update(999L, request);
        });
    }

    @Test
    void testFindAllMilestoneCriteriaAssignments() {
        // Given
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // Создаем дополнительные критерии и назначения
        CriterionEntity criteria2 = CriterionEntity.builder()
                .name("Ведение")
                .build();
        criterionRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriterionEntity criteria3 = CriterionEntity.builder()
                .name("Музыкальность")
                .build();
        criterionRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MilestoneCriterionDto> result = milestoneCriterionService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindMilestoneCriteriaAssignmentById() {
        // Given
        MilestoneCriterionEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // When
        Optional<MilestoneCriterionDto> result = milestoneCriterionService.findById(assignment.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(assignment.getId(), result.get().getId());
        assertEquals(testMilestoneRule.getId(), result.get().getMilestoneRule().getId());
        assertEquals(testCriteria.getId(), result.get().getCriterion().getId());
        assertEquals(PartnerSide.LEADER, result.get().getPartnerSide());
    }

    @Test
    void testFindMilestoneCriteriaAssignmentByIdNotFound() {
        // When
        Optional<MilestoneCriterionDto> result = milestoneCriterionService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByMilestoneIdAndCriterionId() {
        // Given
        MilestoneCriterionEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // When
        MilestoneCriterionDto result = milestoneCriterionService.findByMilestoneIdAndCriterionId(
                testMilestone.getId(), testCriteria.getId());

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestoneRule.getId(), result.getMilestoneRule().getId());
        assertEquals(testCriteria.getId(), result.getCriterion().getId());
        assertEquals(PartnerSide.LEADER, result.getPartnerSide());
    }

    @Test
    void testFindByMilestoneIdAndCriterionIdNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriterionService.findByMilestoneIdAndCriterionId(999L, 999L);
        });
    }

    @Test
    void testFindByMilestoneId() {
        // Given
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // Создаем дополнительные критерии и назначения для того же этапа
        CriterionEntity criteria2 = CriterionEntity.builder()
                .name("Ведение")
                .build();
        criterionRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        // Создаем назначение для другого этапа
        MilestoneEntity anotherMilestone = MilestoneEntity.builder()
                .name("Another Milestone")
                .state(MilestoneState.DRAFT)
                .activity(testMilestone.getActivity())
                .milestoneOrder(2)
                .build();
        anotherMilestone = milestoneRepository.save(anotherMilestone);

        // Создаем правило для другого этапа
        MilestoneRuleEntity anotherMilestoneRule = MilestoneRuleEntity.builder()
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .milestone(anotherMilestone)
                .build();
        anotherMilestoneRule = milestoneRuleRepository.save(anotherMilestoneRule);

        // Связываем этап с правилом
        anotherMilestone.setMilestoneRule(anotherMilestoneRule);
        anotherMilestone = milestoneRepository.save(anotherMilestone);

        createTestAssignment(anotherMilestone, testCriteria, PartnerSide.LEADER);

        // When
        List<MilestoneCriterionDto> result = milestoneCriterionService.findByMilestoneId(
                testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindByCriterionId() {
        // Given
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // Создаем дополнительные этапы и назначения для того же критерия
        MilestoneEntity milestone2 = MilestoneEntity.builder()
                .name("Milestone 2")
                .state(MilestoneState.DRAFT)
                .activity(testMilestone.getActivity())
                .milestoneOrder(3)
                .build();
        milestone2 = milestoneRepository.save(milestone2);

        // Создаем правило для второго этапа
        MilestoneRuleEntity milestone2Rule = MilestoneRuleEntity.builder()
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .milestone(milestone2)
                .build();
        milestone2Rule = milestoneRuleRepository.save(milestone2Rule);

        // Связываем этап с правилом
        milestone2.setMilestoneRule(milestone2Rule);
        milestone2 = milestoneRepository.save(milestone2);

        createTestAssignment(milestone2, testCriteria, PartnerSide.FOLLOWER);

        // Создаем назначение для другого критерия
        CriterionEntity anotherCriteria = CriterionEntity.builder()
                .name("Стилистика")
                .build();
        criterionRepository.save(anotherCriteria);
        createTestAssignment(testMilestone, anotherCriteria, PartnerSide.LEADER);

        // When
        List<MilestoneCriterionDto> result = milestoneCriterionService.findByCriterionId(
                testCriteria.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testDeleteMilestoneCriteriaAssignment() {
        // Given
        MilestoneCriterionEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);
        Long assignmentId = assignment.getId();

        // When
        milestoneCriterionService.deleteById(assignmentId);

        // Then
        assertFalse(milestoneCriterionRepository.existsById(assignmentId));
    }

    @Test
    void testDeleteMilestoneCriteriaAssignmentNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneCriterionService.deleteById(999L);
        });
    }

    @Test
    void testMilestoneCriteriaAssignmentCompetitionRoleMapping() {
        // Given
        CreateMilestoneCriterionRequest request = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(testMilestoneRule.getId())
                .criterionId(testCriteria.getId())
                .partnerSide(PartnerSide.FOLLOWER)
                .scale(10)
                .build();

        // When
        MilestoneCriterionDto result = milestoneCriterionService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(PartnerSide.FOLLOWER, result.getPartnerSide());

        // Проверяем в БД
        Optional<MilestoneCriterionEntity> savedAssignment = milestoneCriterionRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertEquals(PartnerSide.FOLLOWER, savedAssignment.get().getPartnerSide());
    }

    @Test
    void testMilestoneCriteriaAssignmentWithNullCompetitionRole() {
        // Given
        CreateMilestoneCriterionRequest request = CreateMilestoneCriterionRequest.builder()
                .milestoneRuleId(testMilestoneRule.getId())
                .criterionId(testCriteria.getId())
                .partnerSide(null)
                .scale(10)
                .build();

        // When
        MilestoneCriterionDto result = milestoneCriterionService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getPartnerSide());

        // Проверяем в БД
        Optional<MilestoneCriterionEntity> savedAssignment = milestoneCriterionRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertNull(savedAssignment.get().getPartnerSide());
    }

    @Test
    void testMilestoneCriteriaAssignmentCascadeDelete() {
        // Given
        MilestoneCriterionEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);
        Long assignmentId = assignment.getId();

        // When
        milestoneCriterionService.deleteById(assignmentId);

        // Then
        assertFalse(milestoneCriterionRepository.existsById(assignmentId));
        // Проверяем, что связанные сущности остались
        assertTrue(milestoneRepository.existsById(testMilestone.getId()));
        assertTrue(criterionRepository.existsById(testCriteria.getId()));
    }

    // ========== Tests for findByMilestoneIdForCurrentUser method ==========

    @Test
    void testFindByMilestoneIdForCurrentUser_Success() {
        // Given
        // Создаем назначение пользователя на активность
        ActivityUserEntity userActivityAssignment = ActivityUserEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        activityUserRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        CriterionEntity criteria2 = CriterionEntity.builder()
                .name("Ведение")
                .build();
        criterionRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriterionEntity criteria3 = CriterionEntity.builder()
                .name("Музыкальность")
                .build();
        criterionRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null); // null означает для всех

        // When
        List<MilestoneCriterionDto> result = milestoneCriterionService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Только LEADER и null (для всех)

        // Проверяем, что возвращены только подходящие назначения
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.LEADER));
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == null));
        assertFalse(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.FOLLOWER));
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_WithFollowerPartnerSide() {
        // Given
        // Создаем назначение пользователя на активность как FOLLOWER
        ActivityUserEntity userActivityAssignment = ActivityUserEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.FOLLOWER)
                .build();
        activityUserRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        CriterionEntity criteria2 = CriterionEntity.builder()
                .name("Ведение")
                .build();
        criterionRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriterionEntity criteria3 = CriterionEntity.builder()
                .name("Музыкальность")
                .build();
        criterionRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        // When
        List<MilestoneCriterionDto> result = milestoneCriterionService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Только FOLLOWER и null (для всех)

        // Проверяем, что возвращены только подходящие назначения
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.FOLLOWER));
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == null));
        assertFalse(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.LEADER));
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_WithNullPartnerSide() {
        // Given
        // Создаем назначение пользователя на активность без указания стороны
        ActivityUserEntity userActivityAssignment = ActivityUserEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(null)
                .build();
        activityUserRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        CriterionEntity criteria2 = CriterionEntity.builder()
                .name("Ведение")
                .build();
        criterionRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriterionEntity criteria3 = CriterionEntity.builder()
                .name("Музыкальность")
                .build();
        criterionRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        // When
        List<MilestoneCriterionDto> result = milestoneCriterionService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Только null (для всех)

        // Проверяем, что возвращено только назначение для всех
        assertTrue(result.stream().allMatch(dto -> dto.getPartnerSide() == null));
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_EmptyAssignments() {
        // Given
        // Создаем назначение пользователя на активность
        ActivityUserEntity userActivityAssignment = ActivityUserEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        activityUserRepository.save(userActivityAssignment);

        // Не создаем никаких назначений критериев на этап

        // When
        List<MilestoneCriterionDto> result = milestoneCriterionService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_NonExistentMilestone() {
        // Given
        Long nonExistentMilestoneId = 999L;

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            milestoneCriterionService.findByMilestoneIdForCurrentUser(nonExistentMilestoneId);
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_NullMilestoneId() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneCriterionService.findByMilestoneIdForCurrentUser(null);
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_UserNotAssignedToActivity() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // НЕ создаем назначение пользователя на активность

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            milestoneCriterionService.findByMilestoneIdForCurrentUser(testMilestone.getId());
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_UserAssignedToDifferentActivity() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // Создаем другую активность
        ActivityEntity differentActivity = ActivityEntity.builder()
                .name("Different Activity")
                .description("Different Description")
                .startDateTime(java.time.LocalDateTime.now().plusDays(1))
                .endDateTime(java.time.LocalDateTime.now().plusDays(1).plusHours(2))
                .address(org.bn.sensation.core.common.entity.Address.builder()
                        .country("Russia")
                        .city("Moscow")
                        .streetName("Different Street")
                        .streetNumber("3")
                        .comment("Different Address")
                        .build())
                .state(ActivityState.PLANNED)
                .occasion(testMilestone.getActivity().getOccasion())
                .build();
        differentActivity = activityRepository.save(differentActivity);

        // Создаем назначение пользователя на ДРУГУЮ активность
        ActivityUserEntity userActivityAssignment = ActivityUserEntity.builder()
                .user(testUser)
                .activity(differentActivity)
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        activityUserRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            milestoneCriterionService.findByMilestoneIdForCurrentUser(testMilestone.getId());
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_MultipleAssignmentsWithDifferentPartnerSides() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // Создаем назначение пользователя на активность как LEADER
        ActivityUserEntity userActivityAssignment = ActivityUserEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        activityUserRepository.save(userActivityAssignment);

        // Создаем несколько критериев и назначений
        CriterionEntity criteria1 = CriterionEntity.builder().name("Техника").build();
        criterionRepository.save(criteria1);
        createTestAssignment(testMilestone, criteria1, PartnerSide.LEADER);

        CriterionEntity criteria2 = CriterionEntity.builder().name("Ведение").build();
        criterionRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriterionEntity criteria3 = CriterionEntity.builder().name("Музыкальность").build();
        criterionRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        CriterionEntity criteria4 = CriterionEntity.builder().name("Артистизм").build();
        criterionRepository.save(criteria4);
        createTestAssignment(testMilestone, criteria4, PartnerSide.LEADER);

        // When
        List<MilestoneCriterionDto> result = milestoneCriterionService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size()); // LEADER, LEADER, null

        // Проверяем, что возвращены только подходящие назначения
        long leaderCount = result.stream().filter(dto -> dto.getPartnerSide() == PartnerSide.LEADER).count();
        long nullCount = result.stream().filter(dto -> dto.getPartnerSide() == null).count();
        long followerCount = result.stream().filter(dto -> dto.getPartnerSide() == PartnerSide.FOLLOWER).count();

        assertEquals(2, leaderCount);
        assertEquals(1, nullCount);
        assertEquals(0, followerCount);
    }

    // Вспомогательный метод для создания тестового назначения
    private MilestoneCriterionEntity createTestAssignment(MilestoneEntity milestone, CriterionEntity criteria, PartnerSide partnerSide) {
        return transactionTemplate.execute(status -> {
            MilestoneCriterionEntity assignment = MilestoneCriterionEntity.builder()
                    .milestoneRule(milestone.getMilestoneRule())
                    .criterion(criteria)
                    .partnerSide(partnerSide)
                    .scale(10)
                    .build();
            return milestoneCriterionRepository.save(assignment);
        });
    }

    // Вспомогательный метод для настройки SecurityContext с тестовым пользователем
    private void setupSecurityContext(UserEntity user) {
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }
}
