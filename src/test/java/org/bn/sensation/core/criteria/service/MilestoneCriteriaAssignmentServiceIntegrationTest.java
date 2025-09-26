package org.bn.sensation.core.criteria.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.criteria.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.criteria.service.dto.MilestoneCriteriaAssignmentDto;
import org.bn.sensation.core.criteria.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.user.entity.*;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.bn.sensation.security.SecurityUser;

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
    private UserActivityAssignmentRepository userActivityAssignmentRepository;

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
                    .state(State.DRAFT)
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
                    .state(State.DRAFT)
                    .occasion(testOccasion)
                    .build();
            testActivity = activityRepository.save(testActivity);

            // Создание тестового этапа
            testMilestone = MilestoneEntity.builder()
                    .name("Test Milestone")
                    .state(State.DRAFT)
                    .activity(testActivity)
                    .milestoneOrder(1)
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
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId());
        assertEquals(PartnerSide.LEADER, result.getPartnerSide());

        // Проверяем, что назначение сохранено в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertEquals(testMilestone.getId(), savedAssignment.get().getMilestone().getId());
        assertEquals(testCriteria.getId(), savedAssignment.get().getCriteria().getId());
        assertEquals(PartnerSide.LEADER, savedAssignment.get().getPartnerSide());
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNullCompetitionRole() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .partnerSide(null)
                .scale(10)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId());
        assertNull(result.getPartnerSide());

        // Проверяем, что назначение сохранено в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertNull(savedAssignment.get().getPartnerSide());
    }

    @Test
    void testCreateMilestoneCriteriaAssignmentWithNonExistentMilestone() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(999L) // Несуществующий этап
                .criteriaId(testCriteria.getId())
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
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
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
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
                .partnerSide(PartnerSide.LEADER)
                .scale(10)
                .build();
        milestoneCriteriaAssignmentService.create(request1);

        // Создаем второе назначение с теми же этапом и критерием
        CreateMilestoneCriteriaAssignmentRequest request2 = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .partnerSide(PartnerSide.FOLLOWER)
                .scale(10)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneCriteriaAssignmentService.create(request2);
        });
    }

    @Test
    void testUpdateMilestoneCriteriaAssignment() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // Создаем дополнительный критерий
        CriteriaEntity newCriteria = CriteriaEntity.builder()
                .name("Стилистика")
                .build();
        criteriaRepository.save(newCriteria);

        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .criteriaId(newCriteria.getId())
                .partnerSide(PartnerSide.FOLLOWER)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.update(assignment.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(newCriteria.getId(), result.getCriteria().getId());
        assertEquals(PartnerSide.FOLLOWER, result.getPartnerSide());

        // Проверяем, что назначение обновлено в БД
        Optional<MilestoneCriteriaAssignmentEntity> updatedAssignment = milestoneCriteriaAssignmentRepository.findById(assignment.getId());
        assertTrue(updatedAssignment.isPresent());
        assertEquals(newCriteria.getId(), updatedAssignment.get().getCriteria().getId());
        assertEquals(PartnerSide.FOLLOWER, updatedAssignment.get().getPartnerSide());
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentPartial() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .partnerSide(PartnerSide.FOLLOWER)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.update(assignment.getId(), request);

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId()); // Не изменилось
        assertEquals(PartnerSide.FOLLOWER, result.getPartnerSide());

        // Проверяем, что назначение обновлено в БД
        Optional<MilestoneCriteriaAssignmentEntity> updatedAssignment = milestoneCriteriaAssignmentRepository.findById(assignment.getId());
        assertTrue(updatedAssignment.isPresent());
        assertEquals(testCriteria.getId(), updatedAssignment.get().getCriteria().getId());
        assertEquals(PartnerSide.FOLLOWER, updatedAssignment.get().getPartnerSide());
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentNotFound() {
        // Given
        UpdateMilestoneCriteriaAssignmentRequest request = UpdateMilestoneCriteriaAssignmentRequest.builder()
                .partnerSide(PartnerSide.FOLLOWER)
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.update(999L, request);
        });
    }

    @Test
    void testUpdateMilestoneCriteriaAssignmentWithNonExistentMilestone() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

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
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

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
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // Создаем дополнительные критерии и назначения
        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Ведение")
                .build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

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
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // When
        Optional<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findById(assignment.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(assignment.getId(), result.get().getId());
        assertEquals(testMilestone.getId(), result.get().getMilestone().getId());
        assertEquals(testCriteria.getId(), result.get().getCriteria().getId());
        assertEquals(PartnerSide.LEADER, result.get().getPartnerSide());
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
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.findByMilestoneIdAndCriteriaId(
                testMilestone.getId(), testCriteria.getId());

        // Then
        assertNotNull(result);
        assertEquals(assignment.getId(), result.getId());
        assertEquals(testMilestone.getId(), result.getMilestone().getId());
        assertEquals(testCriteria.getId(), result.getCriteria().getId());
        assertEquals(PartnerSide.LEADER, result.getPartnerSide());
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
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // Создаем дополнительные критерии и назначения для того же этапа
        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Ведение")
                .build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        // Создаем назначение для другого этапа
        MilestoneEntity anotherMilestone = MilestoneEntity.builder()
                .name("Another Milestone")
                .state(State.DRAFT)
                .activity(testMilestone.getActivity())
                .milestoneOrder(2)
                .build();
        anotherMilestone = milestoneRepository.save(anotherMilestone);
        createTestAssignment(anotherMilestone, testCriteria, PartnerSide.LEADER);

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
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // Создаем дополнительные этапы и назначения для того же критерия
        MilestoneEntity milestone2 = MilestoneEntity.builder()
                .name("Milestone 2")
                .state(State.DRAFT)
                .activity(testMilestone.getActivity())
                .milestoneOrder(3)
                .build();
        milestone2 = milestoneRepository.save(milestone2);
        createTestAssignment(milestone2, testCriteria, PartnerSide.FOLLOWER);

        // Создаем назначение для другого критерия
        CriteriaEntity anotherCriteria = CriteriaEntity.builder()
                .name("Стилистика")
                .build();
        criteriaRepository.save(anotherCriteria);
        createTestAssignment(testMilestone, anotherCriteria, PartnerSide.LEADER);

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
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);
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
                .partnerSide(PartnerSide.FOLLOWER)
                .scale(10)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(PartnerSide.FOLLOWER, result.getPartnerSide());

        // Проверяем в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertEquals(PartnerSide.FOLLOWER, savedAssignment.get().getPartnerSide());
    }

    @Test
    void testMilestoneCriteriaAssignmentWithNullCompetitionRole() {
        // Given
        CreateMilestoneCriteriaAssignmentRequest request = CreateMilestoneCriteriaAssignmentRequest.builder()
                .milestoneId(testMilestone.getId())
                .criteriaId(testCriteria.getId())
                .partnerSide(null)
                .scale(10)
                .build();

        // When
        MilestoneCriteriaAssignmentDto result = milestoneCriteriaAssignmentService.create(request);

        // Then
        assertNotNull(result);
        assertNull(result.getPartnerSide());

        // Проверяем в БД
        Optional<MilestoneCriteriaAssignmentEntity> savedAssignment = milestoneCriteriaAssignmentRepository.findById(result.getId());
        assertTrue(savedAssignment.isPresent());
        assertNull(savedAssignment.get().getPartnerSide());
    }

    @Test
    void testMilestoneCriteriaAssignmentCascadeDelete() {
        // Given
        MilestoneCriteriaAssignmentEntity assignment = createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);
        Long assignmentId = assignment.getId();

        // When
        milestoneCriteriaAssignmentService.deleteById(assignmentId);

        // Then
        assertFalse(milestoneCriteriaAssignmentRepository.existsById(assignmentId));
        // Проверяем, что связанные сущности остались
        assertTrue(milestoneRepository.existsById(testMilestone.getId()));
        assertTrue(criteriaRepository.existsById(testCriteria.getId()));
    }

    // ========== Tests for findByMilestoneIdForCurrentUser method ==========

    @Test
    void testFindByMilestoneIdForCurrentUser_Success() {
        // Given
        // Создаем назначение пользователя на активность
        UserActivityAssignmentEntity userActivityAssignment = UserActivityAssignmentEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Ведение")
                .build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriteriaEntity criteria3 = CriteriaEntity.builder()
                .name("Музыкальность")
                .build();
        criteriaRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null); // null означает для всех

        // When
        List<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Только LEADER и null (для всех)

        // Проверяем, что возвращены только подходящие назначения
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.LEADER));
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == null));
        assertFalse(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.FOLLOWER));
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_WithFollowerPartnerSide() {
        // Given
        // Создаем назначение пользователя на активность как FOLLOWER
        UserActivityAssignmentEntity userActivityAssignment = UserActivityAssignmentEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.FOLLOWER)
                .build();
        userActivityAssignmentRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Ведение")
                .build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriteriaEntity criteria3 = CriteriaEntity.builder()
                .name("Музыкальность")
                .build();
        criteriaRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        // When
        List<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Только FOLLOWER и null (для всех)

        // Проверяем, что возвращены только подходящие назначения
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.FOLLOWER));
        assertTrue(result.stream().anyMatch(dto -> dto.getPartnerSide() == null));
        assertFalse(result.stream().anyMatch(dto -> dto.getPartnerSide() == PartnerSide.LEADER));
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_WithNullPartnerSide() {
        // Given
        // Создаем назначение пользователя на активность без указания стороны
        UserActivityAssignmentEntity userActivityAssignment = UserActivityAssignmentEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(null)
                .build();
        userActivityAssignmentRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        CriteriaEntity criteria2 = CriteriaEntity.builder()
                .name("Ведение")
                .build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriteriaEntity criteria3 = CriteriaEntity.builder()
                .name("Музыкальность")
                .build();
        criteriaRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        // When
        List<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Только null (для всех)

        // Проверяем, что возвращено только назначение для всех
        assertTrue(result.stream().allMatch(dto -> dto.getPartnerSide() == null));
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_EmptyAssignments() {
        // Given
        // Создаем назначение пользователя на активность
        UserActivityAssignmentEntity userActivityAssignment = UserActivityAssignmentEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(userActivityAssignment);

        // Не создаем никаких назначений критериев на этап

        // When
        List<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_NonExistentMilestone() {
        // Given
        Long nonExistentMilestoneId = 999L;

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(nonExistentMilestoneId);
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_NullMilestoneId() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(null);
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_UserNotAssignedToActivity() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // НЕ создаем назначение пользователя на активность

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(testMilestone.getId());
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_UserAssignedToDifferentActivity() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // Создаем другую активность
        ActivityEntity differentActivity = ActivityEntity.builder()
                .name("Different Activity")
                .description("Different Description")
                .startDateTime(java.time.LocalDateTime.now().plusDays(1))
                .endDateTime(java.time.LocalDateTime.now().plusDays(1).plusHours(2))
                .address(org.bn.sensation.core.common.entity.Address.builder()
                        .country("Russia")
                        .city("Moscow")
                        .streetName("Different Street")
                        .streetNumber("3")
                        .comment("Different Address")
                        .build())
                .state(org.bn.sensation.core.common.entity.State.DRAFT)
                .occasion(testMilestone.getActivity().getOccasion())
                .build();
        differentActivity = activityRepository.save(differentActivity);

        // Создаем назначение пользователя на ДРУГУЮ активность
        UserActivityAssignmentEntity userActivityAssignment = UserActivityAssignmentEntity.builder()
                .user(testUser)
                .activity(differentActivity)
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(userActivityAssignment);

        // Создаем назначения критериев на этап
        createTestAssignment(testMilestone, testCriteria, PartnerSide.LEADER);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(testMilestone.getId());
        });
    }

    @Test
    void testFindByMilestoneIdForCurrentUser_MultipleAssignmentsWithDifferentPartnerSides() {
        // Given
        // Настраиваем SecurityContext с тестовым пользователем
        setupSecurityContext(testUser);

        // Создаем назначение пользователя на активность как LEADER
        UserActivityAssignmentEntity userActivityAssignment = UserActivityAssignmentEntity.builder()
                .user(testUser)
                .activity(testMilestone.getActivity())
                .position(UserActivityPosition.PARTICIPANT)
                .partnerSide(PartnerSide.LEADER)
                .build();
        userActivityAssignmentRepository.save(userActivityAssignment);

        // Создаем несколько критериев и назначений
        CriteriaEntity criteria1 = CriteriaEntity.builder().name("Техника").build();
        criteriaRepository.save(criteria1);
        createTestAssignment(testMilestone, criteria1, PartnerSide.LEADER);

        CriteriaEntity criteria2 = CriteriaEntity.builder().name("Ведение").build();
        criteriaRepository.save(criteria2);
        createTestAssignment(testMilestone, criteria2, PartnerSide.FOLLOWER);

        CriteriaEntity criteria3 = CriteriaEntity.builder().name("Музыкальность").build();
        criteriaRepository.save(criteria3);
        createTestAssignment(testMilestone, criteria3, null);

        CriteriaEntity criteria4 = CriteriaEntity.builder().name("Артистизм").build();
        criteriaRepository.save(criteria4);
        createTestAssignment(testMilestone, criteria4, PartnerSide.LEADER);

        // When
        List<MilestoneCriteriaAssignmentDto> result = milestoneCriteriaAssignmentService.findByMilestoneIdForCurrentUser(testMilestone.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size()); // LEADER, LEADER, null

        // Проверяем, что возвращены только подходящие назначения
        long leaderCount = result.stream().filter(dto -> dto.getPartnerSide() == PartnerSide.LEADER).count();
        long nullCount = result.stream().filter(dto -> dto.getPartnerSide() == null).count();
        long followerCount = result.stream().filter(dto -> dto.getPartnerSide() == PartnerSide.FOLLOWER).count();

        assertEquals(2, leaderCount);
        assertEquals(1, nullCount);
        assertEquals(0, followerCount);
    }

    // Вспомогательный метод для создания тестового назначения
    private MilestoneCriteriaAssignmentEntity createTestAssignment(MilestoneEntity milestone, CriteriaEntity criteria, PartnerSide partnerSide) {
        return transactionTemplate.execute(status -> {
            MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                    .milestone(milestone)
                    .criteria(criteria)
                    .partnerSide(partnerSide)
                    .scale(10)
                    .build();
            return milestoneCriteriaAssignmentRepository.save(assignment);
        });
    }

    // Вспомогательный метод для настройки SecurityContext с тестовым пользователем
    private void setupSecurityContext(UserEntity user) {
        SecurityUser securityUser = (SecurityUser) SecurityUser.fromUser(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }
}
