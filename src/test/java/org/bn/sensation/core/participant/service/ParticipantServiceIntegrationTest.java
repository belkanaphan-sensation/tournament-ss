package org.bn.sensation.core.participant.service;

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
import org.bn.sensation.core.common.entity.CompetitionRole;
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
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
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
class ParticipantServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private ActivityEntity testActivity;
    private RoundEntity testRound;
    private RoundEntity testRound1;
    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private MilestoneEntity testMilestone;
    private ParticipantEntity testParticipant;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Clean up existing data
        participantRepository.deleteAll();
        roundRepository.deleteAll();
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
                .state(State.DRAFT)
                .startDate(LocalDate.now())
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

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .state(State.DRAFT)
                .activity(testActivity)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test rounds
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(State.DRAFT)
                .description("Test Round Description")
                .milestone(testMilestone)
                .build();
        testRound = roundRepository.save(testRound);

        testRound1 = RoundEntity.builder()
                .name("Test Round 1")
                .state(State.DRAFT)
                .description("Test Round 1 Description")
                .milestone(testMilestone)
                .build();
        testRound1 = roundRepository.save(testRound1);

        // Create test participant
        testParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("John")
                        .surname("Doe")
                        .email("john.doe@example.com")
                        .phoneNumber("+1234567890")
                        .build())
                .number("001")
                .competitionRole(CompetitionRole.LEADER)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        testParticipant = participantRepository.save(testParticipant);
    }

    @Test
    void testCreateParticipant() {
        // Given
        CreateParticipantRequest request = CreateParticipantRequest.builder()
                .name("Jane")
                .surname("Smith")
                .secondName("Middle")
                .email("jane.smith@example.com")
                .phoneNumber("+0987654321")
                .number("002")
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When
        ParticipantDto result = participantService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getName(), result.getPerson().getName());
        assertEquals(request.getSurname(), result.getPerson().getSurname());
        assertEquals(request.getSecondName(), result.getPerson().getSecondName());
        assertEquals(request.getEmail(), result.getPerson().getEmail());
        assertEquals(request.getPhoneNumber(), result.getPerson().getPhoneNumber());
        assertEquals(request.getNumber(), result.getNumber());
        assertEquals(request.getCompetitionRole(), result.getCompetitionRole());

        // Verify participant was saved to database
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        assertEquals(request.getName(), savedParticipant.get().getPerson().getName());
        assertEquals(request.getSurname(), savedParticipant.get().getPerson().getSurname());
        assertEquals(request.getSecondName(), savedParticipant.get().getPerson().getSecondName());
        assertEquals(request.getEmail(), savedParticipant.get().getPerson().getEmail());
        assertEquals(request.getPhoneNumber(), savedParticipant.get().getPerson().getPhoneNumber());
        assertEquals(request.getNumber(), savedParticipant.get().getNumber());
        assertEquals(request.getCompetitionRole(), savedParticipant.get().getCompetitionRole());
    }

    @Test
    void testUpdateParticipant() {
        // Given
        UpdateParticipantRequest request = UpdateParticipantRequest.builder()
                .name("Updated")
                .surname("Participant")
                .secondName("New")
                .email("updated@example.com")
                .phoneNumber("+1111111111")
                .number("999")
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When
        ParticipantDto result = participantService.update(testParticipant.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testParticipant.getId(), result.getId());
        assertEquals(request.getName(), result.getPerson().getName());
        assertEquals(request.getSurname(), result.getPerson().getSurname());
        assertEquals(request.getSecondName(), result.getPerson().getSecondName());
        assertEquals(request.getEmail(), result.getPerson().getEmail());
        assertEquals(request.getPhoneNumber(), result.getPerson().getPhoneNumber());
        assertEquals(request.getNumber(), result.getNumber());
        assertEquals(request.getCompetitionRole(), result.getCompetitionRole());
        assertEquals(1, result.getRounds().size());

        // Verify participant was updated in database
        Optional<ParticipantEntity> updatedParticipant = participantRepository.findById(testParticipant.getId());
        assertTrue(updatedParticipant.isPresent());
        assertEquals(request.getName(), updatedParticipant.get().getPerson().getName());
        assertEquals(request.getSurname(), updatedParticipant.get().getPerson().getSurname());
        assertEquals(request.getSecondName(), updatedParticipant.get().getPerson().getSecondName());
        assertEquals(request.getEmail(), updatedParticipant.get().getPerson().getEmail());
        assertEquals(request.getPhoneNumber(), updatedParticipant.get().getPerson().getPhoneNumber());
        assertEquals(request.getNumber(), updatedParticipant.get().getNumber());
        assertEquals(request.getCompetitionRole(), updatedParticipant.get().getCompetitionRole());
        assertEquals(1, updatedParticipant.get().getRounds().size());
        assertTrue(updatedParticipant.get().getRounds().contains(testRound));
    }

    @Test
    void testUpdateParticipantWithNonExistentParticipant() {
        // Given
        UpdateParticipantRequest request = UpdateParticipantRequest.builder()
                .name("Updated")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            participantService.update(999L, request);
        });
    }

    @Test
    void testFindAllParticipants() {
        // Create additional participants
        ParticipantEntity participant2 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Alice")
                        .surname("Johnson")
                        .email("alice@example.com")
                        .phoneNumber("+2222222222")
                        .build())
                .number("003")
                .build();
        participantRepository.save(participant2);

        ParticipantEntity participant3 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Bob")
                        .surname("Wilson")
                        .email("bob@example.com")
                        .phoneNumber("+3333333333")
                        .build())
                .number("004")
                .build();
        participantRepository.save(participant3);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ParticipantDto> result = participantService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindParticipantById() {
        // When
        Optional<ParticipantDto> result = participantService.findById(testParticipant.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testParticipant.getId(), result.get().getId());
        assertEquals(testParticipant.getPerson().getName(), result.get().getPerson().getName());
        assertEquals(testParticipant.getPerson().getSurname(), result.get().getPerson().getSurname());
        assertEquals(testParticipant.getPerson().getEmail(), result.get().getPerson().getEmail());
        assertEquals(testParticipant.getPerson().getPhoneNumber(), result.get().getPerson().getPhoneNumber());
        assertEquals(testParticipant.getNumber(), result.get().getNumber());
        assertEquals(testParticipant.getCompetitionRole(), result.get().getCompetitionRole());
    }

    @Test
    void testFindParticipantByIdNotFound() {
        // When
        Optional<ParticipantDto> result = participantService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteParticipant() {
        // Given
        Long participantId = testParticipant.getId();

        // When
        participantService.deleteById(participantId);

        // Then
        assertFalse(participantRepository.existsById(participantId));
    }

    @Test
    void testDeleteParticipantNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            participantService.deleteById(999L);
        });
    }

    @Test
    void testAssignParticipantToRound() {
        // When
        ParticipantDto result = participantService.assignParticipantToRound(testParticipant.getId(), testRound1.getId());

        // Then
        assertNotNull(result);
        assertEquals(testParticipant.getId(), result.getId());

        // Verify participant was assigned to round
        Optional<ParticipantEntity> updatedParticipant = participantRepository.findById(testParticipant.getId());
        assertTrue(updatedParticipant.isPresent());
        assertEquals(2, updatedParticipant.get().getRounds().size());
        assertTrue(updatedParticipant.get().getRounds().contains(testRound));
        assertTrue(updatedParticipant.get().getRounds().contains(testRound1));
    }

    @Test
    void testAssignParticipantToRoundWithNonExistentParticipant() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            participantService.assignParticipantToRound(999L, testRound.getId());
        });
    }

    @Test
    void testAssignParticipantToRoundWithNonExistentRound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            participantService.assignParticipantToRound(testParticipant.getId(), 999L);
        });
    }

    @Test
    void testParticipantWithNullPersonFields() {
        // Given
        CreateParticipantRequest request = CreateParticipantRequest.builder()
                .name("Test")
                .surname("User")
                .secondName(null) // Null second name
                .email(null) // Null email
                .phoneNumber(null) // Null phone
                .build();

        // When
        ParticipantDto result = participantService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getName(), result.getPerson().getName());
        assertEquals(request.getSurname(), result.getPerson().getSurname());
        assertNull(result.getPerson().getSecondName());
        assertNull(result.getPerson().getEmail());
        assertNull(result.getPerson().getPhoneNumber());

        // Verify participant was saved with null fields
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        assertEquals(request.getName(), savedParticipant.get().getPerson().getName());
        assertEquals(request.getSurname(), savedParticipant.get().getPerson().getSurname());
        assertNull(savedParticipant.get().getPerson().getSecondName());
        assertNull(savedParticipant.get().getPerson().getEmail());
        assertNull(savedParticipant.get().getPerson().getPhoneNumber());
    }

    @Test
    void testParticipantPersonMapping() {
        // Given
        CreateParticipantRequest request = CreateParticipantRequest.builder()
                .name("Mapping")
                .surname("Test")
                .secondName("Validation")
                .email("mapping@test.com")
                .phoneNumber("+5555555555")
                .build();

        // When
        ParticipantDto result = participantService.create(request);

        // Then - Verify all Person fields are correctly mapped
        assertNotNull(result.getPerson());
        assertEquals("Mapping", result.getPerson().getName());
        assertEquals("Test", result.getPerson().getSurname());
        assertEquals("Validation", result.getPerson().getSecondName());
        assertEquals("mapping@test.com", result.getPerson().getEmail());
        assertEquals("+5555555555", result.getPerson().getPhoneNumber());

        // Verify in database
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        Person savedPerson = savedParticipant.get().getPerson();
        assertNotNull(savedPerson);
        assertEquals("Mapping", savedPerson.getName());
        assertEquals("Test", savedPerson.getSurname());
        assertEquals("Validation", savedPerson.getSecondName());
        assertEquals("mapping@test.com", savedPerson.getEmail());
        assertEquals("+5555555555", savedPerson.getPhoneNumber());
    }

    @Test
    void testParticipantCascadeDelete() {
        // Given
        Long participantId = testParticipant.getId();

        // When
        participantService.deleteById(participantId);

        // Then
        assertFalse(participantRepository.existsById(participantId));

        // Verify related entities still exist (no cascade delete)
        assertTrue(roundRepository.existsById(testRound.getId()));
        assertTrue(activityRepository.existsById(testActivity.getId()));
    }

    @Test
    void testParticipantCompetitionRoleMapping() {
        // Given
        CreateParticipantRequest request = CreateParticipantRequest.builder()
                .name("Role")
                .surname("Test")
                .email("role@test.com")
                .phoneNumber("+6666666666")
                .number("R-001")
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When
        ParticipantDto result = participantService.create(request);

        // Then - Verify competition role field is correctly mapped
        assertNotNull(result);
        assertEquals(CompetitionRole.FOLLOWER, result.getCompetitionRole());

        // Verify in database
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        assertEquals(CompetitionRole.FOLLOWER, savedParticipant.get().getCompetitionRole());
    }

    @Test
    void testParticipantCompetitionRoleUpdate() {
        // Given - testParticipant has CompetitionRole.LEADER from setUp()
        assertEquals(CompetitionRole.LEADER, testParticipant.getCompetitionRole());

        UpdateParticipantRequest request = UpdateParticipantRequest.builder()
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When
        ParticipantDto result = participantService.update(testParticipant.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(CompetitionRole.FOLLOWER, result.getCompetitionRole());

        // Verify in database
        Optional<ParticipantEntity> updatedParticipant = participantRepository.findById(testParticipant.getId());
        assertTrue(updatedParticipant.isPresent());
        assertEquals(CompetitionRole.FOLLOWER, updatedParticipant.get().getCompetitionRole());
    }

    @Test
    void testParticipantWithNullCompetitionRole() {
        // Given
        CreateParticipantRequest request = CreateParticipantRequest.builder()
                .name("Null")
                .surname("Role")
                .email("null@test.com")
                .phoneNumber("+7777777777")
                .number("N-001")
                .competitionRole(null) // Null competition role
                .build();

        // When
        ParticipantDto result = participantService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getCompetitionRole());

        // Verify in database
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        assertNull(savedParticipant.get().getCompetitionRole());
    }
}
