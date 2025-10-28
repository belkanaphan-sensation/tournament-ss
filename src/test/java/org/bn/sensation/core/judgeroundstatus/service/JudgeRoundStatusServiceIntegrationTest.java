package org.bn.sensation.core.judgeroundstatus.service;

import java.time.LocalDate;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.entity.UserActivityPosition;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.repository.CriterionRepository;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.repository.MilestoneCriterionRepository;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
        testActivity.getActivityUsers().add(judgeAssignment);
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

}
