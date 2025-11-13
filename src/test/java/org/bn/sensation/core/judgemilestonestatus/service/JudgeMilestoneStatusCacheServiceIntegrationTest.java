package org.bn.sensation.core.judgemilestonestatus.service;

import org.bn.sensation.AbstractIntegrationTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JudgeMilestoneStatusCacheServiceIntegrationTest extends AbstractIntegrationTest {
/*
    @Autowired
    private JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;

    @Autowired
    private JudgeRoundStatusRepository judgeRoundStatusRepository;

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
    private ActivityUserRepository activityUserRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private CriterionRepository criterionRepository;

    @Autowired
    private MilestoneCriterionRepository milestoneCriterionRepository;

    @Autowired
    private CacheManager cacheManager;

    // Test entities
    private OrganizationEntity testOrganization;
    private OccasionEntity testOccasion;
    private ActivityEntity testActivity;
    private MilestoneEntity testMilestone;
    private MilestoneEntity testMilestone2;
    private MilestoneRuleEntity testMilestoneRule;
    private RoundEntity testRound1;
    private RoundEntity testRound2;
    private RoundEntity testRound3;
    private UserEntity testJudge1;
    private UserEntity testJudge2;
    private ActivityUserEntity judgeAssignment1;
    private ActivityUserEntity judgeAssignment2;

    @BeforeEach
    void setUp() {
        // Clean database and cache
        cleanDatabase();
        clearCache();

        // Clean repositories
        judgeRoundStatusRepository.deleteAll();
        activityUserRepository.deleteAll();
        roundRepository.deleteAll();
        milestoneRepository.deleteAll();
        activityRepository.deleteAll();
        occasionRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
        milestoneCriterionRepository.deleteAll();
        criterionRepository.deleteAll();
        milestoneRuleRepository.deleteAll();

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
                .state(OccasionState.PLANNED)
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Create test activity
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Description")
                .state(ActivityState.PLANNED)
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

        // Create second test milestone
        testMilestone2 = MilestoneEntity.builder()
                .name("Test Milestone 2")
                .description("Test Description 2")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneOrder(2)
                .build();
        testMilestone2 = milestoneRepository.save(testMilestone2);

        // Create test milestone rule
        testMilestoneRule = MilestoneRuleEntity.builder()
                .milestone(testMilestone)
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(5)
                .strictPassMode(false)
                .build();
        testMilestoneRule = milestoneRuleRepository.save(testMilestoneRule);

        // Link milestone with rule
        testMilestone.setMilestoneRule(testMilestoneRule);
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test criterion
        CriterionEntity testCriterion = CriterionEntity.builder()
                .name("Test Criterion")
                .build();
        testCriterion = criterionRepository.save(testCriterion);

        // Create milestone criterion
        MilestoneCriterionEntity testMilestoneCriterion = MilestoneCriterionEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criterion(testCriterion)
                .partnerSide(PartnerSide.LEADER)
                .build();
        milestoneCriterionRepository.save(testMilestoneCriterion);

        // Create test users (judges)
        testJudge1 = createUser("judge1", Role.USER);
        testJudge2 = createUser("judge2", Role.USER);

        // Create judge assignments
        judgeAssignment1 = ActivityUserEntity.builder()
                .user(testJudge1)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .build();
        judgeAssignment1 = activityUserRepository.save(judgeAssignment1);

        judgeAssignment2 = ActivityUserEntity.builder()
                .user(testJudge2)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .build();
        judgeAssignment2 = activityUserRepository.save(judgeAssignment2);

        // Add assignments to activity's activityUsers collection
        testActivity.getActivityUsers().add(judgeAssignment1);
        testActivity.getActivityUsers().add(judgeAssignment2);
        activityRepository.save(testActivity);

        // Create test rounds
        testRound1 = RoundEntity.builder()
                .name("Test Round 1")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(0)
                .build();
        testRound1 = roundRepository.save(testRound1);

        testRound2 = RoundEntity.builder()
                .name("Test Round 2")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(1)
                .build();
        testRound2 = roundRepository.save(testRound2);

        testRound3 = RoundEntity.builder()
                .name("Test Round 3")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(2)
                .build();
        testRound3 = roundRepository.save(testRound3);
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

    private void clearCache() {
        cacheManager.getCache("judgeMilestoneStatus").clear();
    }

    @Test
    void getAllJudgesStatusForMilestone_WhenAllJudgesReady_ShouldReturnReadyStatus() {
        // Arrange: Create judge round statuses with all READY
        createJudgeRoundStatus(testRound1, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound1, judgeAssignment2, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound2, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound2, judgeAssignment2, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound3, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound3, judgeAssignment2, JudgeRoundStatus.READY);

        // Act
        List<JudgeMilestoneStatusDto> result = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(JudgeMilestoneStatusDto::getStatus)
                .containsExactlyInAnyOrder(JudgeMilestoneStatus.READY, JudgeMilestoneStatus.READY);
        assertThat(result).extracting(dto -> dto.getJudge().getId())
                .containsExactlyInAnyOrder(judgeAssignment1.getId(), judgeAssignment2.getId());
    }

    @Test
    void getAllJudgesStatusForMilestone_WhenNotAllJudgesReady_ShouldReturnNotReadyStatus() {
        // Arrange: Create judge round statuses with some NOT_READY
        createJudgeRoundStatus(testRound1, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound1, judgeAssignment2, JudgeRoundStatus.NOT_READY);
        createJudgeRoundStatus(testRound2, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound2, judgeAssignment2, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound3, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound3, judgeAssignment2, JudgeRoundStatus.NOT_READY);

        // Act
        List<JudgeMilestoneStatusDto> result = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());

        // Assert
        assertThat(result).hasSize(2);

        JudgeMilestoneStatusDto judge1Status = result.stream()
                .filter(dto -> dto.getJudge().getId().equals(judgeAssignment1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(judge1Status.getStatus()).isEqualTo(JudgeMilestoneStatus.READY);

        JudgeMilestoneStatusDto judge2Status = result.stream()
                .filter(dto -> dto.getJudge().getId().equals(judgeAssignment2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(judge2Status.getStatus()).isEqualTo(JudgeMilestoneStatus.NOT_READY);
    }

    @Test
    void getAllJudgesStatusForMilestone_ShouldCacheResult() {
        // Arrange
        createJudgeRoundStatus(testRound1, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound1, judgeAssignment2, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound2, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound2, judgeAssignment2, JudgeRoundStatus.READY);

        // Act: First call - should execute method and cache result
        List<JudgeMilestoneStatusDto> firstCall = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());

        // Change data in DB (should not affect cached result)
        JudgeRoundStatusEntity statusToChange = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound1.getId(), judgeAssignment1.getId()).orElseThrow();
        statusToChange.setStatus(JudgeRoundStatus.NOT_READY);
        judgeRoundStatusRepository.save(statusToChange);

        // Second call - should return cached result (still READY for both judges)
        List<JudgeMilestoneStatusDto> secondCall = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());

        // Assert: Both calls should return same result (cached)
        assertThat(secondCall).hasSize(2);
        assertThat(secondCall).extracting(JudgeMilestoneStatusDto::getStatus)
                .containsExactlyInAnyOrder(JudgeMilestoneStatus.READY, JudgeMilestoneStatus.READY);

        // Verify cache is being used (result should be same object or equal)
        assertThat(firstCall.get(0).getCalculatedAt()).isEqualTo(secondCall.get(0).getCalculatedAt());
    }

    @Test
    void getAllJudgesStatusForMilestone_AfterInvalidation_ShouldRecalculate() {
        // Arrange
        createJudgeRoundStatus(testRound1, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound1, judgeAssignment2, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound2, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound2, judgeAssignment2, JudgeRoundStatus.READY);

        // First call - cache the result
        List<JudgeMilestoneStatusDto> firstCall = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());
        assertThat(firstCall).extracting(JudgeMilestoneStatusDto::getStatus)
                .containsExactlyInAnyOrder(JudgeMilestoneStatus.READY, JudgeMilestoneStatus.READY);

        // Change data in DB
        JudgeRoundStatusEntity statusToChange = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                testRound1.getId(), judgeAssignment1.getId()).orElseThrow();
        statusToChange.setStatus(JudgeRoundStatus.NOT_READY);
        judgeRoundStatusRepository.save(statusToChange);

        // Invalidate cache
        judgeMilestoneStatusCacheService.invalidateForMilestone(testMilestone.getId());

        // Second call - should recalculate with new data
        List<JudgeMilestoneStatusDto> secondCall = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());

        // Assert: Should reflect the change
        assertThat(secondCall).hasSize(2);

        JudgeMilestoneStatusDto judge1Status = secondCall.stream()
                .filter(dto -> dto.getJudge().getId().equals(judgeAssignment1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(judge1Status.getStatus()).isEqualTo(JudgeMilestoneStatus.NOT_READY);

        JudgeMilestoneStatusDto judge2Status = secondCall.stream()
                .filter(dto -> dto.getJudge().getId().equals(judgeAssignment2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(judge2Status.getStatus()).isEqualTo(JudgeMilestoneStatus.READY);
    }

    @Test
    void getAllJudgesStatusForMilestone_WithDifferentMilestones_ShouldCacheSeparately() {
        // Arrange: Setup data for both milestones
        createJudgeRoundStatus(testRound1, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound1, judgeAssignment2, JudgeRoundStatus.READY);

        // Act: Call for first milestone
        List<JudgeMilestoneStatusDto> milestone1Result = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());

        // Act: Call for second milestone (should also return NOT_READY as no rounds exist for it)
        List<JudgeMilestoneStatusDto> milestone2Result = judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone2.getId());

        // Assert: Both should work independently
        assertThat(milestone1Result).hasSize(2);
        assertThat(milestone2Result).hasSize(2);

        // Invalidate only first milestone
        judgeMilestoneStatusCacheService.invalidateForMilestone(testMilestone.getId());

        // Second milestone cache should still be valid
        List<JudgeMilestoneStatusDto> milestone2ResultAfterInvalidation =
                judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone2.getId());
        assertThat(milestone2ResultAfterInvalidation).hasSize(2);
    }

    @Test
    void getAllJudgesStatusForMilestone_WithNullMilestoneId_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getAllJudgesStatusForMilestone_WithNonExistentMilestoneId_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(99999L))
                .isInstanceOf(Exception.class); // EntityNotFoundException from repository
    }

    @Test
    void invalidateForMilestone_ShouldClearCacheForSpecificMilestone() {
        // Arrange
        createJudgeRoundStatus(testRound1, judgeAssignment1, JudgeRoundStatus.READY);
        createJudgeRoundStatus(testRound1, judgeAssignment2, JudgeRoundStatus.READY);

        // Cache both milestones
        judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone.getId());
        judgeMilestoneStatusCacheService.getAllJudgesStatusForMilestone(testMilestone2.getId());

        // Verify cache is populated
        assertThat(cacheManager.getCache("judgeMilestoneStatus").get(testMilestone.getId())).isNotNull();
        assertThat(cacheManager.getCache("judgeMilestoneStatus").get(testMilestone2.getId())).isNotNull();

        // Act: Invalidate only first milestone
        judgeMilestoneStatusCacheService.invalidateForMilestone(testMilestone.getId());

        // Assert: First milestone cache should be cleared, second should remain
        assertThat(cacheManager.getCache("judgeMilestoneStatus").get(testMilestone.getId())).isNull();
        assertThat(cacheManager.getCache("judgeMilestoneStatus").get(testMilestone2.getId())).isNotNull();
    }

    private void createJudgeRoundStatus(RoundEntity round, ActivityUserEntity judge, JudgeRoundStatus status) {
        JudgeRoundStatusEntity judgeRoundStatus = JudgeRoundStatusEntity.builder()
                .round(round)
                .judge(judge)
                .status(status)
                .build();
        judgeRoundStatusRepository.save(judgeRoundStatus);
    }*/
}
