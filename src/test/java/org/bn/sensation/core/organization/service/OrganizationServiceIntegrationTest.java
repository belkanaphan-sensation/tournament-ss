package org.bn.sensation.core.organization.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bn.sensation.AbstractIntegrationTest;
import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.occasion.statemachine.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.dto.UpdateOrganizationRequest;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;

@Transactional
class OrganizationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OccasionRepository occasionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private OrganizationEntity testOrganization;
    private UserEntity testUser;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        // Очистка базы данных перед каждым тестом
        cleanDatabase();

        // Очистка базы данных в отдельной транзакции
        TransactionTemplate clearTx = new TransactionTemplate(transactionManager);
        clearTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        clearTx.execute(status -> {
            // Очищаем данные в правильном порядке
            userRepository.deleteAll();
            occasionRepository.deleteAll();
            organizationRepository.deleteAll();
            return null;
        });

        // Заполнение тестовыми данными в отдельной транзакции
        TransactionTemplate fillTx = new TransactionTemplate(transactionManager);
        fillTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        fillTx.execute(status -> {
            // Создаем тестовую организацию
            testOrganization = OrganizationEntity.builder()
                    .name("Test Organization")
                    .description("Test Description")
                    .phoneNumber("+1234567890")
                    .email("test@organization.com")
                    .address(Address.builder()
                            .city("Test City")
                            .streetName("Test Street")
                            .streetNumber("123")
                            .build())
                    .build();
            testOrganization = organizationRepository.save(testOrganization);

            // Создаем тестового пользователя
            String uniqueUsername = "testuser_" + UUID.randomUUID();
            testUser = UserEntity.builder()
                    .username(uniqueUsername)
                    .password("password")
                    .status(UserStatus.ACTIVE)
                    .person(Person.builder()
                            .name("Test")
                            .surname("User")
                            .email("test@user.com")
                            .phoneNumber("+0987654321")
                            .build())
                    .roles(Set.of(Role.USER))
                    .organizations(Set.of(testOrganization))
                    .build();
            testUser = userRepository.save(testUser);

            return null;
        });
    }

    @Test
    void testCreateOrganization() {
        // Дано
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("New Organization")
                .description("New Description")
                .phoneNumber("+1111111111")
                .email("new@organization.com")
                .address(AddressDto.builder()
                        .city("New City")
                        .streetName("New Street")
                        .streetNumber("456")
                        .build())
                .build();

        // Когда
        OrganizationDto result = organizationService.create(request);

        // Тогда
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(request.getEmail(), result.getEmail());
        assertNotNull(result.getAddress());
        assertEquals(request.getAddress().getCity(), result.getAddress().getCity());
        assertEquals(request.getAddress().getStreetName(), result.getAddress().getStreetName());
        assertEquals(request.getAddress().getStreetNumber(), result.getAddress().getStreetNumber());

        // Проверяем, что организация была сохранена в базу данных
        Optional<OrganizationEntity> savedOrganization = organizationRepository.findById(result.getId());
        assertTrue(savedOrganization.isPresent());
        assertEquals(request.getName(), savedOrganization.get().getName());
        assertEquals(request.getDescription(), savedOrganization.get().getDescription());
        assertEquals(request.getPhoneNumber(), savedOrganization.get().getPhoneNumber());
        assertEquals(request.getEmail(), savedOrganization.get().getEmail());
        assertNotNull(savedOrganization.get().getAddress());
        assertEquals(request.getAddress().getCity(), savedOrganization.get().getAddress().getCity());
        assertEquals(request.getAddress().getStreetName(), savedOrganization.get().getAddress().getStreetName());
        assertEquals(request.getAddress().getStreetNumber(), savedOrganization.get().getAddress().getStreetNumber());
    }

    @Test
    void testCreateOrganizationWithExistingName() {
        // Дано
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Test Organization") // То же название, что и у существующей организации
                .description("New Description")
                .build();

        // Когда & Then
        assertThrows(IllegalArgumentException.class, () -> {
            organizationService.create(request);
        });
    }

    @Test
    void testCreateOrganizationWithExistingEmail() {
        // Дано
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("New Organization")
                .email("test@organization.com") // Тот же email, что и у существующей организации
                .build();

        // Когда & Then
        assertThrows(IllegalArgumentException.class, () -> {
            organizationService.create(request);
        });
    }

    @Test
    void testCreateOrganizationWithNullAddress() {
        // Дано
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("New Organization")
                .description("New Description")
                .address(null) // Null адрес
                .build();

        // Когда
        OrganizationDto result = organizationService.create(request);

        // Тогда
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertNull(result.getAddress());

        // Проверяем, что организация была сохранена с null адресом
        Optional<OrganizationEntity> savedOrganization = organizationRepository.findById(result.getId());
        assertTrue(savedOrganization.isPresent());
        assertNull(savedOrganization.get().getAddress());
    }

    @Test
    void testUpdateOrganization() {
        // Дано
        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .name("Updated Organization")
                .description("Updated Description")
                .phoneNumber("+2222222222")
                .email("updated@organization.com")
                .address(AddressDto.builder()
                        .city("Updated City")
                        .streetName("Updated Street")
                        .streetNumber("789")
                        .build())
                .build();

        // Когда
        OrganizationDto result = organizationService.update(testOrganization.getId(), request);

        // Тогда
        assertNotNull(result);
        assertEquals(testOrganization.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(request.getEmail(), result.getEmail());
        assertNotNull(result.getAddress());
        assertEquals(request.getAddress().getCity(), result.getAddress().getCity());
        assertEquals(request.getAddress().getStreetName(), result.getAddress().getStreetName());
        assertEquals(request.getAddress().getStreetNumber(), result.getAddress().getStreetNumber());

        // Проверяем, что организация была обновлена в базе данных
        Optional<OrganizationEntity> updatedOrganization = organizationRepository.findById(testOrganization.getId());
        assertTrue(updatedOrganization.isPresent());
        assertEquals(request.getName(), updatedOrganization.get().getName());
        assertEquals(request.getDescription(), updatedOrganization.get().getDescription());
        assertEquals(request.getPhoneNumber(), updatedOrganization.get().getPhoneNumber());
        assertEquals(request.getEmail(), updatedOrganization.get().getEmail());
        assertNotNull(updatedOrganization.get().getAddress());
        assertEquals(request.getAddress().getCity(), updatedOrganization.get().getAddress().getCity());
        assertEquals(request.getAddress().getStreetName(), updatedOrganization.get().getAddress().getStreetName());
        assertEquals(request.getAddress().getStreetNumber(), updatedOrganization.get().getAddress().getStreetNumber());
    }

    @Test
    void testUpdateOrganizationWithExistingName() {
        // Дано
        // Сначала создаем другую организацию
        OrganizationEntity anotherOrg = OrganizationEntity.builder()
                .name("Another Organization")
                .description("Another Description")
                .build();
        organizationRepository.save(anotherOrg);

        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .name("Another Organization") // То же название, что и у другой организации
                .build();

        // Когда & Then
        assertThrows(IllegalArgumentException.class, () -> {
            organizationService.update(testOrganization.getId(), request);
        });
    }

    @Test
    void testUpdateOrganizationWithExistingEmail() {
        // Дано
        // Сначала создаем другую организацию
        OrganizationEntity anotherOrg = OrganizationEntity.builder()
                .name("Another Organization")
                .email("another@organization.com")
                .build();
        organizationRepository.save(anotherOrg);

        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .email("another@organization.com") // Тот же email, что и у другой организации
                .build();

        // Когда & Then
        assertThrows(IllegalArgumentException.class, () -> {
            organizationService.update(testOrganization.getId(), request);
        });
    }

    @Test
    void testUpdateOrganizationWithSameName() {
        // Дано
        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .name("Test Organization") // То же название, что и у текущей организации
                .description("Updated Description")
                .build();

        // Когда
        OrganizationDto result = organizationService.update(testOrganization.getId(), request);

        // Тогда
        assertNotNull(result);
        assertEquals(testOrganization.getId(), result.getId());
        assertEquals("Test Organization", result.getName());
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    void testUpdateOrganizationWithSameEmail() {
        // Дано
        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .email("test@organization.com") // Тот же email, что и у текущей организации
                .description("Updated Description")
                .build();

        // Когда
        OrganizationDto result = organizationService.update(testOrganization.getId(), request);

        // Тогда
        assertNotNull(result);
        assertEquals(testOrganization.getId(), result.getId());
        assertEquals("test@organization.com", result.getEmail());
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    void testUpdateOrganizationWithNonExistentOrganization() {
        // Дано
        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .name("Updated")
                .build();

        // Когда & Then
        assertThrows(EntityNotFoundException.class, () -> {
            organizationService.update(999L, request);
        });
    }

    @Test
    void testFindAllOrganizations() {
        // Создаем дополнительные организации
        OrganizationEntity organization2 = OrganizationEntity.builder()
                .name("Organization 2")
                .description("Description 2")
                .build();
        organizationRepository.save(organization2);

        OrganizationEntity organization3 = OrganizationEntity.builder()
                .name("Organization 3")
                .description("Description 3")
                .build();
        organizationRepository.save(organization3);

        Pageable pageable = PageRequest.of(0, 10);

        // Когда
        Page<OrganizationDto> result = organizationService.findAll(pageable);

        // Тогда
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
    }

    @Test
    void testFindOrganizationById() {
        // Когда
        Optional<OrganizationDto> result = organizationService.findById(testOrganization.getId());

        // Тогда
        assertTrue(result.isPresent());
        assertEquals(testOrganization.getId(), result.get().getId());
        assertEquals(testOrganization.getName(), result.get().getName());
        assertEquals(testOrganization.getDescription(), result.get().getDescription());
        assertEquals(testOrganization.getPhoneNumber(), result.get().getPhoneNumber());
        assertEquals(testOrganization.getEmail(), result.get().getEmail());
        assertNotNull(result.get().getAddress());
        assertEquals(testOrganization.getAddress().getCity(), result.get().getAddress().getCity());
        assertEquals(testOrganization.getAddress().getStreetName(), result.get().getAddress().getStreetName());
        assertEquals(testOrganization.getAddress().getStreetNumber(), result.get().getAddress().getStreetNumber());
        assertEquals(1, result.get().getUsers().size());
    }

    @Test
    void testFindOrganizationByIdNotFound() {
        // Когда
        Optional<OrganizationDto> result = organizationService.findById(999L);

        // Тогда
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteOrganization() {
        // Дано
        Long organizationId = testOrganization.getId();

        // Когда
        organizationService.deleteById(organizationId);

        // Тогда
        assertFalse(organizationRepository.existsById(organizationId));
    }

    @Test
    void testDeleteOrganizationNotFound() {
        // Когда & Then
        assertThrows(EntityNotFoundException.class, () -> {
            organizationService.deleteById(999L);
        });
    }

    @Test
    void testDeleteOrganizationWithActiveOccasion() {
        // Дано
        // Создаем мероприятие со статусом ACTIVE в отдельной транзакции
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(status -> {
            OrganizationEntity orgRef = organizationRepository.getReferenceById(testOrganization.getId());
            OccasionEntity activeOccasion = OccasionEntity.builder()
                    .name("Active Occasion")
                    .description("Active Description")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .state(OccasionState.IN_PROGRESS)
                    .organization(orgRef)
                    .build();
            occasionRepository.save(activeOccasion);
            return null;
        });

        // Когда & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationService.deleteById(testOrganization.getId());
        });

        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("Нельзя удалить организацию, у которой есть активные мероприятия"));
        assertTrue(exception.getMessage().contains("Active Occasion"));
        assertTrue(exception.getMessage().contains("IN_PROGRESS"));
    }

    @Test
    void testDeleteOrganizationWithReadyOccasion() {
        // Дано
        // Создаем мероприятие со статусом READY в отдельной транзакции
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(status -> {
            OrganizationEntity orgRef = organizationRepository.getReferenceById(testOrganization.getId());
            OccasionEntity readyOccasion = OccasionEntity.builder()
                    .name("Ready Occasion")
                    .description("Ready Description")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .state(OccasionState.PLANNED)
                    .organization(orgRef)
                    .build();
            occasionRepository.save(readyOccasion);
            return null;
        });

        // Когда & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationService.deleteById(testOrganization.getId());
        });

        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("Нельзя удалить организацию, у которой есть активные мероприятия"));
        assertTrue(exception.getMessage().contains("Ready Occasion"));
        assertTrue(exception.getMessage().contains("PLANNED"));
    }

    @Test
    void testDeleteOrganizationWithDraftOccasion() {
        // Дано
        // Создаем мероприятие со статусом DRAFT в отдельной транзакции
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(status -> {
            OrganizationEntity orgRef = organizationRepository.getReferenceById(testOrganization.getId());
            OccasionEntity draftOccasion = OccasionEntity.builder()
                    .name("Draft Occasion")
                    .description("Draft Description")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .state(OccasionState.PLANNED)
                    .organization(orgRef)
                    .build();
            occasionRepository.save(draftOccasion);
            return null;
        });

        // Когда
        organizationService.deleteById(testOrganization.getId());

        // Тогда
        assertFalse(organizationRepository.existsById(testOrganization.getId()));
    }

    @Test
    void testDeleteOrganizationWithCompletedOccasion() {
        // Дано
        // Создаем мероприятие со статусом COMPLETED в отдельной транзакции
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(status -> {
            OrganizationEntity orgRef = organizationRepository.getReferenceById(testOrganization.getId());
            OccasionEntity completedOccasion = OccasionEntity.builder()
                    .name("Completed Occasion")
                    .description("Completed Description")
                    .startDate(LocalDate.now().minusDays(10))
                    .endDate(LocalDate.now().minusDays(7))
                    .state(OccasionState.COMPLETED)
                    .organization(orgRef)
                    .build();
            occasionRepository.save(completedOccasion);
            return null;
        });

        // Когда
        organizationService.deleteById(testOrganization.getId());

        // Тогда
        assertFalse(organizationRepository.existsById(testOrganization.getId()));
    }

    @Test
    void testDeleteOrganizationWithoutOccasions() {
        // Дано
        // Создаем организацию без мероприятий
        OrganizationEntity organizationWithoutOccasions = OrganizationEntity.builder()
                .name("Organization Without Occasions")
                .description("Description")
                .build();
        organizationRepository.save(organizationWithoutOccasions);

        // Когда
        organizationService.deleteById(organizationWithoutOccasions.getId());

        // Тогда
        assertFalse(organizationRepository.existsById(organizationWithoutOccasions.getId()));
    }

    @Test
    void testOrganizationWithNullAddressFields() {
        // Дано
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Test Org")
                .description("Test Description")
                .address(AddressDto.builder()
                        .city(null) // Null город
                        .streetName(null) // Null название улицы
                        .streetNumber(null) // Null номер дома
                        .build())
                .build();

        // Когда
        OrganizationDto result = organizationService.create(request);

        // Тогда
        assertNotNull(result);
        assertNotNull(result.getAddress());
        assertNull(result.getAddress().getCity());
        assertNull(result.getAddress().getStreetName());
        assertNull(result.getAddress().getStreetNumber());

        // Проверяем, что организация была сохранена с null адресом fields
        Optional<OrganizationEntity> savedOrganization = organizationRepository.findById(result.getId());
        assertTrue(savedOrganization.isPresent());
        assertNotNull(savedOrganization.get().getAddress());
        assertNull(savedOrganization.get().getAddress().getCity());
        assertNull(savedOrganization.get().getAddress().getStreetName());
        assertNull(savedOrganization.get().getAddress().getStreetNumber());
    }

    @Test
    void testOrganizationAddressMapping() {
        // Дано
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Mapping Test")
                .description("Test Description")
                .address(AddressDto.builder()
                        .city("Mapping City")
                        .streetName("Mapping Street")
                        .streetNumber("Mapping 123")
                        .build())
                .build();

        // Когда
        OrganizationDto result = organizationService.create(request);

        // Тогда - Verify all Address fields are correctly mapped
        assertNotNull(result.getAddress());
        assertEquals("Mapping City", result.getAddress().getCity());
        assertEquals("Mapping Street", result.getAddress().getStreetName());
        assertEquals("Mapping 123", result.getAddress().getStreetNumber());

        // Проверяем в базе данных
        Optional<OrganizationEntity> savedOrganization = organizationRepository.findById(result.getId());
        assertTrue(savedOrganization.isPresent());
        Address savedAddress = savedOrganization.get().getAddress();
        assertNotNull(savedAddress);
        assertEquals("Mapping City", savedAddress.getCity());
        assertEquals("Mapping Street", savedAddress.getStreetName());
        assertEquals("Mapping 123", savedAddress.getStreetNumber());
    }

    @Test
    void testOrganizationCascadeDelete() {
        // Дано
        Long organizationId = testOrganization.getId();

        // Когда
        organizationService.deleteById(organizationId);

        // Тогда
        assertFalse(organizationRepository.existsById(organizationId));

        // Проверяем, что связанные пользователи все еще существуют (нет каскадного удаления)
        assertTrue(userRepository.existsById(testUser.getId()));
    }

    @Test
    void testUpdateOrganizationWithPartialAddress() {
        // Дано
        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .name("Updated Organization")
                .address(AddressDto.builder()
                        .city("Updated City")
                        .streetName(null) // Обновляем только город, оставляем другие поля null
                        .streetNumber(null)
                        .build())
                .build();

        // Когда
        OrganizationDto result = organizationService.update(testOrganization.getId(), request);

        // Тогда
        assertNotNull(result);
        assertEquals("Updated Organization", result.getName());
        assertNotNull(result.getAddress());
        assertEquals("Updated City", result.getAddress().getCity());
        // Название улицы и номер должны остаться неизменными (null значения игнорируются)
        assertEquals("Test Street", result.getAddress().getStreetName());
        assertEquals("123", result.getAddress().getStreetNumber());

        // Проверяем в базе данных
        Optional<OrganizationEntity> updatedOrganization = organizationRepository.findById(testOrganization.getId());
        assertTrue(updatedOrganization.isPresent());
        Address savedAddress = updatedOrganization.get().getAddress();
        assertNotNull(savedAddress);
        assertEquals("Updated City", savedAddress.getCity());
        assertEquals("Test Street", savedAddress.getStreetName());
        assertEquals("123", savedAddress.getStreetNumber());
    }
}
