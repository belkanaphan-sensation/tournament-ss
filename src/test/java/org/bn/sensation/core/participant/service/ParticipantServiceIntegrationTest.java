package org.bn.sensation.core.participant.service;

import org.bn.sensation.AbstractIntegrationTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ParticipantServiceIntegrationTest extends AbstractIntegrationTest {
/*
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityUserRepository activityUserRepository;

    @MockitoBean
    private CurrentUser currentUser;

    private ActivityEntity testActivity;
    private RoundEntity testRound;
    private RoundEntity testRound1;
    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;
    private MilestoneEntity testMilestone;
    private ParticipantEntity testParticipant;
    private UserEntity testUser;
    private ActivityUserEntity userAssignment;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Clean up existing data
        activityUserRepository.deleteAll();
        participantRepository.deleteAll();
        roundRepository.deleteAll();
        milestoneRepository.deleteAll();
        activityRepository.deleteAll();
        occasionRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        // Create test organization
        testOrganization = OrganizationEntity.builder()
                .name("Test Organization")
                .build();
        testOrganization = organizationRepository.save(testOrganization);

        // Create test occasion
        testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Description")
                .state(OccasionState.PLANNED)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
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

        // Create test milestone
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneOrder(1)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Create test rounds
        testRound = RoundEntity.builder()
                .name("Test Round")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .roundOrder(0)
                .build();
        testRound = roundRepository.save(testRound);

        // Create second milestone for testRound1
        MilestoneEntity testMilestone2 = MilestoneEntity.builder()
                .name("Test Milestone 2")
                .description("Test Milestone 2 Description")
                .state(MilestoneState.DRAFT)
                .activity(testActivity)
                .milestoneOrder(1)
                .build();
        testMilestone2 = milestoneRepository.save(testMilestone2);

        testRound1 = RoundEntity.builder()
                .name("Test Round 1")
                .state(RoundState.OPENED)
                .milestone(testMilestone2)
                .roundOrder(1)
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
                .isRegistered(true)
                .partnerSide(PartnerSide.LEADER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .milestones(new HashSet<>(Set.of(testMilestone, testMilestone2)))
                .build();
        testParticipant = participantRepository.save(testParticipant);

        // Create test user
        testUser = UserEntity.builder()
                .username("testuser")
                .password("password")
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Test")
                        .surname("User")
                        .email("test@example.com")
                        .phoneNumber("+1234567890")
                        .build())
                .organizations(Set.of(testOrganization))
                .build();
        testUser = userRepository.save(testUser);

        // Create user activity assignment
        userAssignment = ActivityUserEntity.builder()
                .user(testUser)
                .activity(testActivity)
                .position(UserActivityPosition.JUDGE)
                .partnerSide(PartnerSide.LEADER)
                .build();
        activityUserRepository.save(userAssignment);

        // Refresh the activity to ensure the assignment is loaded
        testActivity = activityRepository.findById(testActivity.getId()).orElseThrow();

        // Mock CurrentUser
        SecurityUser mockSecurityUser = org.mockito.Mockito.mock(SecurityUser.class);
        when(mockSecurityUser.getId()).thenReturn(testUser.getId());
        when(currentUser.getSecurityUser()).thenReturn(mockSecurityUser);
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
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activityId(testActivity.getId())
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
        assertEquals(request.getPartnerSide(), result.getPartnerSide());

        // Verify participant was saved to database
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        assertEquals(request.getName(), savedParticipant.get().getPerson().getName());
        assertEquals(request.getSurname(), savedParticipant.get().getPerson().getSurname());
        assertEquals(request.getSecondName(), savedParticipant.get().getPerson().getSecondName());
        assertEquals(request.getEmail(), savedParticipant.get().getPerson().getEmail());
        assertEquals(request.getPhoneNumber(), savedParticipant.get().getPerson().getPhoneNumber());
        assertEquals(request.getNumber(), savedParticipant.get().getNumber());
        assertEquals(request.getPartnerSide(), savedParticipant.get().getPartnerSide());
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
                .partnerSide(PartnerSide.FOLLOWER)
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
        assertEquals(request.getPartnerSide(), result.getPartnerSide());
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
        assertEquals(request.getPartnerSide(), updatedParticipant.get().getPartnerSide());
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
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
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
                .isRegistered(true)
                .activity(testActivity)
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
                .isRegistered(true)
                .activity(testActivity)
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
        assertEquals(testParticipant.getPartnerSide(), result.get().getPartnerSide());
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
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            participantService.assignParticipantToRound(999L, testRound.getId());
        });
    }

    @Test
    void testAssignParticipantToRoundWithNonExistentRound() {
        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            participantService.assignParticipantToRound(testParticipant.getId(), 999L);
        });
    }

    @Test
    void testAssignParticipantToRound_AlreadyAssignedToAnotherRoundInSameMilestone_ThrowsException() {
        // Given - создаем второго участника и второй раунд в том же этапе
        final ParticipantEntity anotherParticipant = participantRepository.save(ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Jane")
                        .surname("Smith")
                        .email("jane.smith@example.com")
                        .phoneNumber("+1234567891")
                        .build())
                .number("002")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>())
                .milestones(new HashSet<>(Set.of(testMilestone)))
                .build());

        final RoundEntity testRound2 = roundRepository.save(RoundEntity.builder()
                .name("Test Round 2")
                .state(RoundState.OPENED)
                .milestone(testMilestone)
                .participants(new HashSet<>())
                .roundOrder(1)
                .build());

        // Сначала привязываем участника к первому раунду
        participantService.assignParticipantToRound(anotherParticipant.getId(), testRound.getId());

        // When & Then - пытаемся привязать к второму раунду того же этапа
        assertThrows(IllegalArgumentException.class, () -> {
            participantService.assignParticipantToRound(anotherParticipant.getId(), testRound2.getId());
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
                .activityId(testActivity.getId())
                .isRegistered(false)
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
                .activityId(testActivity.getId())
                .isRegistered(false)
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
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activityId(testActivity.getId())
                .build();

        // When
        ParticipantDto result = participantService.create(request);

        // Then - Verify competition role field is correctly mapped
        assertNotNull(result);
        assertEquals(PartnerSide.FOLLOWER, result.getPartnerSide());

        // Verify in database
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        assertEquals(PartnerSide.FOLLOWER, savedParticipant.get().getPartnerSide());
    }

    @Test
    void testParticipantCompetitionRoleUpdate() {
        // Given - testParticipant has CompetitionRole.LEADER from setUp()
        assertEquals(PartnerSide.LEADER, testParticipant.getPartnerSide());

        UpdateParticipantRequest request = UpdateParticipantRequest.builder()
                .partnerSide(PartnerSide.FOLLOWER)
                .build();

        // When
        ParticipantDto result = participantService.update(testParticipant.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(PartnerSide.FOLLOWER, result.getPartnerSide());

        // Verify in database
        Optional<ParticipantEntity> updatedParticipant = participantRepository.findById(testParticipant.getId());
        assertTrue(updatedParticipant.isPresent());
        assertEquals(PartnerSide.FOLLOWER, updatedParticipant.get().getPartnerSide());
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
                .partnerSide(null) // Null competition role
                .activityId(testActivity.getId())
                .isRegistered(true)
                .build();

        // When
        ParticipantDto result = participantService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getPartnerSide());

        // Verify in database
        Optional<ParticipantEntity> savedParticipant = participantRepository.findById(result.getId());
        assertTrue(savedParticipant.isPresent());
        assertNull(savedParticipant.get().getPartnerSide());
    }

    @Test
    void testFindByRoundId() {
        // Given - testParticipant is already assigned to testRound in setUp()

        // When
        List<ParticipantDto> result = participantService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        ParticipantDto participant = result.get(0);
        assertEquals(testParticipant.getId(), participant.getId());
        assertEquals(testParticipant.getPerson().getName(), participant.getPerson().getName());
        assertEquals(testParticipant.getPerson().getSurname(), participant.getPerson().getSurname());
    }

    @Test
    void testFindByRoundIdWithMultipleParticipants() {
        // Given - Create additional participants for testRound
        ParticipantEntity participant2 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Alice")
                        .surname("Johnson")
                        .email("alice@example.com")
                        .phoneNumber("+2222222222")
                        .build())
                .number("003")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
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
                .isRegistered(true)
                .partnerSide(PartnerSide.LEADER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        participantRepository.save(participant3);

        // When
        List<ParticipantDto> result = participantService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify all participants are from the correct round
        result.forEach(participant -> {
            assertTrue(participant.getRounds().stream()
                    .anyMatch(round -> round.getId().equals(testRound.getId())));
        });
    }

    @Test
    void testFindByRoundIdWithManyParticipants() {
        // Given - Create multiple participants for testRound
        for (int i = 0; i < 5; i++) {
            ParticipantEntity participant = ParticipantEntity.builder()
                    .person(Person.builder()
                            .name("Participant" + i)
                            .surname("Surname" + i)
                            .email("participant" + i + "@example.com")
                            .phoneNumber("+111111111" + i)
                            .build())
                    .number("P-" + String.format("%03d", i))
                    .isRegistered(true)
                    .partnerSide(PartnerSide.FOLLOWER)
                    .activity(testActivity)
                    .rounds(new HashSet<>(Set.of(testRound)))
                    .build();
            participantRepository.save(participant);
        }

        // When
        List<ParticipantDto> result = participantService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(6, result.size()); // 1 from setUp + 5 new

        // Verify all participants are from the correct round
        result.forEach(participant -> {
            assertTrue(participant.getRounds().stream()
                    .anyMatch(round -> round.getId().equals(testRound.getId())));
        });
    }

    @Test
    void testFindByRoundIdWithEmptyResult() {
        // Given - Create a participant for a different round
        ParticipantEntity participantForOtherRound = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Other")
                        .surname("Participant")
                        .email("other@example.com")
                        .phoneNumber("+9999999999")
                        .build())
                .number("OTHER-001")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound1))) // Only in testRound1, not testRound
                .build();
        participantRepository.save(participantForOtherRound);

        // When - Search for participants in testRound (which should only have testParticipant)
        List<ParticipantDto> result = participantService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only testParticipant
        assertEquals(testParticipant.getId(), result.get(0).getId());
    }

    @Test
    void testFindByRoundIdWithNonExistentRound() {
        // When
        List<ParticipantDto> result = participantService.findByRoundId(999L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByRoundIdWithNullRoundId() {
        // When
        List<ParticipantDto> result = participantService.findByRoundId(null);

        // Then - Should return empty result instead of throwing exception
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testParticipantInMultipleRounds() {
        // Given - Create a participant that participates in both rounds
        ParticipantEntity multiRoundParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Multi")
                        .surname("Round")
                        .email("multi@example.com")
                        .phoneNumber("+8888888888")
                        .build())
                .number("MULTI-001")
                .isRegistered(true)
                .partnerSide(PartnerSide.LEADER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound, testRound1))) // In both rounds
                .build();
        participantRepository.save(multiRoundParticipant);

        // When - Search for participants in testRound
        List<ParticipantDto> testRoundResult = participantService.findByRoundId(testRound.getId());

        // When - Search for participants in testRound1
        List<ParticipantDto> testRound1Result = participantService.findByRoundId(testRound1.getId());

        // Then
        assertNotNull(testRoundResult);
        assertEquals(2, testRoundResult.size()); // testParticipant + multiRoundParticipant
        assertTrue(testRoundResult.stream()
                .anyMatch(p -> p.getId().equals(multiRoundParticipant.getId())));

        assertNotNull(testRound1Result);
        assertEquals(1, testRound1Result.size()); // Only multiRoundParticipant
        assertEquals(multiRoundParticipant.getId(), testRound1Result.get(0).getId());
    }

    @Test
    void testFindByRoundIdWithVeryManyParticipants() {
        // Given - Create multiple participants
        for (int i = 0; i < 7; i++) {
            ParticipantEntity participant = ParticipantEntity.builder()
                    .person(Person.builder()
                            .name("ManyTest" + i)
                            .surname("Surname" + i)
                            .email("manytest" + i + "@example.com")
                            .phoneNumber("+222222222" + i)
                            .build())
                    .number("MT-" + String.format("%03d", i))
                    .isRegistered(true)
                    .partnerSide(PartnerSide.FOLLOWER)
                    .rounds(new HashSet<>(Set.of(testRound)))
                    .build();
            participantRepository.save(participant);
        }

        // When
        List<ParticipantDto> result = participantService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(8, result.size()); // 1 from setUp + 7 new

        // Verify all participants are from the correct round
        result.forEach(participant -> {
            assertTrue(participant.getRounds().stream()
                    .anyMatch(round -> round.getId().equals(testRound.getId())));
        });
    }

    @Test
    void testFindByRoundIdEnrichesDtoWithActivityAndMilestones() {
        // Given - testParticipant is already assigned to testRound in setUp()
        // testRound belongs to testMilestone, which belongs to testActivity

        // When
        List<ParticipantDto> result = participantService.findByRoundId(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        ParticipantDto participant = result.get(0);

        // Проверяем, что rounds загружены
        assertNotNull(participant.getRounds());
        assertEquals(1, participant.getRounds().size());
        assertTrue(participant.getRounds().stream()
                .anyMatch(round -> round.getId().equals(testRound.getId())));

        // Проверяем, что activity загружена
        assertNotNull(participant.getActivity());
        assertEquals(testActivity.getId(), participant.getActivity().getId());
        assertEquals(testActivity.getName(), participant.getActivity().getValue());

        // Проверяем, что milestones загружены
        assertNotNull(participant.getMilestones());
        assertEquals(2, participant.getMilestones().size());
        assertTrue(participant.getMilestones().stream()
                .anyMatch(milestone -> milestone.getId().equals(testMilestone.getId())));
        assertTrue(participant.getMilestones().stream()
                .anyMatch(milestone -> milestone.getValue().equals(testMilestone.getName())));
    }

    @Test
    void testGetByRoundByRoundIdForCurrentUser_Success() {
        // Given - testParticipant is already assigned to testRound and has PartnerSide.LEADER
        // testUser is assigned to testActivity with PartnerSide.LEADER
        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);
        testRound.getContestants().add(testParticipant);
        roundRepository.save(testRound);
        testParticipant.getRounds().add(testRound);
        participantRepository.save(testParticipant);
        // When
        List<ParticipantDto> result = participantService.getByRoundByRoundIdForCurrentUser(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        var participant = result.get(0);
        assertEquals(testParticipant.getId(), participant.getId());
        assertEquals(testParticipant.getNumber(), participant.getNumber());
        assertEquals(testParticipant.getPerson().getName(), participant.getPerson().getName());
        assertEquals(testParticipant.getPerson().getSurname(), participant.getPerson().getSurname());
    }

    @Test
    void testGetByRoundByRoundIdForCurrentUser_WithPartnerSideFiltering() {
        // Given - Create another participant with FOLLOWER partner side
        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);
        ParticipantEntity followerParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Follower")
                        .surname("Participant")
                        .email("follower@example.com")
                        .phoneNumber("+9876543210")
                        .build())
                .number("002")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        followerParticipant = participantRepository.save(followerParticipant);
        testRound.getContestants().add(followerParticipant);
        testRound.getContestants().add(testParticipant);
        roundRepository.save(testRound);

        // testUser has PartnerSide.LEADER, so only LEADER participants should be returned

        // When
        List<ParticipantDto> result = participantService.getByRoundByRoundIdForCurrentUser(testRound.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only LEADER participant

        var participant = result.get(0);
        assertEquals(testParticipant.getId(), participant.getId());
        assertEquals(PartnerSide.LEADER, testParticipant.getPartnerSide());
    }

    @Test
    void testGetByRoundByRoundIdForCurrentUser_WithoutPartnerSide() {
        // Given - Update user assignment to have null partnerSide
        ActivityUserEntity userAssignment = activityUserRepository
                .findByUserIdAndActivityId(testUser.getId(), testActivity.getId()).orElseThrow();
        userAssignment.setPartnerSide(null);
        activityUserRepository.save(userAssignment);
        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);

        // Create participants with different partner sides
        ParticipantEntity followerParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Follower")
                        .surname("Participant")
                        .email("follower@example.com")
                        .phoneNumber("+9876543210")
                        .build())
                .number("002")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        followerParticipant = participantRepository.save(followerParticipant);
        testRound.getContestants().add(testParticipant);
        testRound.getContestants().add(followerParticipant);
        roundRepository.save(testRound);

        // When
        List<ParticipantDto> result = participantService.getByRoundByRoundIdForCurrentUser(testRound.getId());

        // Then - Should return all participants since user has no partnerSide
        assertNotNull(result);
        assertEquals(2, result.size()); // Both LEADER and FOLLOWER
    }

    @Test
    void testGetByRoundByRoundIdForCurrentUser_RoundNotFound() {
        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            participantService.getByRoundByRoundIdForCurrentUser(999L);
        });
    }

    @Test
    void testGetByRoundByRoundIdForCurrentUser_UserNotAssignedToActivity() {
        // Given - Create a user without activity assignment
        UserEntity unassignedUser = UserEntity.builder()
                .username("unassigned")
                .password("password")
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Unassigned")
                        .surname("User")
                        .email("unassigned@example.com")
                        .phoneNumber("+1111111111")
                        .build())
                .organizations(Set.of(testOrganization))
                .build();
        unassignedUser = userRepository.save(unassignedUser);

        // Mock CurrentUser to return unassigned user
        SecurityUser mockSecurityUser = org.mockito.Mockito.mock(SecurityUser.class);
        when(mockSecurityUser.getId()).thenReturn(unassignedUser.getId());
        when(currentUser.getSecurityUser()).thenReturn(mockSecurityUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            participantService.getByRoundByRoundIdForCurrentUser(testRound.getId());
        });
    }

    @Test
    void testGetByRoundByRoundIdForCurrentUser_NullRoundId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            participantService.getByRoundByRoundIdForCurrentUser(null);
        });
    }

    @Test
    void testGetByRoundByRoundIdForCurrentUser_MultipleParticipants() {
        // Given - Create multiple participants with different partner sides
        ParticipantEntity followerParticipant1 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Follower1")
                        .surname("Participant")
                        .email("follower1@example.com")
                        .phoneNumber("+1111111111")
                        .build())
                .number("002")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        followerParticipant1 = participantRepository.save(followerParticipant1);

        ParticipantEntity followerParticipant2 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Follower2")
                        .surname("Participant")
                        .email("follower2@example.com")
                        .phoneNumber("+2222222222")
                        .build())
                .number("003")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        followerParticipant2 = participantRepository.save(followerParticipant2);

        testRound.getContestants().add(followerParticipant1);
        testRound.getContestants().add(followerParticipant2);
        roundRepository.save(testRound);

        // Update user assignment to FOLLOWER
        ActivityUserEntity userAssignment = activityUserRepository
                .findByUserIdAndActivityId(testUser.getId(), testActivity.getId()).orElseThrow();
        userAssignment.setPartnerSide(PartnerSide.FOLLOWER);
        activityUserRepository.save(userAssignment);

        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);

        // When
        List<ParticipantDto> result = participantService.getByRoundByRoundIdForCurrentUser(testRound.getId());

        // Then - Should return only FOLLOWER participants
        assertNotNull(result);
        assertEquals(2, result.size()); // Only FOLLOWER participants

        result.forEach(participant -> {
            // Verify all returned participants are FOLLOWER
            ParticipantEntity entity = participantRepository.findById(participant.getId()).orElseThrow();
            assertEquals(PartnerSide.FOLLOWER, entity.getPartnerSide());
        });
    }

    @Test
    void testGetByRoundByMilestoneIdForCurrentUser_Success() {
        // Given - testUser is assigned to testActivity with PartnerSide.LEADER
        // testMilestone has testRound and testRound1 with participants
        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);

        // Add participants to rounds
        testRound.getContestants().add(testParticipant);
        testRound1.getContestants().add(testParticipant);
        roundRepository.save(testRound);
        roundRepository.save(testRound1);

        // Add rounds to milestone
        testMilestone.getRounds().add(testRound);
        testMilestone.getRounds().add(testRound1);
        milestoneRepository.save(testMilestone);

        // When
        List<RoundParticipantsDto> result = participantService.getByRoundByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // testRound and testRound1

        // Verify rounds are sorted by ID
        assertTrue(result.get(0).getRound().getId() <= result.get(1).getRound().getId());

        // Verify each round has participants
        result.forEach(roundDto -> {
            assertNotNull(roundDto.getRound());
            assertNotNull(roundDto.getParticipants());
            assertEquals(1, roundDto.getParticipants().size());
            assertEquals(testParticipant.getId(), roundDto.getParticipants().get(0).getId());
        });
    }

    @Test
    void testGetByRoundByMilestoneIdForCurrentUser_WithPartnerSideFiltering() {
        // Given - Create participants with different partner sides
        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);

        ParticipantEntity followerParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Jane")
                        .surname("Doe")
                        .email("jane.doe@example.com")
                        .phoneNumber("+1234567891")
                        .build())
                .number("002")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        followerParticipant = participantRepository.save(followerParticipant);

        // Add participants to rounds
        testRound.getContestants().add(testParticipant); // LEADER
        testRound.getContestants().add(followerParticipant); // FOLLOWER
        roundRepository.save(testRound);

        // Add round to milestone
        testMilestone.getRounds().add(testRound);
        milestoneRepository.save(testMilestone);

        // testUser has PartnerSide.LEADER, so only LEADER participants should be returned
        // When
        List<RoundParticipantsDto> result = participantService.getByRoundByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only testRound

        var roundDto = result.get(0);
        assertNotNull(roundDto.getParticipants());
        assertEquals(1, roundDto.getParticipants().size()); // Only LEADER participant

        var participant = roundDto.getParticipants().get(0);
        assertEquals(testParticipant.getId(), participant.getId());
    }

    @Test
    void testGetByRoundByMilestoneIdForCurrentUser_WithoutPartnerSide() {
        // Given - Remove partnerSide from user assignment
        userAssignment.setPartnerSide(null);
        activityUserRepository.save(userAssignment);
        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);

        // Create participants with different partner sides
        ParticipantEntity followerParticipant = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Jane")
                        .surname("Doe")
                        .email("jane.doe@example.com")
                        .phoneNumber("+1234567891")
                        .build())
                .number("002")
                .isRegistered(true)
                .partnerSide(PartnerSide.FOLLOWER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound)))
                .build();
        followerParticipant = participantRepository.save(followerParticipant);

        // Add participants to rounds
        testRound.getContestants().add(testParticipant); // LEADER
        testRound.getContestants().add(followerParticipant); // FOLLOWER
        roundRepository.save(testRound);

        // Add round to milestone
        testMilestone.getRounds().add(testRound);
        milestoneRepository.save(testMilestone);

        // When - user has no partnerSide, so all participants should be returned
        List<RoundParticipantsDto> result = participantService.getByRoundByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        var roundDto = result.get(0);
        assertNotNull(roundDto.getParticipants());
        assertEquals(2, roundDto.getParticipants().size()); // Both LEADER and FOLLOWER participants
    }

    @Test
    void testGetByRoundByMilestoneIdForCurrentUser_MultipleRounds() {
        // Given - Create additional participants for different rounds
        testActivity.getActivityUsers().add(userAssignment);
        activityRepository.save(testActivity);

        ParticipantEntity participant2 = ParticipantEntity.builder()
                .person(Person.builder()
                        .name("Alice")
                        .surname("Smith")
                        .email("alice.smith@example.com")
                        .phoneNumber("+1234567892")
                        .build())
                .number("003")
                .isRegistered(true)
                .partnerSide(PartnerSide.LEADER)
                .activity(testActivity)
                .rounds(new HashSet<>(Set.of(testRound1)))
                .build();
        participant2 = participantRepository.save(participant2);

        // Add participants to different rounds
        testRound.getContestants().add(testParticipant);
        testRound1.getContestants().add(participant2);
        roundRepository.save(testRound);
        roundRepository.save(testRound1);

        // Add rounds to milestone
        testMilestone.getRounds().add(testRound);
        testMilestone.getRounds().add(testRound1);
        milestoneRepository.save(testMilestone);

        // When
        List<RoundParticipantsDto> result = participantService.getByRoundByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Both rounds

        // Verify each round has its participants
        result.forEach(roundDto -> {
            assertNotNull(roundDto.getRound());
            assertNotNull(roundDto.getParticipants());
            assertEquals(1, roundDto.getParticipants().size());
        });
    }

    @Test
    void testGetByRoundByMilestoneIdForCurrentUser_MilestoneNotFound() {
        // Given - Non-existent milestone ID
        Long nonExistentMilestoneId = 999L;

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () ->
            participantService.getByRoundByMilestoneIdForCurrentUser(nonExistentMilestoneId));
    }

    @Test
    void testGetByRoundByMilestoneIdForCurrentUser_UserNotAssignedToActivity() {
        // Given - Create another user not assigned to the activity
        UserEntity unassignedUser = UserEntity.builder()
                .username("unassigneduser")
                .password("password")
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Unassigned")
                        .surname("User")
                        .email("unassigned@example.com")
                        .phoneNumber("+1234567893")
                        .build())
                .organizations(Set.of(testOrganization))
                .build();
        unassignedUser = userRepository.save(unassignedUser);

        // Mock CurrentUser to return unassigned user
        SecurityUser mockSecurityUser = org.mockito.Mockito.mock(SecurityUser.class);
        when(mockSecurityUser.getId()).thenReturn(unassignedUser.getId());
        when(currentUser.getSecurityUser()).thenReturn(mockSecurityUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            participantService.getByRoundByMilestoneIdForCurrentUser(testMilestone.getId()));
    }

    @Test
    void testGetByRoundByMilestoneIdForCurrentUser_NullMilestoneId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            participantService.getByRoundByMilestoneIdForCurrentUser(null));
    }*/
}
