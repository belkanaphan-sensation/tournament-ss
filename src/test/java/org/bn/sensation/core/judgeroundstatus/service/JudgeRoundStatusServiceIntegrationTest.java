package org.bn.sensation.core.judgeroundstatus.service;

import org.bn.sensation.AbstractIntegrationTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JudgeRoundStatusServiceIntegrationTest extends AbstractIntegrationTest {
/*
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

        // Add assignment to activity's activityUsers collection
        testActivity.getActivityUsers().add(judgeAssignment);
        activityRepository.save(testActivity);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(RoundState.OPENED)
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
    void testMarkNotReady_ShouldChangeStatusToNotReady() {
        // Given - Create judge round status with READY status
        mockCurrentUser(testJudge);

        JudgeRoundStatusEntity status = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        status = judgeRoundStatusRepository.save(status);

        // When
        JudgeRoundStatusDto result = judgeRoundStatusService.markNotReady(testRound.getId());

        // Then - Verify status changed to NOT_READY
        assertNotNull(result);
        assertEquals(JudgeRoundStatus.NOT_READY, result.getStatus());
        assertEquals(judgeAssignment.getId(), result.getJudge().getId());
        assertEquals(testRound.getId(), result.getRound().getId());

        // Verify in database
        JudgeRoundStatusEntity savedStatus = judgeRoundStatusRepository.findById(status.getId()).orElseThrow();
        assertEquals(JudgeRoundStatus.NOT_READY, savedStatus.getStatus());
    }

    @Test
    void testMarkNotReady_WithNonExistentRound_ShouldThrowException() {
        // Given
        mockCurrentUser(testJudge);

        // When & Then
        assertThrows(Exception.class, () -> {
            judgeRoundStatusService.markNotReady(99999L);
        });
    }

    @Test
    void testMarkNotReady_WithNullRoundId_ShouldThrowException() {
        // Given
        mockCurrentUser(testJudge);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            judgeRoundStatusService.markNotReady(null);
        });
    }

    @Test
    void testMarkNotReady_WithNonExistentStatus_ShouldThrowException() {
        // Given - Create round but no judge round status
        mockCurrentUser(testJudge);

        RoundEntity roundWithoutStatus = RoundEntity.builder()
                .name("Round Without Status")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(1)
                .build();
        final RoundEntity savedRound = roundRepository.save(roundWithoutStatus);

        // When & Then
        assertThrows(Exception.class, () -> {
            judgeRoundStatusService.markNotReady(savedRound.getId());
        });
    }

    @Test
    void testGetRoundStatusForCurrentUser_WithExistingStatus_ShouldReturnStatus() {
        // Given - Create judge round status
        mockCurrentUser(testJudge);

        JudgeRoundStatusEntity status = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(status);

        // When
        JudgeRoundStatus result = judgeRoundStatusService.getRoundStatusForCurrentUser(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(JudgeRoundStatus.READY, result);
    }

    @Test
    void testGetRoundStatusForCurrentUser_WithNoStatus_ShouldReturnNull() {
        // Given - No judge round status created
        mockCurrentUser(testJudge);

        // When
        JudgeRoundStatus result = judgeRoundStatusService.getRoundStatusForCurrentUser(testRound.getId());

        // Then
        assertNull(result);
    }

    @Test
    void testGetRoundStatusForCurrentUser_WithNonExistentRound_ShouldThrowException() {
        // Given
        mockCurrentUser(testJudge);

        // When & Then
        assertThrows(Exception.class, () -> {
            judgeRoundStatusService.getRoundStatusForCurrentUser(99999L);
        });
    }

    @Test
    void testGetByMilestoneIdForCurrentUser_WithMultipleRounds_ShouldReturnAllStatuses() {
        // Given - Create multiple rounds with judge statuses
        mockCurrentUser(testJudge);

        RoundEntity round1 = RoundEntity.builder()
                .name("Round 1")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(0)
                .build();
        final RoundEntity savedRound1 = roundRepository.save(round1);

        RoundEntity round2 = RoundEntity.builder()
                .name("Round 2")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(1)
                .build();
        final RoundEntity savedRound2 = roundRepository.save(round2);

        // Create judge round statuses
        JudgeRoundStatusEntity status1 = JudgeRoundStatusEntity.builder()
                .round(savedRound1)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(status1);

        JudgeRoundStatusEntity status2 = JudgeRoundStatusEntity.builder()
                .round(savedRound2)
                .judge(judgeAssignment)
                .status(JudgeRoundStatus.NOT_READY)
                .build();
        judgeRoundStatusRepository.save(status2);

        // When
        List<JudgeRoundStatusDto> result = judgeRoundStatusService.getByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify statuses
        JudgeRoundStatusDto status1Dto = result.stream()
                .filter(dto -> dto.getRound().getId().equals(savedRound1.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(JudgeRoundStatus.READY, status1Dto.getStatus());

        JudgeRoundStatusDto status2Dto = result.stream()
                .filter(dto -> dto.getRound().getId().equals(savedRound2.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(JudgeRoundStatus.NOT_READY, status2Dto.getStatus());
    }

    @Test
    void testGetByMilestoneIdForCurrentUser_WithNoStatuses_ShouldReturnEmptyList() {
        // Given - No judge round statuses created
        mockCurrentUser(testJudge);

        // When
        List<JudgeRoundStatusDto> result = judgeRoundStatusService.getByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByMilestoneIdForCurrentUser_WithNonExistentMilestone_ShouldThrowException() {
        // Given
        mockCurrentUser(testJudge);

        // When & Then
        assertThrows(Exception.class, () -> {
            judgeRoundStatusService.getByMilestoneIdForCurrentUser(99999L);
        });
    }

    @Test
    void testGetByMilestoneIdForCurrentUser_WithDifferentJudge_ShouldReturnEmptyList() {
        // Given - Create status for different judge
        mockCurrentUser(testJudge);

        // Create another judge
        UserEntity otherJudge = createUser("otherJudge", Role.USER);
        ActivityUserEntity otherJudgeAssignment = ActivityUserEntity.builder()
                .user(otherJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .build();
        otherJudgeAssignment = activityUserRepository.save(otherJudgeAssignment);

        testActivity.getActivityUsers().add(otherJudgeAssignment);
        activityRepository.save(testActivity);

        // Create status for other judge
        JudgeRoundStatusEntity statusForOtherJudge = JudgeRoundStatusEntity.builder()
                .round(testRound)
                .judge(otherJudgeAssignment)
                .status(JudgeRoundStatus.READY)
                .build();
        judgeRoundStatusRepository.save(statusForOtherJudge);

        // When - Get statuses for current user (testJudge)
        List<JudgeRoundStatusDto> result = judgeRoundStatusService.getByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then - Should return empty list (status belongs to other judge)
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }*/
}
