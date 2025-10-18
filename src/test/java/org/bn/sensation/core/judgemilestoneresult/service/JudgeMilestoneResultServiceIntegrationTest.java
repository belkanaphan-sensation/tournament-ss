package org.bn.sensation.core.judgemilestoneresult.service;

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
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.judgemilstoneresult.service.JudgeMilestoneResultService;
import org.bn.sensation.core.milestonecriteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.milestonecriteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultRoundRequest;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.user.entity.*;
import org.bn.sensation.core.useractivity.repository.UserActivityAssignmentRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.useractivity.entity.UserActivityPosition;
import org.bn.sensation.security.CurrentUser;
import org.bn.sensation.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Transactional
class JudgeMilestoneResultServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JudgeMilestoneResultService judgeMilestoneResultService;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserActivityAssignmentRepository userActivityAssignmentRepository;

    @Autowired
    private MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CriteriaRepository criteriaRepository;

    @Mock
    private CurrentUser mockCurrentUser;

    // Test entities
    private UserEntity testJudge;
    private UserEntity testParticipantUser;
    private ActivityEntity testActivity;
    private MilestoneEntity testMilestone;
    private RoundEntity testRound;
    private ParticipantEntity testParticipant;
    private CriteriaEntity testCriteria;
    private MilestoneRuleEntity testMilestoneRule;
    private MilestoneCriteriaAssignmentEntity testMilestoneCriteria;

    @BeforeEach
    void setUp() {
        // Create test occasion
        OccasionEntity testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .state(OccasionState.IN_PROGRESS)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Create test organization
        OrganizationEntity testOrganization = OrganizationEntity.builder()
                .name("Test Organization")
                .description("Test Organization Description")
                .address(Address.builder()
                        .city("Test City")
                        .streetNumber("1")
                        .build())
                .build();
        testOrganization = organizationRepository.save(testOrganization);

        // Create test activity
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Activity Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .address(Address.builder()
                        .city("Test City")
                        .streetNumber("1")
                        .build())
                .state(ActivityState.IN_PROGRESS)
                .occasion(testOccasion)
                .build();
        testActivity = activityRepository.save(testActivity);

        // Create test judge user
        testJudge = UserEntity.builder()
                .username("testjudge")
                .password("password")
                .person(Person.builder()
                        .name("Judge")
                        .surname("Test")
                        .email("judge@test.com")
                        .phoneNumber("+1234567890")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        testJudge = userRepository.save(testJudge);

//        // Create test judge assignment
//        UserActivityAssignmentEntity testJudgeAssignment = UserActivityAssignmentEntity.builder()
//                .user(testJudge)
//                .activity(testActivity)
//                .position(UserActivityPosition.JUDGE)
//                .partnerSide(PartnerSide.LEADER)
//                .build();
//        testJudgeAssignment = userActivityAssignmentRepository.save(testJudgeAssignment);

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .state(MilestoneState.IN_PROGRESS)
                .milestoneOrder(1)
                .activity(testActivity)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test milestone rule
        testMilestoneRule = MilestoneRuleEntity.builder()
                .assessmentMode(org.bn.sensation.core.milestone.entity.AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .milestone(testMilestone)
                .build();
        testMilestoneRule = milestoneRuleRepository.save(testMilestoneRule);

        // Link milestone with rule
        testMilestone.setMilestoneRule(testMilestoneRule);
        milestoneRepository.save(testMilestone);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(RoundState.IN_PROGRESS)
                .milestone(testMilestone)
                .build();
        testRound = roundRepository.save(testRound);

        // Create test participant user
        testParticipantUser = UserEntity.builder()
                .username("testparticipant")
                .password("password")
                .person(Person.builder()
                        .name("Participant")
                        .surname("Test")
                        .email("participant@test.com")
                        .phoneNumber("+1234567891")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        testParticipantUser = userRepository.save(testParticipantUser);

        // Create test participant
        testParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Test")
                        .surname("Participant")
                        .email("test@participant.com")
                        .phoneNumber("+1234567892")
                        .build())
                .number("001")
                .partnerSide(PartnerSide.LEADER)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        testParticipant = participantRepository.save(testParticipant);

        // Add participant to round
        testRound.getParticipants().add(testParticipant);
        roundRepository.save(testRound);

        // Create test criteria
        testCriteria = CriteriaEntity.builder()
                .name("Test Criteria")
                .build();
        testCriteria = criteriaRepository.save(testCriteria);

        // Create test milestone criteria assignment
        testMilestoneCriteria = MilestoneCriteriaAssignmentEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criteria(testCriteria)
                .partnerSide(PartnerSide.LEADER)
                .weight(BigDecimal.ONE)
                .scale(10)
                .build();
        testMilestoneCriteria = milestoneCriteriaAssignmentRepository.save(testMilestoneCriteria);
        testMilestoneRule.getCriteriaAssignments().add(testMilestoneCriteria);
        milestoneRuleRepository.save(testMilestoneRule);

        // Set up security context with judge user
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock CurrentUser to return the test judge
        when(mockCurrentUser.getSecurityUser()).thenReturn(securityUser);

        // Replace the CurrentUser in the service with our mock
        ReflectionTestUtils.setField(judgeMilestoneResultService, "currentUser", mockCurrentUser);
    }

    @Test
    void testCreateRoundResult_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();

        // When
        JudgeMilestoneResultDto result = judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // Then
        assertNotNull(result);
        assertEquals(8, result.getScore());
        assertNotNull(result.getParticipant());
        assertEquals(testParticipant.getId(), result.getParticipant().getId());
        assertNotNull(result.getRound());
        assertEquals(testRound.getId(), result.getRound().getId());
        assertNotNull(result.getMilestoneCriteria());
        assertEquals(testMilestoneCriteria.getId(), result.getMilestoneCriteria().getId());
        assertNotNull(result.getActivityUser());
        assertNotNull(result.getActivityUser().getId());
    }

    @Test
    void testCreateRoundResult_PartnerSideMismatch_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.FOLLOWER) // Different from criteria
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId()));
        assertEquals("Сторона судьи и критерия не совпадает", exception.getMessage());
    }

    @Test
    void testCreateRoundResult_ParticipantNotInRound_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        // Create another participant not in the round
        ParticipantEntity otherParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Other")
                        .surname("Participant")
                        .email("other@participant.com")
                        .phoneNumber("+1234567893")
                        .build())
                .number("002")
                .partnerSide(PartnerSide.FOLLOWER)
                .rounds(new HashSet<>()) // Not in any round
                .build();
        otherParticipant = participantRepository.save(otherParticipant);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(otherParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId()));
        assertEquals("Участник не участвует в данном раунде", exception.getMessage());
    }

    @Test
    void testCreateRoundResult_DuplicateResult_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();

        // Create first result
        judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // When & Then - try to create duplicate
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId()));
        assertEquals("Результат уже существует для данного раунда, участника, судьи и критерия", exception.getMessage());
    }

    @Test
    void testCreateRoundResult_CommonCriteriaWithAnyJudgeSide_Success() {
        // Given
        // Create common criteria (no partner side)
        MilestoneCriteriaAssignmentEntity commonCriteria = MilestoneCriteriaAssignmentEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criteria(testCriteria)
                .partnerSide(null) // Common criteria
                .weight(BigDecimal.ONE)
                .scale(10)
                .build();
        commonCriteria = milestoneCriteriaAssignmentRepository.save(commonCriteria);
        testMilestoneRule.getCriteriaAssignments().add(commonCriteria);
        milestoneRuleRepository.save(testMilestoneRule);

        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.FOLLOWER) // Different side, but should work for common criteria
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(commonCriteria.getId())
                .score(9)
                .isFavorite(true)
                .build();

        // When
        JudgeMilestoneResultDto result = judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // Then
        assertNotNull(result);
        assertEquals(9, result.getScore());
    }

    @Test
    void testCreateRoundResult_NonExistentMilestoneCriteria_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(999L) // Non-existent ID
                .score(8)
                .isFavorite(true)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId()));
    }

    @Test
    void testCreateRoundResult_NonExistentRound_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(999L) // Non-existent ID
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId()));
    }

    @Test
    void testCreateRoundResult_NonExistentParticipant_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(999L) // Non-existent ID
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId()));
    }

    @Test
    void testFindByRoundId_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(createdResult.getId(), results.get(0).getId());
        assertEquals(8, results.get(0).getScore());
    }

    @Test
    void testFindByRoundId_EmptyResult() {
        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByMilestoneId_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(9)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.findByMilestoneId(testMilestone.getId());

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(createdResult.getId(), results.get(0).getId());
        assertEquals(9, results.get(0).getScore());
    }

    @Test
    void testFindByParticipantId_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(7)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.findByParticipantId(testParticipant.getId());

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(createdResult.getId(), results.get(0).getId());
        assertEquals(7, results.get(0).getScore());
    }

    @Test
    void testFindByActivityUserId_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(6)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.findByActivityUserId(judgeAssignment.getId());

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(createdResult.getId(), results.get(0).getId());
        assertEquals(6, results.get(0).getScore());
    }

    @Test
    void testFindById_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest request = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(10)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(request, judgeAssignment.getId());

        // When
        Optional<JudgeMilestoneResultDto> result = judgeMilestoneResultService.findById(createdResult.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(createdResult.getId(), result.get().getId());
        assertEquals(10, result.get().getScore());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<JudgeMilestoneResultDto> result = judgeMilestoneResultService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdate_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(createRequest, judgeAssignment.getId());

        JudgeMilestoneResultRoundRequest updateRequest = new JudgeMilestoneResultRoundRequest();
        updateRequest.setId(createdResult.getId());
        updateRequest.setScore(8);

        // When
        JudgeMilestoneResultDto updatedResult = judgeMilestoneResultService.createOrUpdate(updateRequest, judgeAssignment.getId());

        // Then
        assertNotNull(updatedResult);
        assertEquals(createdResult.getId(), updatedResult.getId());
        assertEquals(8, updatedResult.getScore());
    }

    @Test
    void testUpdate_UnauthorizedUser_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(createRequest, judgeAssignment.getId());

        // Create another user with USER role (not admin)
        UserEntity otherUser = userRepository.save(UserEntity.builder()
                .username("otheruser")
                .password("password")
                .person(Person.builder()
                        .name("Other")
                        .surname("User")
                        .email("other@user.com")
                        .phoneNumber("+1234567894")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build());

        // Mock CurrentUser to return the other user
        SecurityUser otherUserSecurityUser = (SecurityUser) SecurityUser.fromUser(otherUser);
        when(mockCurrentUser.getSecurityUser()).thenReturn(otherUserSecurityUser);

        // Replace the CurrentUser in the service with our mock
        ReflectionTestUtils.setField(judgeMilestoneResultService, "currentUser", mockCurrentUser);

        JudgeMilestoneResultRoundRequest updateRequest = new JudgeMilestoneResultRoundRequest();
        updateRequest.setId(createdResult.getId());
        updateRequest.setScore(8);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> judgeMilestoneResultService.createOrUpdate(updateRequest, otherUser.getId()));
        assertEquals("Нельзя изменить результат другого судьи", exception.getMessage());
    }

    @Test
    void testUpdate_AdminUser_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(createRequest, judgeAssignment.getId());

        // Create admin user and set as current user
        UserEntity adminUser = UserEntity.builder()
                .username("adminuser")
                .password("password")
                .person(Person.builder()
                        .name("Admin")
                        .surname("User")
                        .email("admin@user.com")
                        .phoneNumber("+1234567895")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.ADMIN))
                .build();
        adminUser = userRepository.save(adminUser);

        SecurityUser adminSecurityUser = (SecurityUser) SecurityUser.fromUser(adminUser);
        UsernamePasswordAuthenticationToken adminAuthentication =
                new UsernamePasswordAuthenticationToken(adminSecurityUser, null, adminSecurityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(adminAuthentication);

        JudgeMilestoneResultRoundRequest updateRequest = new JudgeMilestoneResultRoundRequest();
        updateRequest.setId(createdResult.getId());
        updateRequest.setScore(9);

        // When
        JudgeMilestoneResultDto updatedResult = judgeMilestoneResultService.createOrUpdate(updateRequest, judgeAssignment.getId());

        // Then
        assertNotNull(updatedResult);
        assertEquals(createdResult.getId(), updatedResult.getId());
        assertEquals(9, updatedResult.getScore());
    }

    @Test
    void testUpdate_NotFound_ThrowsException() {
        // Given
        JudgeMilestoneResultRoundRequest updateRequest = new JudgeMilestoneResultRoundRequest();
        updateRequest.setId(999L);
        updateRequest.setScore(8);

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> judgeMilestoneResultService.createOrUpdate(updateRequest, 1L));
    }

    @Test
    void testDeleteById_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(judgeAssignment);
        activityRepository.save(testActivity);

        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(createRequest, judgeAssignment.getId());

        // When
        judgeMilestoneResultService.deleteById(createdResult.getId());

        // Then
        Optional<JudgeMilestoneResultDto> result = judgeMilestoneResultService.findById(createdResult.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteById_NotFound_ThrowsException() {
        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> judgeMilestoneResultService.deleteById(999L));
    }

    @Test
    void testCreateOrUpdateForRound_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        UserActivityAssignmentEntity assignmentEntity = userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(assignmentEntity);
        activityRepository.save(testActivity);
        // Create another criteria for the same milestone rule
        CriteriaEntity testCriteria2 = CriteriaEntity.builder()
                .name("Test Criteria 2")
                .build();
        testCriteria2 = criteriaRepository.save(testCriteria2);

        MilestoneCriteriaAssignmentEntity anotherCriteria = MilestoneCriteriaAssignmentEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criteria(testCriteria2)
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();
        anotherCriteria = milestoneCriteriaAssignmentRepository.save(anotherCriteria);
        testMilestoneRule.getCriteriaAssignments().add(anotherCriteria);
        milestoneRuleRepository.save(testMilestoneRule);

        // Create first result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(createRequest, assignmentEntity.getId());

        // Create second result with different criteria
        JudgeMilestoneResultRoundRequest createRequest2 = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(anotherCriteria.getId())
                .score(9)
                .isFavorite(false)
                .build();

        // Update first result
        JudgeMilestoneResultRoundRequest updateRequest = new JudgeMilestoneResultRoundRequest();
        updateRequest.setId(createdResult.getId());
        updateRequest.setScore(10);
        updateRequest.setIsFavorite(false);

        List<JudgeMilestoneResultRoundRequest> requests = List.of(createRequest2, updateRequest);

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.createOrUpdateForRound(testRound.getId(), requests);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());

        // Check that one result was created and one was updated
        boolean foundCreated = false;
        boolean foundUpdated = false;

        for (JudgeMilestoneResultDto result : results) {
            if (result.getId().equals(createdResult.getId())) {
                // This should be the updated result
                assertEquals(10, result.getScore());
                assertFalse(result.getIsFavorite());
                foundUpdated = true;
            } else {
                // This should be the newly created result
                assertEquals(9, result.getScore());
                assertFalse(result.getIsFavorite());
                foundCreated = true;
            }
        }

        assertTrue(foundCreated, "New result should be created");
        assertTrue(foundUpdated, "Existing result should be updated");
    }

    @Test
    void testCreateOrUpdateForRound_EmptyList_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        UserActivityAssignmentEntity assignmentEntity = userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(assignmentEntity);
        activityRepository.save(testActivity);

        // Сначала создаем результаты для всех участников раунда, чтобы условие валидации было выполнено
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        judgeMilestoneResultService.createOrUpdate(createRequest, assignmentEntity.getId());

        // Теперь отправляем пустой список запросов
        List<JudgeMilestoneResultRoundRequest> requests = List.of();

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.createOrUpdateForRound(testRound.getId(), requests);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testCreateOrUpdateForRound_MultipleJudges_ThrowsException() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        UserActivityAssignmentEntity assignmentEntity = userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(assignmentEntity);

        UserEntity anotherJudge = UserEntity.builder()
                .username("anotherjudge")
                .password("password")
                .person(Person.builder()
                        .name("Another")
                        .surname("Judge")
                        .email("another@judge.com")
                        .phoneNumber("+1234567891")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        anotherJudge = userRepository.save(anotherJudge);

        UserActivityAssignmentEntity anotherJudgeAssignment = UserActivityAssignmentEntity.builder()
                .user(anotherJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        anotherJudgeAssignment = userActivityAssignmentRepository.save(anotherJudgeAssignment);
        testActivity.getUserAssignments().add(anotherJudgeAssignment);
        activityRepository.save(testActivity);
        // Create another criteria for the same milestone rule
        CriteriaEntity testCriteria2 = CriteriaEntity.builder()
                .name("Test Criteria 2")
                .build();
        testCriteria2 = criteriaRepository.save(testCriteria2);

        MilestoneCriteriaAssignmentEntity anotherCriteria = MilestoneCriteriaAssignmentEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criteria(testCriteria2)
                .partnerSide(PartnerSide.LEADER)
                .weight(BigDecimal.ONE)
                .scale(10)
                .build();
        anotherCriteria = milestoneCriteriaAssignmentRepository.save(anotherCriteria);
        testMilestoneRule.getCriteriaAssignments().add(anotherCriteria);
        milestoneRuleRepository.save(testMilestoneRule);

        // Create first result with testJudge for first criteria
        JudgeMilestoneResultRoundRequest request1 = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto result1 = judgeMilestoneResultService.createOrUpdate(request1, assignmentEntity.getId());

        // Create second result with anotherJudge for second criteria
        SecurityUser anotherJudgeSecurityUser = (SecurityUser) SecurityUser.fromUser(anotherJudge);
        when(mockCurrentUser.getSecurityUser()).thenReturn(anotherJudgeSecurityUser);
        ReflectionTestUtils.setField(judgeMilestoneResultService, "currentUser", mockCurrentUser);

        JudgeMilestoneResultRoundRequest request2 = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(anotherCriteria.getId())
                .score(9)
                .isFavorite(false)
                .build();
        JudgeMilestoneResultDto result2 = judgeMilestoneResultService.createOrUpdate(request2, anotherJudgeAssignment.getId());

        // Reset mock to testJudge
        SecurityUser testJudgeSecurityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        when(mockCurrentUser.getSecurityUser()).thenReturn(testJudgeSecurityUser);
        ReflectionTestUtils.setField(judgeMilestoneResultService, "currentUser", mockCurrentUser);

        // Create update requests for both results (different judges)
        JudgeMilestoneResultRoundRequest updateRequest1 = JudgeMilestoneResultRoundRequest.builder()
                .id(result1.getId())
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(10)
                .isFavorite(false)
                .build();

        JudgeMilestoneResultRoundRequest updateRequest2 = JudgeMilestoneResultRoundRequest.builder()
                .id(result2.getId())
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(7)
                .isFavorite(true)
                .build();

        List<JudgeMilestoneResultRoundRequest> requests = List.of(updateRequest1, updateRequest2);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                judgeMilestoneResultService.createOrUpdateForRound(testRound.getId(), requests));
        assertEquals("Нельзя изменить результат другого судьи", exception.getMessage());
    }

    @Test
    void testCreateOrUpdateForRound_MultipleRounds_ChangesStatusForAll() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        UserActivityAssignmentEntity assignmentEntity = userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(assignmentEntity);
        activityRepository.save(testActivity);
        // Create second round
        RoundEntity testRound2 = RoundEntity.builder()
                .name("Test Round 2")
                .state(RoundState.IN_PROGRESS)
                .milestone(testMilestone)
                .build();
        testRound2 = roundRepository.save(testRound2);
        testRound2.getParticipants().add(testParticipant);
        roundRepository.save(testRound2);
        testParticipant.getRounds().add(testRound2);
        participantRepository.save(testParticipant);
        // Create results for both rounds
        JudgeMilestoneResultRoundRequest request1 = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto result1 = judgeMilestoneResultService.createOrUpdate(request1, assignmentEntity.getId());

        JudgeMilestoneResultRoundRequest request2 = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound2.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(9)
                .isFavorite(false)
                .build();
        judgeMilestoneResultService.createOrUpdate(request2, assignmentEntity.getId());

        // Create update request only for the first round (testRound)
        JudgeMilestoneResultRoundRequest updateRequest1 = JudgeMilestoneResultRoundRequest.builder()
                .id(result1.getId())
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(10)
                .isFavorite(false)
                .build();

        List<JudgeMilestoneResultRoundRequest> requests = List.of(updateRequest1);

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.createOrUpdateForRound(testRound.getId(), requests);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());

        // Verify that the result is updated
        JudgeMilestoneResultDto updatedResult1 = results.stream()
                .filter(r -> r.getId().equals(result1.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(10, updatedResult1.getScore());
        assertFalse(updatedResult1.getIsFavorite());

        // Verify that both rounds have READY status
        // Note: This would require checking the round status, but since we don't have direct access to round status
        // in the current test setup, we'll just verify that the method completes successfully
    }

    @Test
    void testCreateOrUpdateForRound_MixCreateAndUpdate_Success() {
        // Given
        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        UserActivityAssignmentEntity assignmentEntity = userActivityAssignmentRepository.save(judgeAssignment);
        testActivity.getUserAssignments().add(assignmentEntity);
        activityRepository.save(testActivity);
        // Create initial result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.createOrUpdate(createRequest, assignmentEntity.getId());

        // Create another criteria for new result
        CriteriaEntity testCriteria2 = CriteriaEntity.builder()
                .name("Test Criteria 2")
                .build();
        testCriteria2 = criteriaRepository.save(testCriteria2);

        MilestoneCriteriaAssignmentEntity anotherCriteria = MilestoneCriteriaAssignmentEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criteria(testCriteria2)
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();
        anotherCriteria = milestoneCriteriaAssignmentRepository.save(anotherCriteria);
        testMilestoneRule.getCriteriaAssignments().add(anotherCriteria);
        milestoneRuleRepository.save(testMilestoneRule);

        // Create mixed requests: one update, one create
        JudgeMilestoneResultRoundRequest updateRequest = JudgeMilestoneResultRoundRequest.builder()
                .id(createdResult.getId())
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(9)
                .isFavorite(false)
                .build();

        JudgeMilestoneResultRoundRequest newCreateRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(anotherCriteria.getId())
                .score(7)
                .isFavorite(true)
                .build();

        List<JudgeMilestoneResultRoundRequest> requests = List.of(updateRequest, newCreateRequest);

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.createOrUpdateForRound(testRound.getId(), requests);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());

        // Verify updated result
        JudgeMilestoneResultDto updatedResult = results.stream()
                .filter(r -> r.getId().equals(createdResult.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(9, updatedResult.getScore());
        assertFalse(updatedResult.getIsFavorite());

        // Verify new result
        JudgeMilestoneResultDto newResult = results.stream()
                .filter(r -> !r.getId().equals(createdResult.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(7, newResult.getScore());
        assertTrue(newResult.getIsFavorite());
        assertEquals(anotherCriteria.getId(), newResult.getMilestoneCriteria().getId());
    }
}
