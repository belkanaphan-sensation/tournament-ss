package org.bn.sensation.core.round.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.entity.State;
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
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private MilestoneEntity testMilestone;
    private MilestoneEntity testMilestone1;
    private ParticipantEntity testParticipant;
    private ParticipantEntity testParticipant1;
    private ActivityEntity testActivity;
    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private RoundEntity testRound;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Clean up existing data
        roundRepository.deleteAll();
        participantRepository.deleteAll();
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
                .state(State.DRAFT)
                .endDate(LocalDate.now().plusDays(3))
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Create test activity
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Activity Description")
                .state(State.DRAFT)
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

        // Create test milestones
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .state(State.DRAFT)
                .activity(testActivity)
                .milestoneOrder(1)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        testMilestone1 = MilestoneEntity.builder()
                .name("Test Milestone 1")
                .state(State.DRAFT)
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
                .build();
        testParticipant1 = participantRepository.save(testParticipant1);

        // Create test round
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(State.DRAFT)
                .description("Test Round Description")
                .milestone(testMilestone)
                .participants(new HashSet<>(Set.of(testParticipant)))
                .build();
        testRound = roundRepository.save(testRound);
    }

    @Test
    void testCreateRound() {
        // Given
        CreateRoundRequest request = CreateRoundRequest.builder()
                .name("New Round")
                .state(State.DRAFT)
                .description("New Round Description")
                .milestoneId(testMilestone.getId())
                .build();

        // When
        RoundDto result = roundService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
        assertNotNull(result.getMilestone());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());

        // Verify round was saved to database
        Optional<RoundEntity> savedRound = roundRepository.findById(result.getId());
        assertTrue(savedRound.isPresent());
        assertEquals(request.getName(), savedRound.get().getName());
        assertEquals(request.getDescription(), savedRound.get().getDescription());
        assertEquals(testMilestone.getId(), savedRound.get().getMilestone().getId());
    }

    @Test
    void testCreateRoundWithNonExistentMilestone() {
        // Given
        CreateRoundRequest request = CreateRoundRequest.builder()
                .name("New Round")
                .description("New Round Description")
                .milestoneId(999L) // Non-existent milestone
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roundService.create(request);
        });
    }

    @Test
    void testUpdateRound() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("Updated Round")
                .description("Updated Round Description")
                .milestoneId(testMilestone1.getId())
                .participantIds(Set.of(testParticipant.getId(), testParticipant1.getId()))
                .build();

        // When
        RoundDto result = roundService.update(testRound.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testRound.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
        assertNotNull(result.getMilestone());
        assertEquals(testMilestone1.getId(), result.getMilestone().getId());
        assertEquals(2, result.getParticipants().size());

        // Verify round was updated in database
        Optional<RoundEntity> updatedRound = roundRepository.findById(testRound.getId());
        assertTrue(updatedRound.isPresent());
        assertEquals(request.getName(), updatedRound.get().getName());
        assertEquals(request.getDescription(), updatedRound.get().getDescription());
        assertEquals(testMilestone1.getId(), updatedRound.get().getMilestone().getId());
        assertEquals(2, updatedRound.get().getParticipants().size());
        assertTrue(updatedRound.get().getParticipants().contains(testParticipant));
        assertTrue(updatedRound.get().getParticipants().contains(testParticipant1));
    }

    @Test
    void testUpdateRoundWithNonExistentMilestone() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("Updated Round")
                .milestoneId(999L) // Non-existent milestone
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roundService.update(testRound.getId(), request);
        });
    }

    @Test
    void testUpdateRoundWithNonExistentParticipant() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("Updated Round")
                .participantIds(Set.of(999L)) // Non-existent participant
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roundService.update(testRound.getId(), request);
        });
    }

    @Test
    void testUpdateRoundWithEmptyParticipants() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("Updated Round")
                .participantIds(Set.of()) // Empty set
                .build();

        // When
        RoundDto result = roundService.update(testRound.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testRound.getId(), result.getId());

        // Verify round participants were cleared
        Optional<RoundEntity> updatedRound = roundRepository.findById(testRound.getId());
        assertTrue(updatedRound.isPresent());
        assertTrue(updatedRound.get().getParticipants().isEmpty());
    }

    @Test
    void testUpdateRoundWithNonExistentRound() {
        // Given
        UpdateRoundRequest request = UpdateRoundRequest.builder()
                .name("Updated Round")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
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
                .description("Round 2 Description")
                .state(State.DRAFT)
                .milestone(testMilestone)
                .build();
        roundRepository.save(round2);

        RoundEntity round3 = RoundEntity.builder()
                .name("Round 3")
                .description("Round 3 Description")
                .state(State.DRAFT)
                .milestone(testMilestone1)
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
    void testRoundWithMultipleParticipants() {
        // Given
        CreateRoundRequest request = CreateRoundRequest.builder()
                .name("Multi Participant Round")
                .description("Round with multiple participants")
                .state(State.DRAFT)
                .milestoneId(testMilestone.getId())
                .build();

        // When
        RoundDto result = roundService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());

        // Add participants to the round
        UpdateRoundRequest updateRequest = UpdateRoundRequest.builder()
                .participantIds(Set.of(testParticipant.getId(), testParticipant1.getId()))
                .build();

        roundService.update(result.getId(), updateRequest);

        // Verify round has multiple participants
        Optional<RoundEntity> savedRound = roundRepository.findById(result.getId());
        assertTrue(savedRound.isPresent());
        assertEquals(2, savedRound.get().getParticipants().size());
        assertTrue(savedRound.get().getParticipants().contains(testParticipant));
        assertTrue(savedRound.get().getParticipants().contains(testParticipant1));
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
}
