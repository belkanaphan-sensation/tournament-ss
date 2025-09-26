package org.bn.sensation.core.round.service;

import static org.junit.jupiter.api.Assertions.*;

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
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.repository.RoundResultRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundResultRequest;
import org.bn.sensation.core.round.service.dto.RoundResultDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundResultRequest;
import org.bn.sensation.core.round.service.mapper.CreateRoundResultRequestMapper;
import org.bn.sensation.core.round.service.mapper.RoundResultDtoMapper;
import org.bn.sensation.core.round.service.mapper.UpdateRoundResultRequestMapper;
import org.bn.sensation.security.CurrentUser;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.entity.UserActivityPosition;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.EntityNotFoundException;

@Transactional
class RoundResultServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoundResultService roundResultService;


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
    private RoundResultDtoMapper roundResultDtoMapper;

    @Autowired
    private CreateRoundResultRequestMapper createRoundResultRequestMapper;

    @Autowired
    private UpdateRoundResultRequestMapper updateRoundResultRequestMapper;

    @Autowired
    private RoundResultRepository roundResultRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private CriteriaRepository criteriaRepository;

    // Test entities
    private UserEntity testJudge;
    private UserEntity testParticipantUser;
    private ActivityEntity testActivity;
    private MilestoneEntity testMilestone;
    private RoundEntity testRound;
    private ParticipantEntity testParticipant;
    private CriteriaEntity testCriteria;
    private MilestoneCriteriaAssignmentEntity testMilestoneCriteria;

    @BeforeEach
    void setUp() {
        // Create test occasion
        OccasionEntity testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .state(State.IN_PROGRESS)
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
                .state(State.IN_PROGRESS)
                .occasion(testOccasion)
                .build();
        testActivity = activityRepository.save(testActivity);

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .state(State.IN_PROGRESS)
                .milestoneOrder(1)
                .activity(testActivity)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .description("Test Round Description")
                .state(State.IN_PROGRESS)
                .milestone(testMilestone)
                .build();
        testRound = roundRepository.save(testRound);

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
                .milestone(testMilestone)
                .criteria(testCriteria)
                .partnerSide(PartnerSide.LEADER)
                .weight(BigDecimal.ONE)
                .scale(10)
                .build();
        testMilestoneCriteria = milestoneCriteriaAssignmentRepository.save(testMilestoneCriteria);

        // Set up security context with judge user
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(testJudge);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();

        // When
        RoundResultDto result = roundResultService.create(request);

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
        assertEquals(judgeAssignment.getId(), result.getActivityUser().getId());
    }

    @Test
    void testCreateRoundResult_NonJudgeUser_ThrowsException() {
        // Given
        UserActivityAssignmentEntity participantAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge) // Используем testJudge, но с позицией PARTICIPANT
                .activity(testActivity)
                .position(UserActivityPosition.PARTICIPANT) // Не судья!
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(participantAssignment);

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roundResultService.create(request));
        assertEquals("Оценивающий должен быть судьей", exception.getMessage());
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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roundResultService.create(request));
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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(otherParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roundResultService.create(request));
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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();

        // Create first result
        roundResultService.create(request);

        // When & Then - try to create duplicate
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roundResultService.create(request));
        assertEquals("Результат уже существует для данного раунда, участника, судьи и критерия", exception.getMessage());
    }

    @Test
    void testCreateRoundResult_CommonCriteriaWithAnyJudgeSide_Success() {
        // Given
        // Create common criteria (no partner side)
        MilestoneCriteriaAssignmentEntity commonCriteria = MilestoneCriteriaAssignmentEntity.builder()
                .milestone(testMilestone)
                .criteria(testCriteria)
                .partnerSide(null) // Common criteria
                .weight(BigDecimal.ONE)
                .scale(10)
                .build();
        commonCriteria = milestoneCriteriaAssignmentRepository.save(commonCriteria);

        UserActivityAssignmentEntity judgeAssignment = UserActivityAssignmentEntity.builder()
                .user(testJudge)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.FOLLOWER) // Different side, but should work for common criteria
                .build();
        userActivityAssignmentRepository.save(judgeAssignment);

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(commonCriteria.getId())
                .score(9)
                .build();

        // When
        RoundResultDto result = roundResultService.create(request);

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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(999L) // Non-existent ID
                .score(8)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> roundResultService.create(request));
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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(999L) // Non-existent ID
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> roundResultService.create(request));
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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(999L) // Non-existent ID
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> roundResultService.create(request));
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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(8)
                .build();
        RoundResultDto createdResult = roundResultService.create(request);

        // When
        List<RoundResultDto> results = roundResultService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(createdResult.getId(), results.get(0).getId());
        assertEquals(8, results.get(0).getScore());
    }

    @Test
    void testFindByRoundId_EmptyResult() {
        // When
        List<RoundResultDto> results = roundResultService.findByRoundId(testRound.getId());

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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(9)
                .build();
        RoundResultDto createdResult = roundResultService.create(request);

        // When
        List<RoundResultDto> results = roundResultService.findByMilestoneId(testMilestone.getId());

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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(7)
                .build();
        RoundResultDto createdResult = roundResultService.create(request);

        // When
        List<RoundResultDto> results = roundResultService.findByParticipantId(testParticipant.getId());

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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(6)
                .build();
        RoundResultDto createdResult = roundResultService.create(request);

        // When
        List<RoundResultDto> results = roundResultService.findByActivityUserId(judgeAssignment.getId());

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

        CreateRoundResultRequest request = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(10)
                .build();
        RoundResultDto createdResult = roundResultService.create(request);

        // When
        Optional<RoundResultDto> result = roundResultService.findById(createdResult.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(createdResult.getId(), result.get().getId());
        assertEquals(10, result.get().getScore());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<RoundResultDto> result = roundResultService.findById(999L);

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

        CreateRoundResultRequest createRequest = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .build();
        RoundResultDto createdResult = roundResultService.create(createRequest);

        UpdateRoundResultRequest updateRequest = new UpdateRoundResultRequest();
        updateRequest.setScore(8);

        // When
        RoundResultDto updatedResult = roundResultService.update(createdResult.getId(), updateRequest);

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

        CreateRoundResultRequest createRequest = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .build();
        RoundResultDto createdResult = roundResultService.create(createRequest);

        // Create another user with USER role (not admin)
        final UserEntity otherUser = userRepository.save(UserEntity.builder()
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

        // Create a new RoundResultService with the other user as current user
        // This is a workaround since CurrentUser is injected and doesn't change with SecurityContext
        RoundResultService otherUserRoundResultService = new org.bn.sensation.core.round.service.RoundResultServiceImpl(
                roundResultRepository,
                roundResultDtoMapper,
                createRoundResultRequestMapper,
                updateRoundResultRequestMapper,
                milestoneCriteriaAssignmentRepository,
                userActivityAssignmentRepository,
                roundRepository,
                participantRepository,
                new CurrentUser() {
                    @Override
                    public SecurityUser getSecurityUser() {
                        return (SecurityUser) SecurityUser.fromUser(otherUser);
                    }
                }
        );

        UpdateRoundResultRequest updateRequest = new UpdateRoundResultRequest();
        updateRequest.setScore(8);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> otherUserRoundResultService.update(createdResult.getId(), updateRequest));
        assertEquals("Нельзя изменить результат", exception.getMessage());
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

        CreateRoundResultRequest createRequest = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .build();
        RoundResultDto createdResult = roundResultService.create(createRequest);

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

        UpdateRoundResultRequest updateRequest = new UpdateRoundResultRequest();
        updateRequest.setScore(9);

        // When
        RoundResultDto updatedResult = roundResultService.update(createdResult.getId(), updateRequest);

        // Then
        assertNotNull(updatedResult);
        assertEquals(createdResult.getId(), updatedResult.getId());
        assertEquals(9, updatedResult.getScore());
    }

    @Test
    void testUpdate_NotFound_ThrowsException() {
        // Given
        UpdateRoundResultRequest updateRequest = new UpdateRoundResultRequest();
        updateRequest.setScore(8);

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> roundResultService.update(999L, updateRequest));
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

        CreateRoundResultRequest createRequest = CreateRoundResultRequest.builder()
                .participantId(testParticipant.getId())
                .roundId(testRound.getId())
                .milestoneCriteriaId(testMilestoneCriteria.getId())
                .score(5)
                .build();
        RoundResultDto createdResult = roundResultService.create(createRequest);

        // When
        roundResultService.deleteById(createdResult.getId());

        // Then
        Optional<RoundResultDto> result = roundResultService.findById(createdResult.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteById_NotFound_ThrowsException() {
        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> roundResultService.deleteById(999L));
    }
}
