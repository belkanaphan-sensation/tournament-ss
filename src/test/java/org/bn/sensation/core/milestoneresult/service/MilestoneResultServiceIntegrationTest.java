package org.bn.sensation.core.milestoneresult.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.criterion.repository.CriterionRepository;
import org.bn.sensation.core.milestonecriterion.repository.MilestoneCriterionRepository;
import org.bn.sensation.core.judgemilstoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatusEntity;
import org.bn.sensation.core.judgemilstoneresult.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.judgemilestonestatus.repository.JudgeMilestoneStatusRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestoneresult.entity.PassStatus;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.user.entity.*;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.bn.sensation.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Transactional
class MilestoneResultServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MilestoneResultService milestoneResultService;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityUserRepository activityUserRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CriterionRepository criterionRepository;

    @Autowired
    private MilestoneCriterionRepository milestoneCriterionRepository;

    @Autowired
    private JudgeMilestoneResultRepository judgeMilestoneResultRepository;

    @Autowired
    private JudgeMilestoneStatusRepository judgeMilestoneStatusRepository;

    @Mock
    private CurrentUser mockCurrentUser;

    // Test entities
    private UserEntity testJudge1;
    private UserEntity testJudge2;
    private UserEntity testParticipantUser1;
    private UserEntity testParticipantUser2;
    private UserEntity testParticipantUser3;
    private ActivityEntity testActivity;
    private MilestoneEntity testMilestone;
    private RoundEntity testRound;
    private ParticipantEntity testParticipant1;
    private ParticipantEntity testParticipant2;
    private ParticipantEntity testParticipant3;
    private CriterionEntity testCriteria1;
    private CriterionEntity testCriteria2;
    private MilestoneRuleEntity testMilestoneRule;
    private MilestoneCriterionEntity testMilestoneCriteria1;
    private MilestoneCriterionEntity testMilestoneCriteria2;
    private ActivityUserEntity testJudgeAssignment1;
    private ActivityUserEntity testJudgeAssignment2;

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
                .userAssignments(new HashSet<>())
                .build();
        testActivity = activityRepository.save(testActivity);

        // Create test judges
        testJudge1 = UserEntity.builder()
                .username("testjudge1")
                .password("password")
                .person(Person.builder()
                        .name("Judge1")
                        .surname("Test")
                        .email("judge1@test.com")
                        .phoneNumber("+1234567890")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        testJudge1 = userRepository.save(testJudge1);

        testJudge2 = UserEntity.builder()
                .username("testjudge2")
                .password("password")
                .person(Person.builder()
                        .name("Judge2")
                        .surname("Test")
                        .email("judge2@test.com")
                        .phoneNumber("+1234567891")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        testJudge2 = userRepository.save(testJudge2);

        // Create test judge assignments
        testJudgeAssignment1 = ActivityUserEntity.builder()
                .user(testJudge1)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        testJudgeAssignment1 = activityUserRepository.save(testJudgeAssignment1);

        testJudgeAssignment2 = ActivityUserEntity.builder()
                .user(testJudge2)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        testJudgeAssignment2 = activityUserRepository.save(testJudgeAssignment2);

        // Add judge assignments to activity
        testActivity.getUserAssignments().add(testJudgeAssignment1);
        testActivity.getUserAssignments().add(testJudgeAssignment2);
        activityRepository.save(testActivity);

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .state(MilestoneState.IN_PROGRESS)
                .milestoneOrder(0)
                .activity(testActivity)
                .rounds(new HashSet<>())
                .results(new HashSet<>())
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test milestone rule
        testMilestoneRule = MilestoneRuleEntity.builder()
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .milestone(testMilestone)
                .milestoneCriteria(new HashSet<>())
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
                .participants(new HashSet<>())
                .extraRound(false)
                .roundOrder(0)
                .build();
        testRound = roundRepository.save(testRound);

        // Add round to milestone
        testMilestone.getRounds().add(testRound);
        milestoneRepository.save(testMilestone);

        // Create test participants
        testParticipantUser1 = UserEntity.builder()
                .username("testparticipant1")
                .password("password")
                .person(Person.builder()
                        .name("Participant1")
                        .surname("Test")
                        .email("participant1@test.com")
                        .phoneNumber("+1234567892")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        testParticipantUser1 = userRepository.save(testParticipantUser1);

        testParticipantUser2 = UserEntity.builder()
                .username("testparticipant2")
                .password("password")
                .person(Person.builder()
                        .name("Participant2")
                        .surname("Test")
                        .email("participant2@test.com")
                        .phoneNumber("+1234567893")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        testParticipantUser2 = userRepository.save(testParticipantUser2);

        testParticipantUser3 = UserEntity.builder()
                .username("testparticipant3")
                .password("password")
                .person(Person.builder()
                        .name("Participant3")
                        .surname("Test")
                        .email("participant3@test.com")
                        .phoneNumber("+1234567894")
                        .build())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.USER))
                .build();
        testParticipantUser3 = userRepository.save(testParticipantUser3);

        // Create test participants
        testParticipant1 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Test1")
                        .surname("Participant")
                        .email("test1@participant.com")
                        .phoneNumber("+1234567895")
                        .build())
                .number("001")
                .partnerSide(PartnerSide.LEADER)
                .rounds(new HashSet<>())
                .build();
        testParticipant1 = participantRepository.save(testParticipant1);

        testParticipant2 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Test2")
                        .surname("Participant")
                        .email("test2@participant.com")
                        .phoneNumber("+1234567896")
                        .build())
                .number("002")
                .partnerSide(PartnerSide.LEADER)
                .rounds(new HashSet<>())
                .build();
        testParticipant2 = participantRepository.save(testParticipant2);

        testParticipant3 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Test3")
                        .surname("Participant")
                        .email("test3@participant.com")
                        .phoneNumber("+1234567897")
                        .build())
                .number("003")
                .partnerSide(PartnerSide.LEADER)
                .rounds(new HashSet<>())
                .build();
        testParticipant3 = participantRepository.save(testParticipant3);

        // Add participants to round
        testRound.getParticipants().add(testParticipant1);
        testRound.getParticipants().add(testParticipant2);
        testRound.getParticipants().add(testParticipant3);
        roundRepository.save(testRound);

        // Add round to participants
        testParticipant1.getRounds().add(testRound);
        testParticipant2.getRounds().add(testRound);
        testParticipant3.getRounds().add(testRound);
        participantRepository.save(testParticipant1);
        participantRepository.save(testParticipant2);
        participantRepository.save(testParticipant3);

        // Create test criteria
        testCriteria1 = CriterionEntity.builder()
                .name("Test Criteria 1")
                .build();
        testCriteria1 = criterionRepository.save(testCriteria1);

        testCriteria2 = CriterionEntity.builder()
                .name("Test Criteria 2")
                .build();
        testCriteria2 = criterionRepository.save(testCriteria2);

        // Create test milestone criteria assignments
        testMilestoneCriteria1 = MilestoneCriterionEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criterion(testCriteria1)
                .partnerSide(PartnerSide.LEADER)
                .weight(BigDecimal.valueOf(0.6))
                .scale(10)
                .build();
        testMilestoneCriteria1 = milestoneCriterionRepository.save(testMilestoneCriteria1);

        testMilestoneCriteria2 = MilestoneCriterionEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criterion(testCriteria2)
                .partnerSide(PartnerSide.LEADER)
                .weight(BigDecimal.valueOf(0.4))
                .scale(10)
                .build();
        testMilestoneCriteria2 = milestoneCriterionRepository.save(testMilestoneCriteria2);

        // Add criteria assignments to milestone rule
        testMilestoneRule.getMilestoneCriteria().add(testMilestoneCriteria1);
        testMilestoneRule.getMilestoneCriteria().add(testMilestoneCriteria2);
        milestoneRuleRepository.save(testMilestoneRule);

        // No need to setup current user for MilestoneResultService
    }

    @Test
    void testCalculateResults_Success() {
        // Given - Create judge results for all participants
        createJudgeResults();

        // When
        List<MilestoneResultDto> results = milestoneResultService.calculateResults(testMilestone.getId());

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());

        // Verify that all results have valid scores
        results.forEach(result -> {
            assertNotNull(result.getTotalScore());
            assertTrue(result.getTotalScore() >= 0);
        });

        // Verify pass statuses based on limit (FINAL_RESULT_LIMIT = 3)
        long passedCount = results.stream()
                .mapToLong(r -> r.getJudgePassed() == PassStatus.PASSED ? 1 : 0)
                .sum();
        long pendingCount = results.stream()
                .mapToLong(r -> r.getJudgePassed() == PassStatus.PENDING ? 1 : 0)
                .sum();
        long failedCount = results.stream()
                .mapToLong(r -> r.getJudgePassed() == PassStatus.FAILED ? 1 : 0)
                .sum();

        // With 3 participants and limit 3, all should get PASSED
        assertEquals(3, passedCount);
        assertEquals(0, pendingCount);
        assertEquals(0, failedCount);
    }

    @Test
    void testCalculateResults_WithTiedScores() {
        // Given - Create judge results with tied scores
        createJudgeResultsWithTies();

        // When
        List<MilestoneResultDto> results = milestoneResultService.calculateResults(testMilestone.getId());

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());

        // Verify that participants with tied scores get PASSED status (since limit = 3 and we have 3 participants)
        long passedCount = results.stream()
                .mapToLong(r -> r.getJudgePassed() == PassStatus.PASSED ? 1 : 0)
                .sum();
        long pendingCount = results.stream()
                .mapToLong(r -> r.getJudgePassed() == PassStatus.PENDING ? 1 : 0)
                .sum();

        // With 3 participants with tied scores and limit 3, all should get PASSED
        assertEquals(3, passedCount);
        assertEquals(0, pendingCount);
    }

    @Test
    void testCalculateResults_NoResults_ThrowsException() {
        // Given - No judge results created

        // When & Then
        assertThrows(IllegalStateException.class, () ->
            milestoneResultService.calculateResults(testMilestone.getId()));
    }

    @Test
    void testCalculateResults_NotAllJudgesCompleted_ThrowsException() {
        // Given - Create results for only one judge
        createJudgeResultsForOneJudge();

        // When & Then
        assertThrows(IllegalStateException.class, () ->
            milestoneResultService.calculateResults(testMilestone.getId()));
    }

    @Test
    void testCalculateResults_NullMilestoneId_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            milestoneResultService.calculateResults(null));
    }

    @Test
    void testCalculateResults_NonExistentMilestone_ThrowsException() {
        // When & Then
        assertThrows(Exception.class, () ->
            milestoneResultService.calculateResults(999L));
    }

    private void createJudgeResults() {
        // Create judge milestone statuses
        JudgeMilestoneStatusEntity judgeStatus1 = JudgeMilestoneStatusEntity.builder()
                .judge(testJudgeAssignment1)
                .milestone(testMilestone)
                .status(JudgeMilestoneStatus.READY)
                .build();
        judgeMilestoneStatusRepository.save(judgeStatus1);

        JudgeMilestoneStatusEntity judgeStatus2 = JudgeMilestoneStatusEntity.builder()
                .judge(testJudgeAssignment2)
                .milestone(testMilestone)
                .status(JudgeMilestoneStatus.READY)
                .build();
        judgeMilestoneStatusRepository.save(judgeStatus2);

        // Create judge results for participant 1 (highest score)
        JudgeMilestoneResultEntity result1_1 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment1)
                .score(9)
                .isFavorite(true)
                .build();
        judgeMilestoneResultRepository.save(result1_1);

        JudgeMilestoneResultEntity result1_2 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment1)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result1_2);

        JudgeMilestoneResultEntity result1_3 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment2)
                .score(9)
                .isFavorite(true)
                .build();
        judgeMilestoneResultRepository.save(result1_3);

        JudgeMilestoneResultEntity result1_4 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment2)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result1_4);

        // Create judge results for participant 2 (medium score)
        JudgeMilestoneResultEntity result2_1 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment1)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_1);

        JudgeMilestoneResultEntity result2_2 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment1)
                .score(6)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_2);

        JudgeMilestoneResultEntity result2_3 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment2)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_3);

        JudgeMilestoneResultEntity result2_4 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment2)
                .score(6)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_4);

        // Create judge results for participant 3 (lowest score)
        JudgeMilestoneResultEntity result3_1 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment1)
                .score(5)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_1);

        JudgeMilestoneResultEntity result3_2 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment1)
                .score(4)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_2);

        JudgeMilestoneResultEntity result3_3 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment2)
                .score(5)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_3);

        JudgeMilestoneResultEntity result3_4 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment2)
                .score(4)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_4);
    }

    private void createJudgeResultsWithTies() {
        // Create judge milestone statuses
        JudgeMilestoneStatusEntity judgeStatus1 = JudgeMilestoneStatusEntity.builder()
                .judge(testJudgeAssignment1)
                .milestone(testMilestone)
                .status(JudgeMilestoneStatus.READY)
                .build();
        judgeMilestoneStatusRepository.save(judgeStatus1);

        JudgeMilestoneStatusEntity judgeStatus2 = JudgeMilestoneStatusEntity.builder()
                .judge(testJudgeAssignment2)
                .milestone(testMilestone)
                .status(JudgeMilestoneStatus.READY)
                .build();
        judgeMilestoneStatusRepository.save(judgeStatus2);

        // Create judge results with tied scores (all participants get same total score)
        // Participant 1: 8*0.6 + 7*0.4 = 7.6
        JudgeMilestoneResultEntity result1_1 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment1)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result1_1);

        JudgeMilestoneResultEntity result1_2 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment1)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result1_2);

        JudgeMilestoneResultEntity result1_3 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment2)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result1_3);

        JudgeMilestoneResultEntity result1_4 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment2)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result1_4);

        // Participant 2: same scores
        JudgeMilestoneResultEntity result2_1 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment1)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_1);

        JudgeMilestoneResultEntity result2_2 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment1)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_2);

        JudgeMilestoneResultEntity result2_3 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment2)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_3);

        JudgeMilestoneResultEntity result2_4 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant2)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment2)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result2_4);

        // Participant 3: same scores
        JudgeMilestoneResultEntity result3_1 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment1)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_1);

        JudgeMilestoneResultEntity result3_2 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment1)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_2);

        JudgeMilestoneResultEntity result3_3 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment2)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_3);

        JudgeMilestoneResultEntity result3_4 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant3)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment2)
                .score(7)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result3_4);
    }

    private void createJudgeResultsForOneJudge() {
        // Create judge milestone status for only one judge
        JudgeMilestoneStatusEntity judgeStatus1 = JudgeMilestoneStatusEntity.builder()
                .judge(testJudgeAssignment1)
                .milestone(testMilestone)
                .status(JudgeMilestoneStatus.READY)
                .build();
        judgeMilestoneStatusRepository.save(judgeStatus1);

        // Create results for only one judge
        JudgeMilestoneResultEntity result1_1 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria1)
                .activityUser(testJudgeAssignment1)
                .score(9)
                .isFavorite(true)
                .build();
        judgeMilestoneResultRepository.save(result1_1);

        JudgeMilestoneResultEntity result1_2 = JudgeMilestoneResultEntity.builder()
                .participant(testParticipant1)
                .round(testRound)
                .milestoneCriterion(testMilestoneCriteria2)
                .activityUser(testJudgeAssignment1)
                .score(8)
                .isFavorite(false)
                .build();
        judgeMilestoneResultRepository.save(result1_2);
    }
}
