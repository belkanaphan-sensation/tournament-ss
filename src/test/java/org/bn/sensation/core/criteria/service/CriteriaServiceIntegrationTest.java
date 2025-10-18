package org.bn.sensation.core.criteria.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.milestonecriteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.milestonecriteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.criteria.service.dto.CriteriaDto;
import org.bn.sensation.core.criteria.service.dto.CriteriaRequest;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class CriteriaServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CriteriaService criteriaService;

    @Autowired
    private CriteriaRepository criteriaRepository;

    @Autowired
    private MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

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

    private CriteriaEntity testCriteria;
    private MilestoneEntity testMilestone;
    private MilestoneRuleEntity testMilestoneRule;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            milestoneCriteriaAssignmentRepository.deleteAll();
            milestoneRuleRepository.deleteAll();
            criteriaRepository.deleteAll();
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
                    .state(OccasionState.DRAFT)
                    .organization(testOrganization)
                    .build();
            testOccasion = occasionRepository.save(testOccasion);

            // Создаем тестовую активность
            ActivityEntity testActivity = ActivityEntity.builder()
                    .name("Test Activity")
                    .description("Test Activity Description")
                    .state(ActivityState.DRAFT)
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
            testCriteria = CriteriaEntity.builder()
                    .name("Техника")
                    .build();
            testCriteria = criteriaRepository.save(testCriteria);

            return null;
        });
    }

    @Test
    void testCreateCriteria() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("Ведение")
                .build();

        // When
        CriteriaDto result = criteriaService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());

        // Verify criteria was saved to database
        Optional<CriteriaEntity> savedCriteria = criteriaRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals(request.getName(), savedCriteria.get().getName());
    }

    @Test
    void testCreateCriteriaWithExistingName() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("Техника") // То же название, что и у существующего критерия
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criteriaService.create(request);
        });
    }

    @Test
    void testUpdateCriteria() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("Обновленная техника")
                .build();

        // When
        CriteriaDto result = criteriaService.update(testCriteria.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testCriteria.getId(), result.getId());
        assertEquals(request.getName(), result.getName());

        // Verify criteria was updated in database
        Optional<CriteriaEntity> updatedCriteria = criteriaRepository.findById(testCriteria.getId());
        assertTrue(updatedCriteria.isPresent());
        assertEquals(request.getName(), updatedCriteria.get().getName());
    }

    @Test
    void testUpdateCriteriaWithExistingName() {
        // Given
        // Сначала создаем другой критерий
        CriteriaEntity anotherCriteria = CriteriaEntity.builder()
                .name("Другой критерий")
                .build();
        criteriaRepository.save(anotherCriteria);

        CriteriaRequest request = CriteriaRequest.builder()
                .name("Другой критерий") // То же название, что и у другого критерия
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criteriaService.update(testCriteria.getId(), request);
        });
    }

    @Test
    void testUpdateCriteriaWithSameName() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("Техника") // То же название, что и у текущего критерия
                .build();

        // When
        CriteriaDto result = criteriaService.update(testCriteria.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(testCriteria.getId(), result.getId());
        assertEquals("Техника", result.getName());
    }

    @Test
    void testUpdateCriteriaWithNonExistentCriteria() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("Updated")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            criteriaService.update(999L, request);
        });
    }

    @Test
    void testFindAllCriteria() {
        // Create additional criteria
        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Стилистика")
                .build();
        criteriaRepository.save(criteria2);

        CriteriaEntity criteria3 = CriteriaEntity.builder()
                .name("Музыкальность")
                .build();
        criteriaRepository.save(criteria3);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CriteriaDto> result = criteriaService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindCriteriaById() {
        // When
        Optional<CriteriaDto> result = criteriaService.findById(testCriteria.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCriteria.getId(), result.get().getId());
        assertEquals(testCriteria.getName(), result.get().getName());
    }

    @Test
    void testFindCriteriaByIdNotFound() {
        // When
        Optional<CriteriaDto> result = criteriaService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteCriteria() {
        // Given
        Long criteriaId = testCriteria.getId();

        // When
        criteriaService.deleteById(criteriaId);

        // Then
        assertFalse(criteriaRepository.existsById(criteriaId));
    }

    @Test
    void testDeleteCriteriaNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            criteriaService.deleteById(999L);
        });
    }

    @Test
    void testDeleteCriteriaWithMilestoneAssignments() {
        // Given
        // Создаем назначение критерия правилу этапа
        MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                .milestoneRule(testMilestoneRule)
                .criteria(testCriteria)
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();
        milestoneCriteriaAssignmentRepository.save(assignment);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            criteriaService.deleteById(testCriteria.getId());
        });

        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("Нельзя удалить критерий, который используется в этапах"));
    }

    @Test
    void testDeleteCriteriaWithoutAssignments() {
        // Given
        // Создаем критерий без назначений
        CriteriaEntity criteriaWithoutAssignments = CriteriaEntity.builder()
                .name("Неиспользуемый критерий")
                .build();
        criteriaRepository.save(criteriaWithoutAssignments);

        // When
        criteriaService.deleteById(criteriaWithoutAssignments.getId());

        // Then
        assertFalse(criteriaRepository.existsById(criteriaWithoutAssignments.getId()));
    }

    @Test
    void testCriteriaNameMapping() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("Маппинг тест")
                .build();

        // When
        CriteriaDto result = criteriaService.create(request);

        // Then - Verify name field is correctly mapped
        assertNotNull(result);
        assertEquals("Маппинг тест", result.getName());

        // Verify in database
        Optional<CriteriaEntity> savedCriteria = criteriaRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals("Маппинг тест", savedCriteria.get().getName());
    }

    @Test
    void testCriteriaCascadeDelete() {
        // Given
        Long criteriaId = testCriteria.getId();

        // When
        criteriaService.deleteById(criteriaId);

        // Then
        assertFalse(criteriaRepository.existsById(criteriaId));

        // Verify related entities still exist (no cascade delete)
        assertTrue(milestoneRepository.existsById(testMilestone.getId()));
    }

    @Test
    void testFindCriteriaByName() {
        // Given
        String criteriaName = "Техника";

        // When
        Optional<CriteriaEntity> result = criteriaRepository.findByName(criteriaName);

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
        Optional<CriteriaEntity> result = criteriaRepository.findByName(criteriaName);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testCriteriaWithSpecialCharacters() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("Критерий с символами: !@#$%^&*()")
                .build();

        // When
        CriteriaDto result = criteriaService.create(request);

        // Then
        assertNotNull(result);
        assertEquals("Критерий с символами: !@#$%^&*()", result.getName());

        // Verify in database
        Optional<CriteriaEntity> savedCriteria = criteriaRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals("Критерий с символами: !@#$%^&*()", savedCriteria.get().getName());
    }

    @Test
    void testCriteriaWithLongName() {
        // Given
        String longName = "Очень длинное название критерия оценки, которое содержит много символов и должно корректно обрабатываться системой";
        CriteriaRequest request = CriteriaRequest.builder()
                .name(longName)
                .build();

        // When
        CriteriaDto result = criteriaService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(longName, result.getName());

        // Verify in database
        Optional<CriteriaEntity> savedCriteria = criteriaRepository.findById(result.getId());
        assertTrue(savedCriteria.isPresent());
        assertEquals(longName, savedCriteria.get().getName());
    }

    @Test
    void testUpdateCriteriaWithEmptyName() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name("") // Пустое название
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criteriaService.update(testCriteria.getId(), request);
        });
    }

    @Test
    void testUpdateCriteriaWithNullName() {
        // Given
        CriteriaRequest request = CriteriaRequest.builder()
                .name(null) // Null название
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            criteriaService.update(testCriteria.getId(), request);
        });
    }
}
