package org.bn.sensation.core.round.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.activity.statemachine.ActivityState;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.occasion.statemachine.OccasionState;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.bn.sensation.core.user.entity.*;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class RoundServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoundService roundService;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private ParticipantRepository participantRepository;

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
    private JudgeRoundStatusRepository judgeRoundStatusRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Mock
    private CurrentUser mockCurrentUser;

    private MilestoneEntity testMilestone;
    private MilestoneEntity testMilestone1;
    private ParticipantEntity testParticipant;
    private ParticipantEntity testParticipant1;
    private ActivityEntity testActivity;
    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private RoundEntity testRound;
    private UserEntity testJudge;
    private UserEntity testJudgeChief;
    private ActivityUserEntity testJudgeAssignment;
    private ActivityUserEntity testJudgeChiefAssignment;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Clean up existing data
        judgeRoundStatusRepository.deleteAll();
        roundRepository.deleteAll();
        activityUserRepository.deleteAll();
        participantRepository.deleteAll();
        milestoneRepository.deleteAll();
        activityRepository.deleteAll();
        occasionRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

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
                .state(OccasionState.PLANNED)
                .endDate(LocalDate.now().plusDays(3))
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Create test activity
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Activity Description")
                .state(ActivityState.PLANNED)
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

        // Create test users (judges)
        testJudge = UserEntity.builder()
                .username("testjudge")
                .password("password")
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Test")
                        .surname("Judge")
                        .email("judge@example.com")
                        .phoneNumber("+1234567891")
                        .build())
                .roles(Set.of(Role.USER, Role.SUPERADMIN))
                .organizations(Set.of(testOrganization))
                .build();
        testJudge = userRepository.save(testJudge);

        testJudgeChief = UserEntity.builder()
                .username("testjudgechief")
                .password("password")
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Test")
                        .surname("JudgeChief")
                        .email("judgechief@example.com")
                        .phoneNumber("+1234567892")
                        .build())
                .roles(Set.of(Role.USER))
                .organizations(Set.of(testOrganization))
                .build();
        testJudgeChief = userRepository.save(testJudgeChief);

        // Create user activity assignments (judges)
        testJudgeAssignment = ActivityUserEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        testJudgeAssignment = activityUserRepository.save(testJudgeAssignment);

        testJudgeChiefAssignment = ActivityUserEntity.builder()
                .user(testJudgeChief)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE_CHIEF)
                .partnerSide(PartnerSide.LEADER)
                .build();
        testJudgeChiefAssignment = activityUserRepository.save(testJudgeChiefAssignment);

        // Add assignments to activity
        testActivity.getActivityUsers().add(testJudgeAssignment);
        testActivity.getActivityUsers().add(testJudgeChiefAssignment);
        testActivity = activityRepository.save(testActivity);

        // Create test milestones
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneOrder(1)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        testMilestone1 = MilestoneEntity.builder()
                .name("Test Milestone 1")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneOrder(2)
                .build();
        testMilestone1 = milestoneRepository.save(testMilestone1);

        // Create test participants
        testParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("John")
                        .surname("Doe")
                        .email("john.doe@example.com")
                        .phoneNumber("+1234567890")
                        .build())
                .number("001")
                .isRegistered(true)
                .build();
        testParticipant = participantRepository.save(testParticipant);

        testParticipant1 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Jane")
                        .surname("Smith")
                        .email("jane.smith@example.com")
                        .phoneNumber("+0987654321")
                        .build())
                .number("002")
                .isRegistered(true)
                .build();
        testParticipant1 = participantRepository.save(testParticipant1);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .roundOrder(0)
                .build();
        testRound = roundRepository.save(testRound);
    }

    @Test
    void testCreateRound() {
        // Given
        CreateRoundRequest request = CreateRoundRequest.builder()
                .name("New Round")
                .milestoneId(testMilestone.getId())
                .build();

        // Set up security context with judge user
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock CurrentUser to return the test judge
        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);

        // When
        RoundDto result = roundService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertNotNull(result.getMilestone());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());

        // Verify round was saved to database
        Optional<RoundEntity> savedRound = roundRepository.findById(result.getId());
        assertTrue(savedRound.isPresent());
        assertEquals(request.getName(), savedRound.get().getName());
        assertEquals(testMilestone.getId(), savedRound.get().getMilestone().getId());

        // Verify that judge statuses were created for all judges
        var judgeStatuses = judgeRoundStatusRepository.findByRoundId(result.getId());
        assertEquals(2, judgeStatuses.size()); // Should have statuses for both judges

        // Verify each judge has a status with NOT_READY
        var judgeIds = judgeStatuses.stream()
                .map(status -> status.getJudge().getId())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(judgeIds.contains(testJudgeAssignment.getId()));
        assertTrue(judgeIds.contains(testJudgeChiefAssignment.getId()));

        // Verify all statuses are NOT_READY
        judgeStatuses.forEach(status -> {
            assertEquals(JudgeRoundStatus.NOT_READY, status.getStatus());
            assertEquals(result.getId(), status.getRound().getId());
        });
    }

    @Test
    void testCreateRoundWithNonExistentMilestone() {
        // Given
        CreateRoundRequest request = CreateRoundRequest.builder()
                .name("New Round")
                .milestoneId(999L) // Non-existent milestone
                .build();

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            roundService.create(request);
        });
    }

    @Test
    void testUpdateRound() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("Updated Round")
                .build();

        // Set up security context with judge user
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock CurrentUser to return the test judge
        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);

        // When
        RoundDto result = roundService.update(testRound.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testRound.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertNotNull(result.getMilestone());
        assertEquals(1, result.getParticipants().size());

        // Verify round was updated in database
        Optional<RoundEntity> updatedRound = roundRepository.findById(testRound.getId());
        assertTrue(updatedRound.isPresent());
        assertEquals(request.getName(), updatedRound.get().getName());
        assertEquals(1, updatedRound.get().getParticipants().size());
        assertTrue(updatedRound.get().getParticipants().contains(testParticipant));
    }

    @Test
    void testUpdateRoundWithNonExistentRound() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("Updated Round")
                .build();

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            roundService.update(999L, request);
        });
    }

    @Test
    void testUpdateRoundWithEmptyName() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roundService.update(testRound.getId(), request);
        });
    }

    @Test
    void testFindAllRounds() {
        // Create additional rounds
        RoundEntity round2 = RoundEntity.builder()
                .name("Round 2")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(1)
                .build();
        roundRepository.save(round2);

        RoundEntity round3 = RoundEntity.builder()
                .name("Round 3")
                .state(RoundState.OPENED)
                .milestone(testMilestone1)
                .roundOrder(0)
                .build();
        roundRepository.save(round3);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RoundDto> result = roundService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindRoundById() {
        // When
        Optional<RoundDto> result = roundService.findById(testRound.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRound.getId(), result.get().getId());
        assertEquals(testRound.getName(), result.get().getName());
        assertNotNull(result.get().getMilestone());
        assertEquals(testMilestone.getId(), result.get().getMilestone().getId());
        assertEquals(1, result.get().getParticipants().size());
    }

    @Test
    void testFindRoundByIdNotFound() {
        // When
        Optional<RoundDto> result = roundService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteRound() {
        // Given
        Long roundId = testRound.getId();

        // When
        roundService.deleteById(roundId);

        // Then
        assertFalse(roundRepository.existsById(roundId));
    }

    @Test
    void testDeleteRoundNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roundService.deleteById(999L);
        });
    }

    @Test
    void testRoundCascadeDelete() {
        // Given
        Long roundId = testRound.getId();

        // When
        roundService.deleteById(roundId);

        // Then
        assertFalse(roundRepository.existsById(roundId));

        // Verify participants still exist (no cascade delete)
        assertTrue(participantRepository.existsById(testParticipant.getId()));
        assertTrue(milestoneRepository.existsById(testMilestone.getId()));
    }

    @Test
    void testFindByMilestoneIdInLifeStates() {
        // Given - Create additional rounds with different states
        RoundEntity plannedRound = RoundEntity.builder()
                .name("Planned Round")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .roundOrder(0)
                .build();
        plannedRound = roundRepository.save(plannedRound);

        RoundEntity inProgressRound = RoundEntity.builder()
                .name("In Progress Round")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .roundOrder(1)
                .build();
        inProgressRound = roundRepository.save(inProgressRound);

        RoundEntity readyRound = RoundEntity.builder()
                .name("Ready Round")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .roundOrder(2)
                .build();
        readyRound = roundRepository.save(readyRound);

        RoundEntity completedRound = RoundEntity.builder()
                .name("Completed Round")
                .state(RoundState.CLOSED)
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .roundOrder(3)
                .build();
        completedRound = roundRepository.save(completedRound);

        // Add rounds to milestone
        testMilestone.getRounds().add(plannedRound);
        testMilestone.getRounds().add(inProgressRound);
        testMilestone.getRounds().add(readyRound);
        testMilestone.getRounds().add(completedRound);
        milestoneRepository.save(testMilestone);

        // Create judge round statuses for the rounds
        JudgeRoundStatusEntity plannedStatus = JudgeRoundStatusEntity.builder()
                .round(plannedRound)
                .judge(testJudgeAssignment)
                .status(JudgeRoundStatus.NOT_READY)
                .build();
        judgeRoundStatusRepository.save(plannedStatus);

        JudgeRoundStatusEntity inProgressStatus = JudgeRoundStatusEntity.builder()
                .round(inProgressRound)
                .judge(testJudgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(inProgressStatus);

        JudgeRoundStatusEntity readyStatus = JudgeRoundStatusEntity.builder()
                .round(readyRound)
                .judge(testJudgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(readyStatus);

        JudgeRoundStatusEntity completedStatus = JudgeRoundStatusEntity.builder()
                .round(completedRound)
                .judge(testJudgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(completedStatus);

        // Set up security context with judge user
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock CurrentUser to return the test judge
        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);

        // Replace the CurrentUser in the service with our mock
        ReflectionTestUtils.setField(roundService, "currentUser", mockCurrentUser);

        // When
        List<RoundWithJRStatusDto> result = roundService.findByMilestoneIdInLifeStates(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(4, result.size()); // Should return all life state rounds (PLANNED, IN_PROGRESS, READY, COMPLETED)

        // Verify rounds are sorted by ID
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getId() <= result.get(i).getId());
        }

        // Verify each round has the correct judge status
        Map<Long, RoundWithJRStatusDto> resultMap = result.stream()
                .collect(Collectors.toMap(RoundWithJRStatusDto::getId, dto -> dto));

        assertEquals(JudgeRoundStatus.NOT_READY, resultMap.get(plannedRound.getId()).getJudgeRoundStatus());
        assertEquals(JudgeRoundStatus.READY, resultMap.get(inProgressRound.getId()).getJudgeRoundStatus());
        assertEquals(JudgeRoundStatus.READY, resultMap.get(readyRound.getId()).getJudgeRoundStatus());
        assertEquals(JudgeRoundStatus.READY, resultMap.get(completedRound.getId()).getJudgeRoundStatus());

        // Verify round details
        result.forEach(round -> {
            assertNotNull(round.getName());
            assertNotNull(round.getState());
            assertTrue(RoundState.LIFE_ROUND_STATES.contains(round.getState()));
            assertEquals(testMilestone.getId(), round.getMilestone().getId());
        });
    }

    @Test
    void testFindByMilestoneIdInLifeStates_WithDraftRound_ExcludesDraft() {
        // Given - Create a draft round (should be excluded)
        RoundEntity draftRound = RoundEntity.builder()
                .name("Draft Round")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .roundOrder(0)
                .build();
        draftRound = roundRepository.save(draftRound);

        RoundEntity plannedRound = RoundEntity.builder()
                .name("Planned Round")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .roundOrder(1)
                .build();
        plannedRound = roundRepository.save(plannedRound);

        // Add rounds to milestone
        testMilestone.getRounds().add(plannedRound);
        milestoneRepository.save(testMilestone);

        // Create judge round status for planned round only
        JudgeRoundStatusEntity plannedStatus = JudgeRoundStatusEntity.builder()
                .round(plannedRound)
                .judge(testJudgeAssignment)
                .status(JudgeRoundStatus.NOT_READY)
                .build();
        judgeRoundStatusRepository.save(plannedStatus);

        // Set up security context with judge user
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock CurrentUser to return the test judge
        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);

        // Replace the CurrentUser in the service with our mock
        ReflectionTestUtils.setField(roundService, "currentUser", mockCurrentUser);

        // When
        var result = roundService.findByMilestoneIdInLifeStates(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Should only return the planned round, not the draft
        assertEquals(plannedRound.getId(), result.get(0).getId());
        assertEquals(RoundState.OPENED, result.get(0).getState());
    }

    @Test
    void testFindByMilestoneIdInLifeStates_NonExistentMilestone_ThrowsException() {
        // Given
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);
        ReflectionTestUtils.setField(roundService, "currentUser", mockCurrentUser);

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            roundService.findByMilestoneIdInLifeStates(999L);
        });
    }

    @Test
    void testFindByMilestoneIdInLifeStates_UserNotAssignedToActivity_ThrowsException() {
        // Given - Create a user not assigned to the activity
        UserEntity otherUser = UserEntity.builder()
                .username("otheruser")
                .password("password")
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Other")
                        .surname("User")
                        .email("other@example.com")
                        .phoneNumber("+1234567899")
                        .build())
                .roles(Set.of(Role.USER))
                .organizations(Set.of(testOrganization))
                .build();
        otherUser = userRepository.save(otherUser);

        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(otherUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);
        ReflectionTestUtils.setField(roundService, "currentUser", mockCurrentUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roundService.findByMilestoneIdInLifeStates(testMilestone.getId());
        });
    }

    @Test
    void testGenerateRounds_WithFinalMilestone_ShouldCreateOneFinalRound() {
        // Given - Create a final milestone (milestoneOrder == 0)
        MilestoneRuleEntity finalMilestoneRule = MilestoneRuleEntity.builder()
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(5)
                .strictPassMode(false)
                .build();
        finalMilestoneRule = milestoneRuleRepository.save(finalMilestoneRule);

        MilestoneEntity finalMilestone = MilestoneEntity.builder()
                .name("Final Milestone")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneRule(finalMilestoneRule)
                .milestoneOrder(0) // Final milestone
                .build();
        finalMilestone = milestoneRepository.save(finalMilestone);
        
        finalMilestoneRule.setMilestone(finalMilestone);
        milestoneRuleRepository.save(finalMilestoneRule);
        finalMilestone.setMilestoneRule(finalMilestoneRule);
        finalMilestone = milestoneRepository.save(finalMilestone);

        // Create participants for final milestone
        ParticipantEntity participant1 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Final")
                        .surname("Participant1")
                        .email("final1@test.com")
                        .phoneNumber("+1234567890")
                        .build())
                .number("F001")
                .isRegistered(true)
                .partnerSide(PartnerSide.LEADER)
                .activity(testActivity)
                .rounds(new HashSet<>())
                .milestones(new HashSet<>())
                .build();
        participant1 = participantRepository.save(participant1);

        ParticipantEntity participant2 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Final")
                        .surname("Participant2")
                        .email("final2@test.com")
                        .phoneNumber("+1234567891")
                        .build())
                .number("F002")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>())
                .milestones(new HashSet<>())
                .build();
        participant2 = participantRepository.save(participant2);

        List<Long> participantIds = List.of(participant1.getId(), participant2.getId());

        // When - Generate rounds for final milestone
        List<RoundDto> rounds = roundService.generateRounds(finalMilestone, participantIds, false, 5);

        // Then - Verify only one round created with name "Финал"
        assertNotNull(rounds);
        assertEquals(1, rounds.size());
        
        RoundDto finalRound = rounds.get(0);
        assertEquals("Финал", finalRound.getName());
        assertEquals(RoundState.OPENED, finalRound.getState());
        assertEquals(0, finalRound.getRoundOrder());
        assertNotNull(finalRound.getMilestone());
        assertEquals(finalMilestone.getId(), finalRound.getMilestone().getId());
        assertNotNull(finalRound.getParticipants());
        assertEquals(2, finalRound.getParticipants().size()); // Both participants should be in final round
    }

    @Test
    void testGenerateRounds_WithFinalMilestone_ExceedsParticipantLimit_ShouldStillCreateOneRound() {
        // Given - Create a final milestone with participant limit less than actual participants
        MilestoneRuleEntity finalMilestoneRule = MilestoneRuleEntity.builder()
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(5) // Limit is 5
                .roundParticipantLimit(5)
                .strictPassMode(false)
                .build();
        finalMilestoneRule = milestoneRuleRepository.save(finalMilestoneRule);

        MilestoneEntity finalMilestone = MilestoneEntity.builder()
                .name("Final Milestone")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneRule(finalMilestoneRule)
                .milestoneOrder(0) // Final milestone
                .build();
        finalMilestone = milestoneRepository.save(finalMilestone);
        
        finalMilestoneRule.setMilestone(finalMilestone);
        milestoneRuleRepository.save(finalMilestoneRule);
        finalMilestone.setMilestoneRule(finalMilestoneRule);
        finalMilestone = milestoneRepository.save(finalMilestone);

        // Create more participants than limit
        List<Long> participantIds = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ParticipantEntity participant = ParticipantEntity.builder()
                    .person(Person.builder()
                            .name("Final")
                            .surname("Participant" + i)
                            .email("final" + i + "@test.com")
                            .phoneNumber("+123456789" + i)
                            .build())
                    .number("F00" + i)
                    .isRegistered(true)
                    .partnerSide(i % 2 == 0 ? PartnerSide.FOLLOWER : PartnerSide.LEADER)
                    .activity(testActivity)
                    .rounds(new HashSet<>())
                    .milestones(new HashSet<>())
                    .build();
            participant = participantRepository.save(participant);
            participantIds.add(participant.getId());
        }

        // When - Generate rounds for final milestone
        List<RoundDto> rounds = roundService.generateRounds(finalMilestone, participantIds, false, 5);

        // Then - Verify only one round created (final round ignores limit)
        assertNotNull(rounds);
        assertEquals(1, rounds.size());
        
        RoundDto finalRound = rounds.get(0);
        assertEquals("Финал", finalRound.getName());
        assertNotNull(finalRound.getParticipants());
        assertEquals(8, finalRound.getParticipants().size()); // All 8 participants should be in final round
    }
}
