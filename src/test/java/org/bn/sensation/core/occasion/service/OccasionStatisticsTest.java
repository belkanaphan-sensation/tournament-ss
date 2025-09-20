package org.bn.sensation.core.occasion.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.occasion.service.dto.OccasionStatisticsDto;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class OccasionStatisticsTest extends AbstractIntegrationTest {

    @Autowired
    private OccasionService occasionService;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

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
                .description("Test Description")
                .status(Status.DRAFT)
                .startDate(ZonedDateTime.now(ZoneId.of("Europe/Samara")))
                .endDate(ZonedDateTime.now(ZoneId.of("Europe/Samara")).plusDays(3))
                .organization(testOrganization)
                .build();
        testOccasion = occasionRepository.save(testOccasion);
    }

    @Test
    void testGetStatistics_WithVariousActivityStatuses() {
        // Создаем активности с разными статусами
        ActivityEntity completedActivity = ActivityEntity.builder()
                .name("Completed Activity")
                .description("Completed Description")
                .status(Status.COMPLETED)
                .occasion(testOccasion)
                .build();
        activityRepository.save(completedActivity);

        ActivityEntity readyActivity = ActivityEntity.builder()
                .name("Ready Activity")
                .description("Ready Description")
                .status(Status.READY)
                .occasion(testOccasion)
                .build();
        activityRepository.save(readyActivity);

        ActivityEntity activeActivity = ActivityEntity.builder()
                .name("Active Activity")
                .description("Active Description")
                .status(Status.ACTIVE)
                .occasion(testOccasion)
                .build();
        activityRepository.save(activeActivity);

        ActivityEntity draftActivity = ActivityEntity.builder()
                .name("Draft Activity")
                .description("Draft Description")
                .status(Status.DRAFT)
                .occasion(testOccasion)
                .build();
        activityRepository.save(draftActivity);

        // Получаем статистику
        OccasionStatisticsDto statistics = occasionService.getStatistics(testOccasion.getId());

        // Проверяем результаты
        assertNotNull(statistics);
        assertEquals(testOccasion.getId(), statistics.getOccasionId());
        assertEquals("Test Occasion", statistics.getOccasionName());
        assertEquals(1L, statistics.getCompletedActivitiesCount());
        assertEquals(2L, statistics.getActiveActivitiesCount()); // READY + ACTIVE
        assertEquals(4L, statistics.getTotalActivitiesCount());
    }

    @Test
    void testGetStatistics_WithNoActivities() {
        // Получаем статистику для мероприятия без активностей
        OccasionStatisticsDto statistics = occasionService.getStatistics(testOccasion.getId());

        // Проверяем результаты
        assertNotNull(statistics);
        assertEquals(testOccasion.getId(), statistics.getOccasionId());
        assertEquals("Test Occasion", statistics.getOccasionName());
        assertEquals(0L, statistics.getCompletedActivitiesCount());
        assertEquals(0L, statistics.getActiveActivitiesCount());
        assertEquals(0L, statistics.getTotalActivitiesCount());
    }

    @Test
    void testGetStatistics_WithNonExistentOccasion() {
        // Пытаемся получить статистику для несуществующего мероприятия
        assertThrows(EntityNotFoundException.class, () -> {
            occasionService.getStatistics(999L);
        });
    }
}
