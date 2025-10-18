package org.bn.sensation.core.judgemilestoneresult.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
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
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultRoundRequest;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JudgeMilestoneResultCreateOrUpdateForMilestoneIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JudgeMilestoneResultService judgeMilestoneResultService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActivityAssignmentRepository userActivityAssignmentRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private CriteriaRepository criteriaRepository;

    @Autowired
    private MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Mock
    private CurrentUser mockCurrentUser;

    // Test entities
    private OccasionEntity testOccasion;
    private ActivityEntity testActivity;
    private UserEntity testJudge;
    private UserActivityAssignmentEntity testJudgeAssignment;
    private MilestoneEntity testMilestone;
    private MilestoneRuleEntity testMilestoneRule;
    private CriteriaEntity testCriteria;
    private MilestoneCriteriaAssignmentEntity testMilestoneCriteria;
    private ParticipantEntity testParticipant;
    private RoundEntity testRound;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();
        // Clean up data
        transactionTemplate.execute(status -> {
            judgeMilestoneResultService.findAll(org.springframework.data.domain.PageRequest.of(0, 1000))
                    .getContent().forEach(result ->
                            judgeMilestoneResultService.deleteById(result.getId()));
            roundRepository.deleteAll();
            participantRepository.deleteAll();
            milestoneCriteriaAssignmentRepository.deleteAll();
            criteriaRepository.deleteAll();
            milestoneRuleRepository.deleteAll();
            milestoneRepository.deleteAll();
            userActivityAssignmentRepository.deleteAll();
            userRepository.deleteAll();
            activityRepository.deleteAll();
            occasionRepository.deleteAll();
            return null;
        });


        // Create test occasion
        testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Occasion Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .state(OccasionState.IN_PROGRESS)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Create test activity
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Activity Description")
                .startDateTime(java.time.LocalDateTime.now())
                .endDateTime(java.time.LocalDateTime.now().plusHours(2))
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

        // Create test judge assignment BEFORE creating milestone
        testJudgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        testJudgeAssignment = userActivityAssignmentRepository.save(testJudgeAssignment);

        testActivity.getUserAssignments().add(testJudgeAssignment);
        activityRepository.save(testActivity);

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .state(MilestoneState.IN_PROGRESS)
                .milestoneOrder(1)
                .activity(testActivity)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test criteria
        testCriteria = CriteriaEntity.builder()
                .name("Test Criteria")
                .build();
        testCriteria = criteriaRepository.save(testCriteria);

        // Create test participant
        testParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Test Participant")
                        .surname("Test")
                        .email("participant@test.com")
                        .phoneNumber("+1234567890")
                        .build())
                .partnerSide(PartnerSide.LEADER)
                .activity(testActivity)
                .build();
        testParticipant = participantRepository.save(testParticipant);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(RoundState.IN_PROGRESS)
                .milestone(testMilestone)
                .build();
        testRound.getParticipants().add(testParticipant);
        testRound = roundRepository.save(testRound);

        testMilestone.getRounds().add(testRound);
        milestoneRepository.save(testMilestone);

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

    private void createMilestoneRuleWithAssessmentMode(AssessmentMode assessmentMode) {
        // Create test milestone rule with specific assessment mode
        testMilestoneRule = MilestoneRuleEntity.builder()
                .assessmentMode(assessmentMode)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .milestone(testMilestone)
                .build();
        testMilestoneRule = milestoneRuleRepository.save(testMilestoneRule);

        // Link milestone with rule
        testMilestone.setMilestoneRule(testMilestoneRule);
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test milestone criteria assignment
        testMilestoneCriteria = MilestoneCriteriaAssignmentEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criteria(testCriteria)
                .scale(10)
                .build();
        testMilestoneCriteria = milestoneCriteriaAssignmentRepository.save(testMilestoneCriteria);
    }

    @Test
    void testCreateOrUpdateForMilestone_SCORE_Success() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.SCORE);

        // Create initial result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.create(createRequest);

        // Create update request
        JudgeMilestoneResultMilestoneRequest updateRequest = JudgeMilestoneResultMilestoneRequest.builder()
                .id(createdResult.getId())
                .score(9)
                .isFavorite(false)
                .isCandidate(true)
                .build();

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of(updateRequest);

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.createOrUpdateForMilestone(
                testMilestone.getId(), requests);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());

        JudgeMilestoneResultDto result = results.get(0);
        assertEquals(createdResult.getId(), result.getId());
        assertEquals(9, result.getScore());
        assertFalse(result.getIsFavorite());
        assertTrue(result.getIsCandidate());
    }

    @Test
    void testCreateOrUpdateForMilestone_SCORE_InvalidScore_ThrowsException() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.SCORE);

        // Create initial result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .isFavorite(true)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.create(createRequest);

        // Create update request with invalid score (greater than scale)
        JudgeMilestoneResultMilestoneRequest updateRequest = JudgeMilestoneResultMilestoneRequest.builder()
                .id(createdResult.getId())
                .score(15) // Greater than scale (10)
                .isFavorite(false)
                .isCandidate(true)
                .build();

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of(updateRequest);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                judgeMilestoneResultService.createOrUpdateForMilestone(testMilestone.getId(), requests));
    }

    @Test
    void testCreateOrUpdateForMilestone_PASS_Success() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.PASS);

        // Create initial result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(0)
                .isFavorite(false)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.create(createRequest);

        // Create update request with pass score (1)
        JudgeMilestoneResultMilestoneRequest updateRequest = JudgeMilestoneResultMilestoneRequest.builder()
                .id(createdResult.getId())
                .score(1) // Pass score
                .isFavorite(false)
                .isCandidate(true)
                .build();

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of(updateRequest);

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.createOrUpdateForMilestone(
                testMilestone.getId(), requests);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());

        JudgeMilestoneResultDto result = results.get(0);
        assertEquals(createdResult.getId(), result.getId());
        assertEquals(1, result.getScore());
        assertFalse(result.getIsFavorite());
        assertTrue(result.getIsCandidate());
    }

    @Test
    void testCreateOrUpdateForMilestone_PASS_InvalidScore_ThrowsException() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.PASS);

        // Create initial result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(0)
                .isFavorite(false)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.create(createRequest);

        // Create update request with invalid score (not 0 or 1)
        JudgeMilestoneResultMilestoneRequest updateRequest = JudgeMilestoneResultMilestoneRequest.builder()
                .id(createdResult.getId())
                .score(2) // Invalid for PASS mode
                .isFavorite(false)
                .isCandidate(true)
                .build();

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of(updateRequest);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                judgeMilestoneResultService.createOrUpdateForMilestone(testMilestone.getId(), requests));
    }

    @Test
    void testCreateOrUpdateForMilestone_PLACE_Success() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.PLACE);

        // Create initial result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(1)
                .isFavorite(false)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.create(createRequest);

        // Create update request with place score
        JudgeMilestoneResultMilestoneRequest updateRequest = JudgeMilestoneResultMilestoneRequest.builder()
                .id(createdResult.getId())
                .score(1) // Place 1
                .isFavorite(false)
                .isCandidate(true)
                .build();

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of(updateRequest);

        // When
        List<JudgeMilestoneResultDto> results = judgeMilestoneResultService.createOrUpdateForMilestone(
                testMilestone.getId(), requests);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());

        JudgeMilestoneResultDto result = results.get(0);
        assertEquals(createdResult.getId(), result.getId());
        assertEquals(1, result.getScore());
        assertFalse(result.getIsFavorite());
        assertTrue(result.getIsCandidate());
    }

    @Test
    void testCreateOrUpdateForMilestone_PLACE_InvalidScore_ThrowsException() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.PLACE);

        // Create initial result
        JudgeMilestoneResultRoundRequest createRequest = JudgeMilestoneResultRoundRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(1)
                .isFavorite(false)
                .build();
        JudgeMilestoneResultDto createdResult = judgeMilestoneResultService.create(createRequest);

        // Create update request with invalid place (greater than participant count)
        JudgeMilestoneResultMilestoneRequest updateRequest = JudgeMilestoneResultMilestoneRequest.builder()
                .id(createdResult.getId())
                .score(5) // Invalid place (only 1 participant)
                .isFavorite(false)
                .isCandidate(true)
                .build();

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of(updateRequest);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                judgeMilestoneResultService.createOrUpdateForMilestone(testMilestone.getId(), requests));
    }

    @Test
    void testCreateOrUpdateForMilestone_NonExistentMilestone_ThrowsException() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.SCORE);

        JudgeMilestoneResultMilestoneRequest updateRequest = JudgeMilestoneResultMilestoneRequest.builder()
                .id(1L)
                .score(5)
                .isFavorite(false)
                .isCandidate(true)
                .build();

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of(updateRequest);

        // When & Then
        assertThrows(EntityNotFoundException.class, () ->
                judgeMilestoneResultService.createOrUpdateForMilestone(999L, requests));
    }

    @Test
    void testCreateOrUpdateForMilestone_EmptyList_Success() {
        // Given
        createMilestoneRuleWithAssessmentMode(AssessmentMode.SCORE);

        List<JudgeMilestoneResultMilestoneRequest> requests = List.of();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                judgeMilestoneResultService.createOrUpdateForMilestone(
                        testMilestone.getId(), requests));
    }
}
