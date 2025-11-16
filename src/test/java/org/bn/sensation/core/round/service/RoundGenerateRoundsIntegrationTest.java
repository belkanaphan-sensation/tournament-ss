package org.bn.sensation.core.round.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Интеграционные тесты для метода generateRounds в RoundService
 */
@Transactional
class RoundGenerateRoundsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoundService roundService;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    // Тестовые сущности
    private MilestoneEntity testMilestone;
    private ActivityEntity testActivity;
    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private MilestoneRuleEntity testMilestoneRule;

    // Тестовые участники
    private ParticipantEntity leader1;
    private ParticipantEntity leader2;
    private ParticipantEntity leader3;
    private ParticipantEntity leader4;
    private ParticipantEntity leader5;
    private ParticipantEntity follower1;
    private ParticipantEntity follower2;
    private ParticipantEntity follower3;
    private ParticipantEntity follower4;
    private ParticipantEntity follower5;
    private ParticipantEntity unregisteredParticipant;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Clean up existing data
        roundRepository.deleteAll();
        participantRepository.deleteAll();
        milestoneRuleRepository.deleteAll();
        milestoneRepository.deleteAll();
        activityRepository.deleteAll();
        occasionRepository.deleteAll();
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
                .endDate(LocalDate.now().plusDays(3))
                .state(OccasionState.PLANNED)
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

        // Create milestone rule
        testMilestoneRule = MilestoneRuleEntity.builder()
                .milestone(null) // Will be set after milestone creation
                .roundParticipantLimit(3) // 3 участника на одну PartnerSide (лидеры или последователи)
                .participantLimit(20)
                .strictPassMode(false)
                .assessmentMode(AssessmentMode.PASS)
                .build();
        testMilestoneRule = milestoneRuleRepository.save(testMilestoneRule);

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneRule(testMilestoneRule)
                .milestoneOrder(1)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);
        testMilestoneRule.setMilestone(testMilestone);
        milestoneRuleRepository.save(testMilestoneRule);

        // Update milestone rule with milestone reference
        testMilestoneRule.setMilestone(testMilestone);
        milestoneRuleRepository.save(testMilestoneRule);

        // Create test participants
        createTestParticipants();
    }

    private List<ParticipantEntity> createTestParticipants() {
        // Лидеры
        leader1 = createParticipant("Leader1", PartnerSide.LEADER, "1", true);
        leader2 = createParticipant("Leader2", PartnerSide.LEADER, "2", true);
        leader3 = createParticipant("Leader3", PartnerSide.LEADER, "3", true);
        leader4 = createParticipant("Leader4", PartnerSide.LEADER, "4", true);
        leader5 = createParticipant("Leader5", PartnerSide.LEADER, "5", true);

        // Последователи
        follower1 = createParticipant("Follower1", PartnerSide.FOLLOWER, "6", true);
        follower2 = createParticipant("Follower2", PartnerSide.FOLLOWER, "7", true);
        follower3 = createParticipant("Follower3", PartnerSide.FOLLOWER, "8", true);
        follower4 = createParticipant("Follower4", PartnerSide.FOLLOWER, "9", true);
        follower5 = createParticipant("Follower5", PartnerSide.FOLLOWER, "10", true);

        // Незарегистрированный участник
        unregisteredParticipant = createParticipant("Unregistered", PartnerSide.LEADER, "11", false);
        return List.of(leader1, leader2, leader3, leader4, leader5, follower1, follower2, follower3, follower4, follower5, unregisteredParticipant);
    }

    private ParticipantEntity createParticipant(String name, PartnerSide partnerSide, String number, boolean isRegistered) {
        ParticipantEntity participant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name(name)
                        .surname("Test")
                        .email(name.toLowerCase() + "@test.com")
                        .build())
                .partnerSide(partnerSide)
                .number(number)
                .isRegistered(isRegistered)
                .activity(testActivity)
                .rounds(new HashSet<>())
                .milestones(new HashSet<>())
                .build();
        return participantRepository.save(participant);
    }

    @Test
    void testGenerateRounds_AllParticipants_Success() {
        // When
        List<RoundDto> result = roundService.generateRounds(testMilestone, null, false, 3);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 5 лидеров / 3 лимит = 2 раунда (1 полный + 1 с остатком)

        // Проверяем первый раунд
        RoundDto round1 = result.get(0);
        assertEquals("Раунд 1", round1.getName());
        assertEquals(RoundState.OPENED, round1.getState());
        assertEquals(0, round1.getRoundOrder());
        assertEquals(6, round1.getParticipants().size()); // 3 лидера + 3 последователя

        // Проверяем второй раунд
        RoundDto round2 = result.get(1);
        assertEquals("Раунд 2", round2.getName());
        assertEquals(RoundState.OPENED, round2.getState());
        assertEquals(1, round2.getRoundOrder());
        assertEquals(4, round2.getParticipants().size()); // 2 лидера + 2 последователя

        // Проверяем что участники правильно распределены
        Set<Long> allParticipantIds = new HashSet<>();
        result.forEach(round ->
            round.getParticipants().forEach(participant ->
                allParticipantIds.add(participant.getId())
            )
        );
        assertEquals(10, allParticipantIds.size()); // Все 10 зарегистрированных участников
    }

    @Test
    void testGenerateRounds_SpecificParticipants_Success() {
        // Given
        List<Long> specificParticipantIds = Arrays.asList(leader1.getId(), leader2.getId(), leader3.getId(), follower1.getId(), follower2.getId());

        // When
        List<RoundDto> result = roundService.generateRounds(testMilestone, specificParticipantIds, false, 3);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        RoundDto round1 = result.get(0);
        assertEquals("Раунд 1", round1.getName());
        assertEquals(5, round1.getParticipants().size()); // 3 лидера + 3 последователя (но у нас только 2 последователя)

        // Проверяем что только указанные участники добавлены
        Set<Long> participantIds = new HashSet<>();
        result.forEach(round ->
            round.getParticipants().forEach(participant ->
                participantIds.add(participant.getId())
            )
        );
        assertTrue(participantIds.contains(leader1.getId()));
        assertTrue(participantIds.contains(leader2.getId()));
        assertTrue(participantIds.contains(leader3.getId()));
        assertTrue(participantIds.contains(follower1.getId()));
        assertTrue(participantIds.contains(follower2.getId()));
        assertEquals(5, participantIds.size());
    }

    @Test
    void testGenerateRounds_ReGenerate_Success() {
        // Given - сначала создаем раунды
        roundService.generateRounds(testMilestone, null, false, 3);

        // When - перегенерируем
        List<RoundDto> result = roundService.generateRounds(testMilestone, null, true, 3);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 5 лидеров / 3 лимит = 2 раунда
    }

    @Test
    void testGenerateRounds_EmptyParticipants_ReturnsEmptyList() {
        // Given - удаляем всех участников
        participantRepository.deleteAll();

        // When
        List<RoundDto> result = roundService.generateRounds(testMilestone, null, false, 3);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateRounds_OnlyLeaders_Success() {
        // Given - удаляем всех последователей
        participantRepository.delete(follower1);
        participantRepository.delete(follower2);
        participantRepository.delete(follower3);
        participantRepository.delete(follower4);
        participantRepository.delete(follower5);

        // When
        List<RoundDto> result = roundService.generateRounds(testMilestone, null, false, 3);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 5 лидеров / 3 лимит = 2 раунда (1 полный + 1 с остатком)

        RoundDto round = result.get(0);
        assertEquals(3, round.getParticipants().size()); // Первый раунд: 3 лидера
    }

    @Test
    void testGenerateRounds_MilestoneAlreadyHasParticipants_ThrowsException() {
        // Given - добавляем участника в этап
        testMilestone.getParticipants().add(leader1);
        milestoneRepository.save(testMilestone);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roundService.generateRounds(testMilestone, null, false, 3);
        });
    }

    @Test
    void testGenerateRounds_NonExistentParticipant_ThrowsException() {
        // Given
        List<Long> nonExistentParticipantIds = Arrays.asList(999L, 1000L);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roundService.generateRounds(testMilestone, nonExistentParticipantIds, false, 3);
        });
    }

    @Test
    void testGenerateRounds_UnregisteredParticipant_ThrowsException() {
        // Given
        List<Long> participantIds = Arrays.asList(leader1.getId(), unregisteredParticipant.getId());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            roundService.generateRounds(testMilestone, participantIds, false, 3);
        });
    }

    @Test
    void testGenerateRounds_LargeNumberOfParticipants_CreatesMultipleRounds() {
        // Given - создаем больше участников
        for (int i = 8; i <= 15; i++) {
            createParticipant("Leader" + i, PartnerSide.LEADER, String.valueOf(i), true);
            createParticipant("Follower" + i, PartnerSide.FOLLOWER, String.valueOf(i + 10), true);
        }

        // When
        List<RoundDto> result = roundService.generateRounds(testMilestone, null, false, 3);

        // Then
        assertNotNull(result);
        // 10 исходных + 16 новых = 26 участников (13 лидеров + 13 последователей)
        // 13 лидеров / 3 лимит = 4 раундов (4 полных + 1 распределится)
        assertEquals(4, result.size());

        // Проверяем что все раунды имеют правильный порядок
        for (int i = 0; i < result.size(); i++) {
            assertEquals(i, result.get(i).getRoundOrder());
            assertEquals("Раунд " + (i + 1), result.get(i).getName());
        }
    }

    @Test
    void testGenerateRounds_ParticipantDistribution_LeadersAndFollowers() {
        // When
        List<RoundDto> result = roundService.generateRounds(testMilestone, null, false, 3);

        // Then
        assertNotNull(result);

        // Проверяем что каждый раунд содержит участников
        for (RoundDto round : result) {
            assertNotNull(round.getParticipants());
            assertFalse(round.getParticipants().isEmpty(), "Раунд должен содержать хотя бы одного участника");
        }

        // Проверяем что все участники распределены по раундам
        Set<Long> allParticipantIds = new HashSet<>();
        result.forEach(round ->
            round.getParticipants().forEach(participant ->
                allParticipantIds.add(participant.getId())
            )
        );
        assertEquals(10, allParticipantIds.size()); // Все 10 зарегистрированных участников
    }
}
