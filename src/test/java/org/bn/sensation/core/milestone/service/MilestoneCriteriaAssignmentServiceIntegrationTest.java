package org.bn.sensation.core.milestone.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.CompetitionRole;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.milestone.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneCriteriaAssignmentDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
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
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class MilestoneCriteriaAssignmentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MilestoneCriteriaAssignmentService milestoneCriteriaAssignmentService;

    @Autowired
    private MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private CriteriaRepository criteriaRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private MilestoneEntity testMilestone;
    private CriteriaEntity testCriteria;
    private OrganizationEntity testOrganization;
    private UserEntity testUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            milestoneCriteriaAssignmentRepository.deleteAll();
            milestoneRepository.deleteAll();
            criteriaRepository.deleteAll();
            activityRepository.deleteAll();
            occasionRepository.deleteAll();
            organizationRepository.deleteAll();
            userRepository.deleteAll();
            return null;
        });

        // Создание тестовых данных в отдельной транзакции
        transactionTemplate.execute(status -> {
            // Создание тестового пользователя
            testUser = UserEntity.builder()
                    .username("testuser" + System.currentTimeMillis())
                    .password("password123")
                    .person(org.bn.sensation.core.common.entity.Person.builder()
                            .name("Test")
                            .surname("User")
                            .email("test@example.com")
                            .phoneNumber("+1234567890")
                            .build())
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(Role.USER))
                    .build();
            testUser = userRepository.save(testUser);

            // Создание тестовой организации
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

            // Создание тестового мероприятия
            OccasionEntity testOccasion = OccasionEntity.builder()
                    .name("Test Occasion")
                    .description("Test Description")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .status(Status.DRAFT)
                    .organization(testOrganization)
                    .build();
            testOccasion = occasionRepository.save(testOccasion);

            // Создание тестовой активности
            ActivityEntity testActivity = ActivityEntity.builder()
                    .name("Test Activity")
                    .description("Test Description")
                    .startDateTime(java.time.LocalDateTime.now())
                    .endDateTime(java.time.LocalDateTime.now().plusHours(2))
                    .address(Address.builder()
                            .country("Russia")
                            .city("Moscow")
                            .streetName("Activity Street")
                            .streetNumber("2")
                            .comment("Activity Address")
                            .build())
                    .status(Status.DRAFT)
                    .occasion(testOccasion)
                    .build();
            testActivity = activityRepository.save(testActivity);

            // Создание тестового этапа
            testMilestone = MilestoneEntity.builder()
                    .name("Test Milestone")
                    .status(Status.DRAFT)
                    .activity(testActivity)
                    .build();
            testMilestone = milestoneRepository.save(testMilestone);

            // Создание тестового критерия
            testCriteria = CriteriaEntity.builder()
                    .name("Техника")
                    .build();
            testCriteria = criteriaRepository.save(testCriteria);

            return null;
        });
    }

    @Test
    void testCreateMilestoneCriteriaAssignment() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .competitionRole(CompetitionRole.LEADER)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId());
        assertEquals(CompetitionRole.LEADER, result.getCompetitionRole());

        // Проверяем, что назначение сохранено в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertEquals(testMilestone.getId(), savedAssignment.get().getMilestone().getId());
        assertEquals(testCriteria.getId(), savedAssignment.get().getCriteria().getId());
        assertEquals(CompetitionRole.LEADER, savedAssignment.get().getCompetitionRole());
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNullCompetitionRole() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .competitionRole(null)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId());
        assertNull(result.getCompetitionRole());

        // Проверяем, что назначение сохранено в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertNull(savedAssignment.get().getCompetitionRole());
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNonExistentMilestone() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(999L) // Несуществующий этап
                .criteriaId(testCriteria.getId())
                .competitionRole(CompetitionRole.LEADER)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.create(request);
        });
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNonExistentCriteria() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(999L) // Несуществующий критерий
                .competitionRole(CompetitionRole.LEADER)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.create(request);
        });
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithExistingAssignment() {
        // Given
        // Создаем первое назначение
        CreateMilestoneCriteriaAssignmentRequest request1 = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .competitionRole(CompetitionRole.LEADER)
                .build();
        milestoneCriteriaAssignmentService.create(request1);

        // Создаем второе назначение с теми же этапом и критерием
        CreateMilestoneCriteriaAssignmentRequest request2 = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneCriteriaAssignmentService.create(request2);
        });
    }

    @Test
    void testUpdateMilestoneCriteriaAssignment() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);
        
        // Создаем дополнительный критерий
        CriteriaEntity newCriteria = CriteriaEntity.builder()
                .name("Стилистика")
                .build();
        criteriaRepository.save(newCriteria);

        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .criteriaId(newCriteria.getId())
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.update(assignment.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(newCriteria.getId(), result.getCriteria().getId());
        assertEquals(CompetitionRole.FOLLOWER, result.getCompetitionRole());

        // Проверяем, что назначение обновлено в БД
        Optional<MilestoneCriteriaAssignmentEntity> updatedAssignment = milestoneCriteriaAssignmentRepository.findById(assignment.getId());
        assertTrue(updatedAssignment.isPresent());
        assertEquals(newCriteria.getId(), updatedAssignment.get().getCriteria().getId());
        assertEquals(CompetitionRole.FOLLOWER, updatedAssignment.get().getCompetitionRole());
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentPartial() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);

        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.update(assignment.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId()); // Не изменилось
        assertEquals(CompetitionRole.FOLLOWER, result.getCompetitionRole());

        // Проверяем, что назначение обновлено в БД
        Optional<MilestoneCriteriaAssignmentEntity> updatedAssignment = milestoneCriteriaAssignmentRepository.findById(assignment.getId());
        assertTrue(updatedAssignment.isPresent());
        assertEquals(testCriteria.getId(), updatedAssignment.get().getCriteria().getId());
        assertEquals(CompetitionRole.FOLLOWER, updatedAssignment.get().getCompetitionRole());
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentNotFound() {
        // Given
        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.update(999L, request);
        });
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentWithNonExistentMilestone() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);

        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(999L) // Несуществующий этап
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.update(assignment.getId(), request);
        });
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentWithNonExistentCriteria() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);

        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .criteriaId(999L) // Несуществующий критерий
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.update(assignment.getId(), request);
        });
    }

    @Test
    void testFindAllMilestoneCriteriaAssignments() {
        // Given
        createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);
        
        // Создаем дополнительные критерии и назначения
        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Ведение")
                .build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, CompetitionRole.FOLLOWER);

        CriteriaEntity criteria3 = CriteriaEntity.builder()
                .name("Музыкальность")
                .build();
        criteriaRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindMilestoneCriteriaAssignmentById() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);

        // When
        Optional<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findById(assignment.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(assignment.getId(), result.get().getId());
        assertEquals(testMilestone.getId(), result.get().getMilestone().getId());
        assertEquals(testCriteria.getId(), result.get().getCriteria().getId());
        assertEquals(CompetitionRole.LEADER, result.get().getCompetitionRole());
    }

    @Test
    void testFindMilestoneCriteriaAssignmentByIdNotFound() {
        // When
        Optional<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByMilestoneIdAndCriteriaId() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.findByMilestoneIdAndCriteriaId(
                testMilestone.getId(), testCriteria.getId());

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId());
        assertEquals(CompetitionRole.LEADER, result.getCompetitionRole());
    }

    @Test
    void testFindByMilestoneIdAndCriteriaIdNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.findByMilestoneIdAndCriteriaId(999L, 999L);
        });
    }

    @Test
    void testFindByMilestoneId() {
        // Given
        createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);
        
        // Создаем дополнительные критерии и назначения для того же этапа
        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Ведение")
                .build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, CompetitionRole.FOLLOWER);

        // Создаем назначение для другого этапа
        MilestoneEntity anotherMilestone = MilestoneEntity.builder()
                .name("Another Milestone")
                .status(Status.DRAFT)
                .activity(testMilestone.getActivity())
                .build();
        anotherMilestone = milestoneRepository.save(anotherMilestone);
        createTestAssignment(anotherMilestone, testCriteria, CompetitionRole.LEADER);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findByMilestoneId(
                testMilestone.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void testFindByCriteriaId() {
        // Given
        createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);
        
        // Создаем дополнительные этапы и назначения для того же критерия
        MilestoneEntity milestone2 = MilestoneEntity.builder()
                .name("Milestone 2")
                .status(Status.DRAFT)
                .activity(testMilestone.getActivity())
                .build();
        milestone2 = milestoneRepository.save(milestone2);
        createTestAssignment(milestone2, testCriteria, CompetitionRole.FOLLOWER);

        // Создаем назначение для другого критерия
        CriteriaEntity anotherCriteria = CriteriaEntity.builder()
                .name("Стилистика")
                .build();
        criteriaRepository.save(anotherCriteria);
        createTestAssignment(testMilestone, anotherCriteria, CompetitionRole.LEADER);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findByCriteriaId(
                testCriteria.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void testDeleteMilestoneCriteriaAssignment() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);
        Long assignmentId = assignment.getId();

        // When
        milestoneCriteriaAssignmentService.deleteById(assignmentId);

        // Then
        assertFalse(milestoneCriteriaAssignmentRepository.existsById(assignmentId));
    }

    @Test
    void testDeleteMilestoneCriteriaAssignmentNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneCriteriaAssignmentService.deleteById(999L);
        });
    }

    @Test
    void testMilestoneCriteriaAssignmentCompetitionRoleMapping() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .competitionRole(CompetitionRole.FOLLOWER)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(CompetitionRole.FOLLOWER, result.getCompetitionRole());

        // Проверяем в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertEquals(CompetitionRole.FOLLOWER, savedAssignment.get().getCompetitionRole());
    }

    @Test
    void testMilestoneCriteriaAssignmentWithNullCompetitionRole() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .competitionRole(null)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getCompetitionRole());

        // Проверяем в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertNull(savedAssignment.get().getCompetitionRole());
    }

    @Test
    void testMilestoneCriteriaAssignmentCascadeDelete() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, CompetitionRole.LEADER);
        Long assignmentId = assignment.getId();

        // When
        milestoneCriteriaAssignmentService.deleteById(assignmentId);

        // Then
        assertFalse(milestoneCriteriaAssignmentRepository.existsById(assignmentId));
        // Проверяем, что связанные сущности остались
        assertTrue(milestoneRepository.existsById(testMilestone.getId()));
        assertTrue(criteriaRepository.existsById(testCriteria.getId()));
    }

    // Вспомогательный метод для создания тестового назначения
    private MilestoneCriteriaAssignmentEntity createTestAssignment(MilestoneEntity milestone, CriteriaEntity criteria, CompetitionRole competitionRole) {
        return transactionTemplate.execute(status -> {
            MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                    .milestone(milestone)
                    .criteria(criteria)
                    .competitionRole(competitionRole)
                    .build();
            return milestoneCriteriaAssignmentRepository.save(assignment);
        });
    }
}
