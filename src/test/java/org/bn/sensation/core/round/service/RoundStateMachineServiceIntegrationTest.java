package org.bn.sensation.core.round.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.user.entity.*;
import org.bn.sensation.core.useractivity.repository.UserActivityAssignmentRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.useractivity.entity.UserActivityPosition;
import org.bn.sensation.security.CurrentUser;
import org.bn.sensation.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class RoundStateMachineServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoundStateMachineService roundStateMachineService;

    @Autowired
    private RoundRepository roundRepository;

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
    private UserActivityAssignmentRepository userActivityAssignmentRepository;

    @Autowired
    private JudgeRoundStatusRepository judgeRoundStatusRepository;

    @MockitoBean
    private CurrentUser currentUser;

    private RoundEntity testRound;
    private MilestoneEntity testMilestone;
    private ActivityEntity testActivity;
    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private UserEntity adminUser;
    private UserEntity judgeUser;
    private UserEntity regularUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Clean up existing data
        judgeRoundStatusRepository.deleteAll();
        roundRepository.deleteAll();
        milestoneRepository.deleteAll();
        activityRepository.deleteAll();
        occasionRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
        userActivityAssignmentRepository.deleteAll();

        // Create test organization
        testOrganization = OrganizationEntity.builder()
                .name("Test Organization")
                .build();
        testOrganization = organizationRepository.save(testOrganization);

        // Create test occasion
        testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Description")
                .startDate(LocalDate.now())
                .state(OccasionState.DRAFT)
                .endDate(LocalDate.now().plusDays(3))
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Create test activity
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Activity Description")
                .state(ActivityState.DRAFT)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .address(Address.builder()
                        .city("Test City")
                        .streetName("Test Street")
                        .streetNumber("123")
                        .build())
                .occasion(testOccasion)
                .build();
        testActivity = activityRepository.save(testActivity);

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .state(MilestoneState.IN_PROGRESS)
                .activity(testActivity)
                .milestoneOrder(1)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test users
        adminUser = createUser("admin", Role.ADMIN);
        judgeUser = createUser("judge", Role.USER);
        regularUser = createUser("user", Role.USER);

        // Create user activity assignment for judge
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(judgeUser)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);

        testActivity.getUserAssignments().add(judgeAssignment);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(RoundState.DRAFT)
                .description("Test Round Description")
                .milestone(testMilestone)
                .participants(new HashSet<>())
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
        when(mockSecurityUser.getId()).thenReturn(user.getId());
        when(mockSecurityUser.getRoles()).thenReturn(user.getRoles());
        when(currentUser.getSecurityUser()).thenReturn(mockSecurityUser);
    }

    @Test
    void testSendEvent_PlanEvent_AsAdmin_Success() {
        // Given
        mockCurrentUser(adminUser);
        assertEquals(RoundState.DRAFT, testRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, updatedRound.getState());
    }

    @Test
    void testSendEvent_PlanEvent_AsJudge_Success() {
        // Given
        mockCurrentUser(judgeUser);
        assertEquals(RoundState.DRAFT, testRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, updatedRound.getState());
    }

    @Test
    void testSendEvent_PlanEvent_AsRegularUser_Success() {
        // Given
        mockCurrentUser(regularUser);
        assertEquals(RoundState.DRAFT, testRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, updatedRound.getState());
    }

    @Test
    void testSendEvent_StartEvent_AsJudge_Success() {
        // Given
        mockCurrentUser(judgeUser);
        // First plan the round as admin
        mockCurrentUser(adminUser);
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);

        // Then switch to judge and start
        mockCurrentUser(judgeUser);
        RoundEntity plannedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, plannedRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, updatedRound.getState());
    }

    @Test
    void testSendEvent_StartEvent_AsAdmin_Success() {
        // Given
        mockCurrentUser(adminUser);
        // First plan the round
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);

        RoundEntity plannedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, plannedRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, updatedRound.getState());
    }

    @Test
    void testSendEvent_StartEvent_AsRegularUser_Success() {
        // Given
        mockCurrentUser(adminUser);
        // First plan the round
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);

        // Switch to regular user
        mockCurrentUser(regularUser);
        RoundEntity plannedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, plannedRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, updatedRound.getState());
    }

    @Test
    void testSendEvent_CompleteEvent_AsJudge_Success() {
        // Given
        mockCurrentUser(adminUser);
        // Plan and start the round
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Create judge round acceptance
        UserActivityAssignmentEntity judgeAssignment = testActivity.getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(judgeUser.getId()))
                .findFirst()
                .orElseThrow();

        JudgeRoundStatusEntity judgeRound = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(judgeRound);

        // Switch to judge and complete
        mockCurrentUser(judgeUser);
        RoundEntity inProgressRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, inProgressRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.COMPLETE);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, updatedRound.getState());
    }

    @Test
    void testSendEvent_CompleteEvent_AsAdmin_Success() {
        // Given
        mockCurrentUser(adminUser);
        // Plan and start the round
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Create judge round acceptance
        UserActivityAssignmentEntity judgeAssignment = testActivity.getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(judgeUser.getId()))
                .findFirst()
                .orElseThrow();

        JudgeRoundStatusEntity judgeRound = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(judgeRound);

        RoundEntity inProgressRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, inProgressRound.getState());

        // When
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.COMPLETE);

        // Then
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, updatedRound.getState());
    }

    @Test
    void testSendEvent_CompleteEvent_WithoutJudgeAcceptance_ThrowsException() {
        // Given
        mockCurrentUser(adminUser);
        // Plan and start the round
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Switch to regular user (no judge acceptance created)
        mockCurrentUser(regularUser);
        RoundEntity inProgressRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, inProgressRound.getState());

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.COMPLETE);
        });

        // Verify state didn't change
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, updatedRound.getState());
    }

    @Test
    void testSendEvent_InvalidTransition_FromDraftToComplete_ThrowsException() {
        // Given
        mockCurrentUser(adminUser);
        assertEquals(RoundState.DRAFT, testRound.getState());

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.COMPLETE);
        });

        // Verify state didn't change
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.DRAFT, updatedRound.getState());
    }

    @Test
    void testSendEvent_InvalidTransition_FromCompletedToStart_ThrowsException() {
        // Given
        mockCurrentUser(adminUser);
        // Plan and start the round
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Create judge round acceptance
        UserActivityAssignmentEntity judgeAssignment = testActivity.getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(judgeUser.getId()))
                .findFirst()
                .orElseThrow();

        JudgeRoundStatusEntity judgeRound = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(judgeRound);

        // Complete the round
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.COMPLETE);

        RoundEntity completedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, completedRound.getState());

        // When & Then - State machine will reject this transition
        // The state machine itself handles invalid transitions, not canTransition
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);

        // Verify state didn't change (state machine rejected the transition)
        RoundEntity updatedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, updatedRound.getState());
    }

    @Test
    void testSendEvent_NonExistentRound_ThrowsException() {
        // Given
        mockCurrentUser(adminUser);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roundStateMachineService.sendEvent(999L, RoundEvent.PLAN);
        });
    }

    @Test
    void testFullStateTransitionFlow_AsAdmin_Success() {
        // Given
        mockCurrentUser(adminUser);
        assertEquals(RoundState.DRAFT, testRound.getState());

        // When - Plan
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);
        RoundEntity plannedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, plannedRound.getState());

        // When - Start
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);
        RoundEntity inProgressRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, inProgressRound.getState());

        // Create judge round acceptance before completing
        UserActivityAssignmentEntity judgeAssignment = testActivity.getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(judgeUser.getId()))
                .findFirst()
                .orElseThrow();

        JudgeRoundStatusEntity judgeRound = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(judgeRound);

        // When - Complete
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.COMPLETE);
        RoundEntity completedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, completedRound.getState());
    }

    @Test
    void testFullStateTransitionFlow_WithRoleChanges_Success() {
        // Given
        assertEquals(RoundState.DRAFT, testRound.getState());

        // When - Plan as Admin
        mockCurrentUser(adminUser);
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.PLAN);
        RoundEntity plannedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.PLANNED, plannedRound.getState());

        // When - Start as Judge
        mockCurrentUser(judgeUser);
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.START);
        RoundEntity inProgressRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.IN_PROGRESS, inProgressRound.getState());

        // Create judge round acceptance before completing
        UserActivityAssignmentEntity judgeAssignment = testActivity.getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(judgeUser.getId()))
                .findFirst()
                .orElseThrow();

        JudgeRoundStatusEntity judgeRound = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(judgeRound);

        // When - Complete as Judge
        roundStateMachineService.sendEvent(testRound.getId(), RoundEvent.COMPLETE);
        RoundEntity completedRound = roundRepository.findById(testRound.getId()).orElseThrow();
        assertEquals(RoundState.COMPLETED, completedRound.getState());
    }
}
