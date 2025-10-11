package org.bn.sensation.core.milestone.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRuleRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneRuleDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRuleRequest;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class MilestoneRuleServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MilestoneRuleService milestoneRuleService;

    @Autowired
    private MilestoneRuleRepository milestoneRuleRepository;

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
    private CriteriaRepository criteriaRepository;

    @Autowired
    private MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private UserEntity testUser;
    private OrganizationEntity testOrganization;
    private OccasionEntity testOccasion;
    private ActivityEntity testActivity;
    private MilestoneEntity testMilestone;
    private CriteriaEntity testCriteria;

    @BeforeEach
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            // Сначала удаляем данные с внешними ключами
            milestoneCriteriaAssignmentRepository.deleteAll();
            milestoneRuleRepository.deleteAll();
            milestoneRepository.deleteAll();
            activityRepository.deleteAll();
            occasionRepository.deleteAll();
            organizationRepository.deleteAll();
            criteriaRepository.deleteAll();
            userRepository.deleteAll();
            return null;
        });

        transactionTemplate.execute(status -> {
        // Создаем тестового пользователя
        testUser = UserEntity.builder()
                .username("testuser")
                .password("password")
                .roles(Set.of(Role.ADMIN))
                .status(UserStatus.ACTIVE)
                .person(Person.builder()
                        .name("Test")
                        .surname("User")
                        .email("test@example.com")
                        .build())
                .build();
        testUser = userRepository.save(testUser);

        // Создаем тестовую организацию
        testOrganization = OrganizationEntity.builder()
                .name("Test Organization")
                .description("Test Organization Description")
                .address(Address.builder()
                        .country("Russia")
                        .city("Moscow")
                        .streetName("Test Street")
                        .streetNumber("1")
                        .build())
                .build();
        testOrganization = organizationRepository.save(testOrganization);

        // Создаем тестовое мероприятие
        testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Occasion Description")
                .startDate(java.time.LocalDate.now().plusDays(1))
                .endDate(java.time.LocalDate.now().plusDays(2))
                .organization(testOrganization)
                .state(OccasionState.DRAFT)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Создаем тестовую активность
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Activity Description")
                .occasion(testOccasion)
                .state(ActivityState.DRAFT)
                .build();
        testActivity = activityRepository.save(testActivity);

        // Создаем тестовый этап
        testMilestone = MilestoneEntity.builder()
                .name("Test Milestone")
                .description("Test Milestone Description")
                .activity(testActivity)
                .state(MilestoneState.DRAFT)
                .milestoneOrder(1)
                .build();
        testMilestone = milestoneRepository.save(testMilestone);

        // Создаем тестовый критерий
        testCriteria = CriteriaEntity.builder()
                .name("Прохождение")
                .build();
        testCriteria = criteriaRepository.save(testCriteria);
            return null;
        });
    }

    @Test
    void testCreateMilestoneRule_Success() {
        // Given
        CreateMilestoneRuleRequest request = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();

        // When
        MilestoneRuleDto result = milestoneRuleService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(AssessmentMode.SCORE, result.getAssessmentMode());
        assertEquals(10, result.getParticipantLimit());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());

        // Проверяем, что правило сохранено в БД
        Optional<MilestoneRuleEntity> savedRule = milestoneRuleRepository.findById(result.getId());
        assertTrue(savedRule.isPresent());
        assertEquals(AssessmentMode.SCORE, savedRule.get().getAssessmentMode());
        assertEquals(10, savedRule.get().getParticipantLimit());
    }

    @Test
    void testCreateMilestoneRule_WithNonExistentMilestone() {
        // Given
        CreateMilestoneRuleRequest request = CreateMilestoneRuleRequest.builder()
                .milestoneId(999L)
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneRuleService.create(request);
        });
    }

    @Test
    void testCreateMilestoneRule_WithNullMilestoneId() {
        // Given
        CreateMilestoneRuleRequest request = CreateMilestoneRuleRequest.builder()
                .milestoneId(null)
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneRuleService.create(request);
        });
    }

    @Test
    void testCreateMilestoneRule_WithExistingRule() {
        // Given - создаем первое правило
        CreateMilestoneRuleRequest firstRequest = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        milestoneRuleService.create(firstRequest);

        // When - пытаемся создать второе правило для того же этапа
        CreateMilestoneRuleRequest secondRequest = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.PASS)
                .participantLimit(20)
                .roundParticipantLimit(10)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneRuleService.create(secondRequest);
        });
    }

    @Test
    void testFindById_Success() {
        // Given
        CreateMilestoneRuleRequest request = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto created = milestoneRuleService.create(request);

        // When
        Optional<MilestoneRuleDto> result = milestoneRuleService.findById(created.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(AssessmentMode.SCORE, result.get().getAssessmentMode());
        assertEquals(10, result.get().getParticipantLimit());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<MilestoneRuleDto> result = milestoneRuleService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll_Success() {
        // Given - создаем несколько правил
        MilestoneEntity milestone2 = createTestMilestone("Test Milestone 2");
        MilestoneEntity milestone3 = createTestMilestone("Test Milestone 3");

        CreateMilestoneRuleRequest request1 = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        milestoneRuleService.create(request1);

        CreateMilestoneRuleRequest request2 = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestone2.getId())
                .assessmentMode(AssessmentMode.PASS)
                .participantLimit(20)
                .roundParticipantLimit(10)
                .build();
        milestoneRuleService.create(request2);

        CreateMilestoneRuleRequest request3 = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestone3.getId())
                .assessmentMode(AssessmentMode.PLACE)
                .participantLimit(30)
                .roundParticipantLimit(10)
                .build();
        milestoneRuleService.create(request3);

        // When
        Page<MilestoneRuleDto> result = milestoneRuleService.findAll(PageRequest.of(0, 10));

        // Then
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream().anyMatch(r -> r.getAssessmentMode() == AssessmentMode.SCORE));
        assertTrue(result.getContent().stream().anyMatch(r -> r.getAssessmentMode() == AssessmentMode.PASS));
        assertTrue(result.getContent().stream().anyMatch(r -> r.getAssessmentMode() == AssessmentMode.PLACE));
    }

    @Test
    void testFindByMilestoneId_Success() {
        // Given
        CreateMilestoneRuleRequest request = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto created = milestoneRuleService.create(request);

        // When
        MilestoneRuleDto result = milestoneRuleService.findByMilestoneId(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals(AssessmentMode.SCORE, result.getAssessmentMode());
        assertEquals(10, result.getParticipantLimit());
    }

    @Test
    void testFindByMilestoneId_NotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneRuleService.findByMilestoneId(testMilestone.getId());
        });
    }

    @Test
    void testUpdate_Success() {
        // Given
        CreateMilestoneRuleRequest createRequest = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto created = milestoneRuleService.create(createRequest);

        UpdateMilestoneRuleRequest updateRequest = UpdateMilestoneRuleRequest.builder()
                .assessmentMode(AssessmentMode.PASS)
                .participantLimit(20)
                .roundParticipantLimit(10)
                .build();

        // When
        MilestoneRuleDto result = milestoneRuleService.update(created.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals(AssessmentMode.PASS, result.getAssessmentMode());
        assertEquals(20, result.getParticipantLimit());

        // Проверяем, что изменения сохранены в БД
        Optional<MilestoneRuleEntity> savedRule = milestoneRuleRepository.findById(created.getId());
        assertTrue(savedRule.isPresent());
        assertEquals(AssessmentMode.PASS, savedRule.get().getAssessmentMode());
        assertEquals(20, savedRule.get().getParticipantLimit());
    }

    @Test
    void testUpdate_Partial() {
        // Given
        CreateMilestoneRuleRequest createRequest = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto created = milestoneRuleService.create(createRequest);

        UpdateMilestoneRuleRequest updateRequest = UpdateMilestoneRuleRequest.builder()
                .assessmentMode(AssessmentMode.PASS)
                .build(); // participantLimit не указан

        // When
        MilestoneRuleDto result = milestoneRuleService.update(created.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(AssessmentMode.PASS, result.getAssessmentMode());
        assertEquals(10, result.getParticipantLimit()); // Должен остаться прежним
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        UpdateMilestoneRuleRequest updateRequest = UpdateMilestoneRuleRequest.builder()
                .assessmentMode(AssessmentMode.PASS)
                .participantLimit(20)
                .roundParticipantLimit(10)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneRuleService.update(999L, updateRequest);
        });
    }

    @Test
    void testUpdate_WithNullId() {
        // Given
        UpdateMilestoneRuleRequest updateRequest = UpdateMilestoneRuleRequest.builder()
                .assessmentMode(AssessmentMode.PASS)
                .participantLimit(20)
                .roundParticipantLimit(10)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneRuleService.update(null, updateRequest);
        });
    }

    @Test
    void testDeleteById_Success() {
        // Given
        CreateMilestoneRuleRequest request = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto created = milestoneRuleService.create(request);
        // When
        milestoneRuleService.deleteById(created.getId());
        // Then
        assertFalse(milestoneRuleRepository.existsById(created.getId()));
    }

    @Test
    void testDeleteById_NotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneRuleService.deleteById(999L);
        });
    }

    @Test
    void testDeleteById_WithNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneRuleService.deleteById(null);
        });
    }

    @Test
    void testCreateMilestoneRule_WithAllAssessmentModes() {
        // Given - создаем этапы для каждого режима оценивания
        MilestoneEntity milestoneScore = createTestMilestone("Score Milestone");
        MilestoneEntity milestonePass = createTestMilestone("Pass Milestone");
        MilestoneEntity milestonePlace = createTestMilestone("Place Milestone");

        // When & Then - создаем правила с разными режимами оценивания
        CreateMilestoneRuleRequest scoreRequest = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestoneScore.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto scoreResult = milestoneRuleService.create(scoreRequest);
        assertEquals(AssessmentMode.SCORE, scoreResult.getAssessmentMode());

        CreateMilestoneRuleRequest passRequest = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestonePass.getId())
                .assessmentMode(AssessmentMode.PASS)
                .participantLimit(20)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto passResult = milestoneRuleService.create(passRequest);
        assertEquals(AssessmentMode.PASS, passResult.getAssessmentMode());

        CreateMilestoneRuleRequest placeRequest = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestonePlace.getId())
                .assessmentMode(AssessmentMode.PLACE)
                .participantLimit(30)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto placeResult = milestoneRuleService.create(placeRequest);
        assertEquals(AssessmentMode.PLACE, placeResult.getAssessmentMode());
    }

    @Test
    void testCreateMilestoneRule_WithDifferentParticipantLimits() {
        // Given - создаем этапы для разных лимитов участников
        MilestoneEntity milestone1 = createTestMilestone("Milestone 1");
        MilestoneEntity milestone2 = createTestMilestone("Milestone 2");
        MilestoneEntity milestone3 = createTestMilestone("Milestone 3");

        // When & Then - создаем правила с разными лимитами участников
        CreateMilestoneRuleRequest request1 = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestone1.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(1)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto result1 = milestoneRuleService.create(request1);
        assertEquals(1, result1.getParticipantLimit());

        CreateMilestoneRuleRequest request2 = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestone2.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(100)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto result2 = milestoneRuleService.create(request2);
        assertEquals(100, result2.getParticipantLimit());

        CreateMilestoneRuleRequest request3 = CreateMilestoneRuleRequest.builder()
                .milestoneId(milestone3.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(1000)
                .roundParticipantLimit(10)
                .build();
        MilestoneRuleDto result3 = milestoneRuleService.create(request3);
        assertEquals(1000, result3.getParticipantLimit());
    }

    @Test
    void testMilestoneRule_CascadeRelationship() {
        // Given
        CreateMilestoneRuleRequest request = CreateMilestoneRuleRequest.builder()
                .milestoneId(testMilestone.getId())
                .assessmentMode(AssessmentMode.SCORE)
                .participantLimit(10)
                .roundParticipantLimit(10)
                .build();

        // When
        MilestoneRuleDto result = milestoneRuleService.create(request);

        // Then - проверяем, что связь между Milestone и MilestoneRule установлена корректно
        Optional<MilestoneEntity> milestone = milestoneRepository.findById(testMilestone.getId());
        assertTrue(milestone.isPresent());
        assertNotNull(milestone.get().getMilestoneRule());
        assertEquals(result.getId(), milestone.get().getMilestoneRule().getId());

        Optional<MilestoneRuleEntity> rule = milestoneRuleRepository.findById(result.getId());
        assertTrue(rule.isPresent());
        assertNotNull(rule.get().getMilestone());
        assertEquals(testMilestone.getId(), rule.get().getMilestone().getId());
    }

    /**
     * Вспомогательный метод для создания тестового этапа
     */
    private MilestoneEntity createTestMilestone(String name) {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .name(name)
                .description(name + " Description")
                .activity(testActivity)
                .state(MilestoneState.DRAFT)
                .milestoneOrder((int) (milestoneRepository.count() + 1))
                .build();
        return milestoneRepository.save(milestone);
    }
}
