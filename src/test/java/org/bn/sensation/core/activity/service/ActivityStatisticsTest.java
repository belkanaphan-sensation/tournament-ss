package org.bn.sensation.core.activity.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.service.dto.ActivityStatisticsDto;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class ActivityStatisticsTest extends AbstractIntegrationTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private ActivityEntity testActivity;
    private OccasionEntity testOccasion;
    private OrganizationEntity testOrganization;

    @BeforeEach
    void setUp() {
        // Создаем тестовую организацию
        testOrganization = OrganizationEntity.builder()
                .name("Test Organization")
                .description("Test Description")
                .build();
        testOrganization = organizationRepository.save(testOrganization);

        // Создаем тестовое мероприятие
        testOccasion = OccasionEntity.builder()
                .name("Test Occasion")
                .description("Test Occasion Description")
                .startDate(java.time.LocalDate.now())
                .endDate(java.time.LocalDate.now().plusDays(1))
                .status(Status.DRAFT)
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);

        // Создаем тестовую активность
        testActivity = ActivityEntity.builder()
                .name("Test Activity")
                .description("Test Activity Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .status(Status.DRAFT)
                .occasion(testOccasion)
                .build();
        testActivity = activityRepository.save(testActivity);
    }

    @Test
    void testGetMilestoneStatistics_WithVariousMilestoneStatuses() {
        // Создаем этапы с разными статусами
        MilestoneEntity completedMilestone = MilestoneEntity.builder()
                .name("Completed Milestone")
                .description("Completed Description")
                .status(Status.COMPLETED)
                .activity(testActivity)
                .build();
        milestoneRepository.save(completedMilestone);

        MilestoneEntity readyMilestone = MilestoneEntity.builder()
                .name("Ready Milestone")
                .description("Ready Description")
                .status(Status.READY)
                .activity(testActivity)
                .build();
        milestoneRepository.save(readyMilestone);

        MilestoneEntity activeMilestone = MilestoneEntity.builder()
                .name("Active Milestone")
                .description("Active Description")
                .status(Status.ACTIVE)
                .activity(testActivity)
                .build();
        milestoneRepository.save(activeMilestone);

        MilestoneEntity draftMilestone = MilestoneEntity.builder()
                .name("Draft Milestone")
                .description("Draft Description")
                .status(Status.DRAFT)
                .activity(testActivity)
                .build();
        milestoneRepository.save(draftMilestone);

        // Получаем статистику
        ActivityStatisticsDto statistics = activityService.getMilestoneStatistics(testActivity.getId());

        // Проверяем результаты
        assertNotNull(statistics);
        assertEquals(testActivity.getId(), statistics.getActivityId());
        assertEquals("Test Activity", statistics.getActivityName());
        assertEquals(1L, statistics.getCompletedMilestonesCount());
        assertEquals(4L, statistics.getTotalMilestonesCount());
    }

    @Test
    void testGetMilestoneStatistics_WithNoMilestones() {
        // Получаем статистику для активности без этапов
        ActivityStatisticsDto statistics = activityService.getMilestoneStatistics(testActivity.getId());

        // Проверяем результаты
        assertNotNull(statistics);
        assertEquals(testActivity.getId(), statistics.getActivityId());
        assertEquals("Test Activity", statistics.getActivityName());
        assertEquals(0L, statistics.getCompletedMilestonesCount());
        assertEquals(0L, statistics.getTotalMilestonesCount());
    }

    @Test
    void testGetMilestoneStatistics_WithOnlyCompletedMilestones() {
        // Создаем только завершенные этапы
        MilestoneEntity completedMilestone1 = MilestoneEntity.builder()
                .name("Completed Milestone 1")
                .description("Completed Description 1")
                .status(Status.COMPLETED)
                .activity(testActivity)
                .build();
        milestoneRepository.save(completedMilestone1);

        MilestoneEntity completedMilestone2 = MilestoneEntity.builder()
                .name("Completed Milestone 2")
                .description("Completed Description 2")
                .status(Status.COMPLETED)
                .activity(testActivity)
                .build();
        milestoneRepository.save(completedMilestone2);

        // Получаем статистику
        ActivityStatisticsDto statistics = activityService.getMilestoneStatistics(testActivity.getId());

        // Проверяем результаты
        assertNotNull(statistics);
        assertEquals(testActivity.getId(), statistics.getActivityId());
        assertEquals("Test Activity", statistics.getActivityName());
        assertEquals(2L, statistics.getCompletedMilestonesCount());
        assertEquals(2L, statistics.getTotalMilestonesCount());
    }

    @Test
    void testGetMilestoneStatistics_WithNonExistentActivity() {
        // Пытаемся получить статистику для несуществующей активности
        Long nonExistentActivityId = 999L;

        assertThrows(EntityNotFoundException.class, () -> {
            activityService.getMilestoneStatistics(nonExistentActivityId);
        });
    }
}
