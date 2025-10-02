package no.experis.bgo.mybank.mapper;

import no.experis.bgo.mybank.api.model.Customer;
import no.experis.bgo.mybank.api.model.CustomerRequest;
import no.experis.bgo.mybank.model.CustomerEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {

    @Test
    void toApiModel_shouldMapAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CustomerEntity entity = new CustomerEntity()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .personalIdNumber("12345678901")
                .phoneNumber("+4712345678")
                .created(now)
                .updated(now);

        // When
        Customer result = CustomerMapper.toApiModel(entity);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("12345678901", result.getPersonalIdNumber());
        assertEquals("+4712345678", result.getPhoneNumber());
        assertEquals(now.atOffset(ZoneOffset.UTC), result.getCreated());
        assertEquals(now.atOffset(ZoneOffset.UTC), result.getUpdated());
    }

    @Test
    void toApiModel_withNullInput_shouldReturnNull() {
        assertNull(CustomerMapper.toApiModel(null));
    }

    @Test
    void toEntity_shouldMapAllFields() {
        // Given
        CustomerRequest request = new CustomerRequest()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .personalIdNumber("98765432109")
                .phoneNumber("+4787654321");

        // When
        CustomerEntity result = CustomerMapper.toEntity(request);

        // Then
        assertNotNull(result);
        assertNull(result.id()); // ID should not be set from request
        assertEquals("Jane", result.firstName());
        assertEquals("Smith", result.lastName());
        assertEquals("jane.smith@example.com", result.email());
        assertEquals("98765432109", result.personalIdNumber());
        assertEquals("+4787654321", result.phoneNumber());
        assertNull(result.created()); // Should be set by DB
        assertNull(result.updated()); // Should be set by DB
    }

    @Test
    void toEntity_shouldHandleValidEmailFormat() {
        // Given
        CustomerRequest request = new CustomerRequest()
                .firstName("Test")
                .lastName("User")
                .email("valid.email@example.com")
                .personalIdNumber("12345678901")
                .phoneNumber("+4712345678");

        // When
        CustomerEntity result = CustomerMapper.toEntity(request);

        // Then
        assertNotNull(result);
        assertEquals("valid.email@example.com", result.email());
    }

    @Test
    void toEntity_shouldHandleValidPhoneFormat() {
        // Given
        CustomerRequest request = new CustomerRequest()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .personalIdNumber("12345678901")
                .phoneNumber("+47-123-45-678");

        // When
        CustomerEntity result = CustomerMapper.toEntity(request);

        // Then
        assertNotNull(result);
        assertEquals("+47-123-45-678", result.phoneNumber());
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CustomerEntity existingEntity = new CustomerEntity()
                .id(1L)
                .firstName("Old")
                .lastName("Name")
                .email("old.name@example.com")
                .personalIdNumber("11111111111")
                .phoneNumber("+4711111111")
                .created(now)
                .updated(now);

        CustomerRequest updateRequest = new CustomerRequest()
                .firstName("New")
                .lastName("Name")
                .email("new.name@example.com")
                .personalIdNumber("22222222222")
                .phoneNumber("+4722222222");

        // When
        CustomerEntity result = CustomerMapper.updateEntity(existingEntity, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id()); // ID should remain unchanged
        assertEquals("New", result.firstName());
        assertEquals("Name", result.lastName());
        assertEquals("new.name@example.com", result.email());
        assertEquals("22222222222", result.personalIdNumber());
        assertEquals("+4722222222", result.phoneNumber());
        assertEquals(now, result.created()); // Created timestamp should remain unchanged
        assertEquals(now, result.updated()); // Updated timestamp will be changed by DB
    }

    @Test
    void updateEntity_withNullRequest_shouldReturnOriginalEntity() {
        // Given
        CustomerEntity entity = new CustomerEntity()
                .firstName("Original")
                .lastName("Name");

        // When
        CustomerEntity result = CustomerMapper.updateEntity(entity, null);

        // Then
        assertSame(entity, result);
    }

    @Test
    void updateEntity_withNullEntity_shouldReturnNull() {
        // Given
        CustomerRequest request = new CustomerRequest()
                .firstName("Test")
                .lastName("Name");

        // When
        CustomerEntity result = CustomerMapper.updateEntity(null, request);

        // Then
        assertNull(result);
    }

    @Test
    void toApiModelList_shouldMapAllEntities() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<CustomerEntity> entities = List.of(
                new CustomerEntity()
                        .id(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@example.com")
                        .personalIdNumber("12345678901")
                        .phoneNumber("+4712345678")
                        .created(now)
                        .updated(now),
                new CustomerEntity()
                        .id(2L)
                        .firstName("Jane")
                        .lastName("Smith")
                        .email("jane.smith@example.com")
                        .personalIdNumber("98765432109")
                        .phoneNumber("+4787654321")
                        .created(now)
                        .updated(now)
        );

        // When
        List<Customer> results = CustomerMapper.toApiModelList(entities);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());

        Customer first = results.get(0);
        assertEquals(1L, first.getId());
        assertEquals("John", first.getFirstName());
        assertEquals("Doe", first.getLastName());
        assertEquals("john.doe@example.com", first.getEmail());
        assertEquals("12345678901", first.getPersonalIdNumber());
        assertEquals("+4712345678", first.getPhoneNumber());

        Customer second = results.get(1);
        assertEquals(2L, second.getId());
        assertEquals("Jane", second.getFirstName());
        assertEquals("Smith", second.getLastName());
        assertEquals("jane.smith@example.com", second.getEmail());
        assertEquals("98765432109", second.getPersonalIdNumber());
        assertEquals("+4787654321", second.getPhoneNumber());
    }

    @Test
    void toApiModelList_withNullInput_shouldReturnEmptyList() {
        List<Customer> results = CustomerMapper.toApiModelList(null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
