package org.bn.sensation.core.judgeroundstatus.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.judgeroundstatus.service.dto.JudgeRoundStatusDto;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.repository.CriterionRepository;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.repository.MilestoneCriterionRepository;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.security.CurrentUser;
import org.bn.sensation.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JudgeRoundStatusServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JudgeRoundStatusService judgeRoundStatusService;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private JudgeRoundStatusRepository judgeRoundStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityUserRepository activityUserRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private CriterionRepository criterionRepository;

    @Autowired
    private MilestoneCriterionRepository milestoneCriterionRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @MockitoBean
    private CurrentUser currentUser;

    // Test entities
    private UserEntity testJudge;
    private UserEntity testAdmin;
    private UserEntity testRegularUser;
    private ActivityEntity testActivity;
    private MilestoneEntity testMilestone;
    private MilestoneRuleEntity testMilestoneRule;
    private CriterionEntity testCriterion;
    private MilestoneCriterionEntity testMilestoneCriterion;
    private RoundEntity testRound;
    private ActivityUserEntity judgeAssignment;
    private OrganizationEntity testOrganization;
    private OccasionEntity testOccasion;

    @BeforeEach
    void setUp() {
        // Clean database
        cleanDatabase();
        judgeRoundStatusRepository.deleteAll();
        activityUserRepository.deleteAll();
        roundRepository.deleteAll();
        milestoneRepository.deleteAll();
        activityRepository.deleteAll();
        occasionRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        // Create test organization
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

        // Create test occasion
        testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .state(OccasionState.DRAFT)
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Create test activity
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Description")
                .state(ActivityState.DRAFT)
                .occasion(testOccasion)
                .build();
        testActivity = activityRepository.save(testActivity);

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .description("Test Description")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneOrder(1)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test milestone rule
        testMilestoneRule = MilestoneRuleEntity.builder()
                .milestone(testMilestone)
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(5)
                .strictPassMode(false)
                .build();
        testMilestoneRule = milestoneRuleRepository.save(testMilestoneRule);

        // Create test criterion
        testCriterion = CriterionEntity.builder()
                .name("Test Criterion")
                .build();
        testCriterion = criterionRepository.save(testCriterion);

        // Create milestone criterion
        testMilestoneCriterion = MilestoneCriterionEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criterion(testCriterion)
                .partnerSide(PartnerSide.LEADER)
                .build();
        testMilestoneCriterion = milestoneCriterionRepository.save(testMilestoneCriterion);

        // Link milestone with rule
        testMilestone.setMilestoneRule(testMilestoneRule);
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test users
        testJudge = createUser("judge", Role.USER);
        testAdmin = createUser("admin", Role.ADMIN);
        testRegularUser = createUser("user", Role.USER);

        // Create judge assignment
        judgeAssignment = ActivityUserEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .build();
        judgeAssignment = activityUserRepository.save(judgeAssignment);

        // Add assignment to activity's userAssignments collection
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(RoundState.IN_PROGRESS)
                .milestone(testMilestone)
                .roundOrder(0)
                .build();
        testRound = roundRepository.save(testRound);
    }

    private UserEntity createUser(String username, Role role) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .password("password")
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Test")
                        .surname("User")
                        .email(username + "@example.com")
                        .phoneNumber("+1234567890")
                        .build())
                .roles(Set.of(role))
                .organizations(Set.of(testOrganization))
                .build();
        return userRepository.save(user);
    }

    private void mockCurrentUser(UserEntity user) {
        SecurityUser mockSecurityUser = Mockito.mock(SecurityUser.class);
        Mockito.when(mockSecurityUser.getId()).thenReturn(user.getId());
        Mockito.when(currentUser.getSecurityUser()).thenReturn(mockSecurityUser);
    }

    @Test
    void testAcceptRound_AsJudge_Success() {
        // Given
        mockCurrentUser(testJudge);

        // When
        JudgeRoundStatusDto result = judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.READY);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(JudgeRoundStatus.READY, result.getStatus());
        assertNotNull(result.getJudge());
        assertNotNull(result.getRound());

        // Verify in database
        JudgeRoundStatusEntity savedEntity = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound.getId(), judgeAssignment.getId()).orElseThrow();
        assertEquals(JudgeRoundStatus.READY, savedEntity.getStatus());
        assertEquals(testRound.getId(), savedEntity.getRound().getId());
        assertEquals(judgeAssignment.getId(), savedEntity.getJudge().getId());
    }

    @Test
    void testRejectRound_AsJudge_Success() {
        // Given
        mockCurrentUser(testJudge);

        // When
        JudgeRoundStatusDto result = judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.NOT_READY);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(JudgeRoundStatus.NOT_READY, result.getStatus());
        assertNotNull(result.getJudge());
        assertNotNull(result.getRound());

        // Verify in database
        JudgeRoundStatusEntity savedEntity = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound.getId(), judgeAssignment.getId()).orElseThrow();
        assertEquals(JudgeRoundStatus.NOT_READY, savedEntity.getStatus());
    }

    @Test
    void testChangeJudgeRoundStatus_UpdateExistingStatus_Success() {
        // Given
        mockCurrentUser(testJudge);

        // First accept the round
        judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.READY);

        // Verify initial status
        JudgeRoundStatusEntity initialEntity = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound.getId(), judgeAssignment.getId()).orElseThrow();
        assertEquals(JudgeRoundStatus.READY, initialEntity.getStatus());

        // When - change to rejected
        JudgeRoundStatusDto result = judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.NOT_READY);

        // Then
        assertNotNull(result);
        assertEquals(JudgeRoundStatus.NOT_READY, result.getStatus());
        assertEquals(initialEntity.getId(), result.getId()); // Same entity, updated

        // Verify in database
        JudgeRoundStatusEntity updatedEntity = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound.getId(), judgeAssignment.getId()).orElseThrow();
        assertEquals(JudgeRoundStatus.NOT_READY, updatedEntity.getStatus());
        assertEquals(initialEntity.getId(), updatedEntity.getId()); // Same ID
    }

    @Test
    void testChangeJudgeRoundStatus_AsNonJudge_ThrowsException() {
        // Given
        mockCurrentUser(testRegularUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.READY);
        });
    }

    @Test
    void testChangeJudgeRoundStatus_AsAdmin_ThrowsException() {
        // Given
        mockCurrentUser(testAdmin);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.READY);
        });
    }

    @Test
    void testChangeRoundStatus_NonExistentJudgeRound_ThrowsException() {
        // Given
        mockCurrentUser(testJudge);
        Long nonExistentRoundId = 999L;

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            judgeRoundStatusService.changeJudgeRoundStatus(nonExistentRoundId, JudgeRoundStatus.READY);
        });
    }

    @Test
    void testChangeRoundStatus_NullJudgeRoundId_ThrowsException() {
        // Given
        mockCurrentUser(testJudge);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            judgeRoundStatusService.changeJudgeRoundStatus(null, JudgeRoundStatus.READY);
        });
    }

    @Test
    void testChangeJudgeRoundStatus_NullStatus_ThrowsException() {
        // Given
        mockCurrentUser(testJudge);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), null);
        });
    }

    @Test
    void testMultipleJudges_DifferentStatuses_Success() {
        // Given
        // Create second judge
        UserEntity secondJudge = createUser("judge2", Role.USER);
        ActivityUserEntity secondJudgeAssignment = ActivityUserEntity.builder()
                .user(secondJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .build();
        secondJudgeAssignment = activityUserRepository.save(secondJudgeAssignment);

        // Add second assignment to activity's userAssignments collection
        testActivity.getUserAssignments().add(secondJudgeAssignment);
        activityRepository.save(testActivity);

        // First judge accepts
        mockCurrentUser(testJudge);
        JudgeRoundStatusDto firstResult = judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.READY);

        // Second judge rejects
        mockCurrentUser(secondJudge);
        JudgeRoundStatusDto secondResult = judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.NOT_READY);

        // Then
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        assertNotEquals(firstResult.getId(), secondResult.getId()); // Different entities

        assertEquals(JudgeRoundStatus.READY, firstResult.getStatus());
        assertEquals(JudgeRoundStatus.NOT_READY, secondResult.getStatus());

        // Verify both in database
        JudgeRoundStatusEntity firstEntity = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound.getId(), judgeAssignment.getId()).orElseThrow();
        JudgeRoundStatusEntity secondEntity = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound.getId(), secondJudgeAssignment.getId()).orElseThrow();

        assertEquals(JudgeRoundStatus.READY, firstEntity.getStatus());
        assertEquals(JudgeRoundStatus.NOT_READY, secondEntity.getStatus());
    }

    @Test
    void testJudgeRoundStatus_UniqueConstraint_Success() {
        // Given
        mockCurrentUser(testJudge);

        // When - create first status
        JudgeRoundStatusDto firstResult = judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.READY);

        // When - update same judge's status (should update, not create new)
        JudgeRoundStatusDto secondResult = judgeRoundStatusService.changeJudgeRoundStatus(testRound.getId(), JudgeRoundStatus.NOT_READY);

        // Then
        assertEquals(firstResult.getId(), secondResult.getId()); // Same entity updated
        assertEquals(JudgeRoundStatus.NOT_READY, secondResult.getStatus());

        // Verify only one record exists for this judge-round combination
        long count = judgeRoundStatusRepository.findAll().stream()
                .filter(jr -> jr.getRound().getId().equals(testRound.getId())
                           && jr.getJudge().getId().equals(judgeAssignment.getId()))
                .count();
        assertEquals(1, count);
    }

    @Test
    void testChangeRoundStatus_Judge_RoundInDraftState_ThrowsException() {
        // Given
        mockCurrentUser(testJudge);

        // Create round in DRAFT state
        final RoundEntity draftRound = RoundEntity.builder()
                .name("Draft Round")
                .state(RoundState.DRAFT)
                .milestone(testMilestone)
                .roundOrder(0)
                .build();
        roundRepository.save(draftRound);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            judgeRoundStatusService.changeJudgeRoundStatus(draftRound.getId(), JudgeRoundStatus.READY);
        });

        assertTrue(exception.getMessage().contains("Статус раунда DRAFT. Не может быть принят или отменен судьей"));
    }

}
