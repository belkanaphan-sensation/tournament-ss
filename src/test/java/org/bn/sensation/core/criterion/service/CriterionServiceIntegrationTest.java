package org.bn.sensation.core.criterion.service;

import org.bn.sensation.AbstractIntegrationTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CriterionServiceIntegrationTest extends AbstractIntegrationTest {
/*
    @Autowired
    private CriterionService criterionService;

    @Autowired
    private CriterionRepository criterionRepository;

    @Autowired
    private MilestoneCriterionRepository milestoneCriterionRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private CriterionEntity testCriteria;
    private MilestoneEntity testMilestone;
    private MilestoneRuleEntity testMilestoneRule;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            milestoneCriterionRepository.deleteAll();
            milestoneRuleRepository.deleteAll();
            criterionRepository.deleteAll();
            milestoneRepository.deleteAll();
            activityRepository.deleteAll();
            occasionRepository.deleteAll();
            organizationRepository.deleteAll();
            return null;
        });

        // Создание тестовых данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            // Создаем тестовую организацию
            OrganizationEntity testOrganization = OrganizationEntity.builder()
                    .name("Test Organization")
                    .description("Test Description")
                    .build();
            testOrganization = organizationRepository.save(testOrganization);

            // Создаем тестовое мероприятие
            OccasionEntity testOccasion = OccasionEntity.builder()
                    .name("Test Occasion")
                    .description("Test Description")
                    .state(OccasionState.PLANNED)
                    .organization(testOrganization)
                    .build();
            testOccasion = occasionRepository.save(testOccasion);

            // Создаем тестовую активность
            ActivityEntity testActivity = ActivityEntity.builder()
                    .name("Test Activity")
                    .description("Test Activity Description")
                    .state(ActivityState.PLANNED)
                    .occasion(testOccasion)
                    .build();
            testActivity = activityRepository.save(testActivity);

            // Создаем тестовый этап
            testMilestone = MilestoneEntity.builder()
                    .name("Test Milestone")
                    .state(MilestoneState.DRAFT)
                    .activity(testActivity)
                    .milestoneOrder(1)
                    .build();
            testMilestone = milestoneRepository.save(testMilestone);

            // Создаем тестовое правило этапа
            testMilestoneRule = MilestoneRuleEntity.builder()
                    .assessmentMode(AssessmentMode.SCORE)
                    .participantLimit(10)
                    .roundParticipantLimit(10)
                    .milestone(testMilestone)
                    .build();
            testMilestoneRule = milestoneRuleRepository.save(testMilestoneRule);

            // Связываем этап с правилом
            testMilestone.setMilestoneRule(testMilestoneRule);
            milestoneRepository.save(testMilestone);

            // Создаем тестовый критерий
            testCriteria = CriterionEntity.builder()
                    .name("Техника")
                    .build();
            testCriteria = criterionRepository.save(testCriteria);

            return null;
        });
    }

    @Test
    void testCreateCriteria() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("Ведение")
                .build();

        // When
        CriterionDto result = criterionService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());

        // Verify criteria was saved to database
        Optional<CriterionEntity> savedCriteria = criterionRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals(request.getName(), savedCriteria.get().getName());
    }

    @Test
    void testCreateCriteriaWithExistingName() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("Техника") // То же название, что и у существующего критерия
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criterionService.create(request);
        });
    }

    @Test
    void testUpdateCriteria() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("Обновленная техника")
                .build();

        // When
        CriterionDto result = criterionService.update(testCriteria.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testCriteria.getId(), result.getId());
        assertEquals(request.getName(), result.getName());

        // Verify criteria was updated in database
        Optional<CriterionEntity> updatedCriteria = criterionRepository.findById(testCriteria.getId());
        assertTrue(updatedCriteria.isPresent());
        assertEquals(request.getName(), updatedCriteria.get().getName());
    }

    @Test
    void testUpdateCriteriaWithExistingName() {
        // Given
        // Сначала создаем другой критерий
        CriterionEntity anotherCriteria = CriterionEntity.builder()
                .name("Другой критерий")
                .build();
        criterionRepository.save(anotherCriteria);

        CriterionRequest request = CriterionRequest.builder()
                .name("Другой критерий") // То же название, что и у другого критерия
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criterionService.update(testCriteria.getId(), request);
        });
    }

    @Test
    void testUpdateCriteriaWithSameName() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("Техника") // То же название, что и у текущего критерия
                .build();

        // When
        CriterionDto result = criterionService.update(testCriteria.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testCriteria.getId(), result.getId());
        assertEquals("Техника", result.getName());
    }

    @Test
    void testUpdateCriteriaWithNonExistentCriteria() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("Updated")
                .build();

        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            criterionService.update(999L, request);
        });
    }

    @Test
    void testFindAllCriteria() {
        // Create additional criteria
        CriterionEntity criteria2 = CriterionEntity.builder()
                .name("Стилистика")
                .build();
        criterionRepository.save(criteria2);

        CriterionEntity criteria3 = CriterionEntity.builder()
                .name("Музыкальность")
                .build();
        criterionRepository.save(criteria3);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CriterionDto> result = criterionService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindCriteriaById() {
        // When
        Optional<CriterionDto> result = criterionService.findById(testCriteria.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCriteria.getId(), result.get().getId());
        assertEquals(testCriteria.getName(), result.get().getName());
    }

    @Test
    void testFindCriteriaByIdNotFound() {
        // When
        Optional<CriterionDto> result = criterionService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteCriteria() {
        // Given
        Long criteriaId = testCriteria.getId();

        // When
        criterionService.deleteById(criteriaId);

        // Then
        assertFalse(criterionRepository.existsById(criteriaId));
    }

    @Test
    void testDeleteCriteriaNotFound() {
        // When & Then
        assertThrows(JpaObjectRetrievalFailureException.class, () -> {
            criterionService.deleteById(999L);
        });
    }

    @Test
    void testDeleteCriteriaWithMilestoneAssignments() {
        // Given
        // Создаем назначение критерия правилу этапа
        MilestoneCriterionEntity assignment = MilestoneCriterionEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criterion(testCriteria)
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();
        milestoneCriterionRepository.save(assignment);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            criterionService.deleteById(testCriteria.getId());
        });

        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("Нельзя удалить критерий, который используется в этапах"));
    }

    @Test
    void testDeleteCriteriaWithoutAssignments() {
        // Given
        // Создаем критерий без назначений
        CriterionEntity criteriaWithoutAssignments = CriterionEntity.builder()
                .name("Неиспользуемый критерий")
                .build();
        criterionRepository.save(criteriaWithoutAssignments);

        // When
        criterionService.deleteById(criteriaWithoutAssignments.getId());

        // Then
        assertFalse(criterionRepository.existsById(criteriaWithoutAssignments.getId()));
    }

    @Test
    void testCriteriaNameMapping() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("Маппинг тест")
                .build();

        // When
        CriterionDto result = criterionService.create(request);

        // Then - Verify name field is correctly mapped
        assertNotNull(result);
        assertEquals("Маппинг тест", result.getName());

        // Verify in database
        Optional<CriterionEntity> savedCriteria = criterionRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals("Маппинг тест", savedCriteria.get().getName());
    }

    @Test
    void testCriteriaCascadeDelete() {
        // Given
        Long criteriaId = testCriteria.getId();

        // When
        criterionService.deleteById(criteriaId);

        // Then
        assertFalse(criterionRepository.existsById(criteriaId));

        // Verify related entities still exist (no cascade delete)
        assertTrue(milestoneRepository.existsById(testMilestone.getId()));
    }

    @Test
    void testFindCriteriaByName() {
        // Given
        String criteriaName = "Техника";

        // When
        Optional<CriterionEntity> result = criterionRepository.findByName(criteriaName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCriteria.getId(), result.get().getId());
        assertEquals(criteriaName, result.get().getName());
    }

    @Test
    void testFindCriteriaByNameNotFound() {
        // Given
        String criteriaName = "Несуществующий критерий";

        // When
        Optional<CriterionEntity> result = criterionRepository.findByName(criteriaName);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testCriteriaWithSpecialCharacters() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("Критерий с символами: !@#$%^&*()")
                .build();

        // When
        CriterionDto result = criterionService.create(request);

        // Then
        assertNotNull(result);
        assertEquals("Критерий с символами: !@#$%^&*()", result.getName());

        // Verify in database
        Optional<CriterionEntity> savedCriteria = criterionRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals("Критерий с символами: !@#$%^&*()", savedCriteria.get().getName());
    }

    @Test
    void testCriteriaWithLongName() {
        // Given
        String longName = "Очень длинное название критерия оценки, которое содержит много символов и должно корректно обрабатываться системой";
        CriterionRequest request = CriterionRequest.builder()
                .name(longName)
                .build();

        // When
        CriterionDto result = criterionService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(longName, result.getName());

        // Verify in database
        Optional<CriterionEntity> savedCriteria = criterionRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals(longName, savedCriteria.get().getName());
    }

    @Test
    void testUpdateCriteriaWithEmptyName() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name("") // Пустое название
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criterionService.update(testCriteria.getId(), request);
        });
    }

    @Test
    void testUpdateCriteriaWithNullName() {
        // Given
        CriterionRequest request = CriterionRequest.builder()
                .name(null) // Null название
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criterionService.update(testCriteria.getId(), request);
        });
    }*/
}
