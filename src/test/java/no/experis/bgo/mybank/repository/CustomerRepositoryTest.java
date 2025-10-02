package no.experis.bgo.mybank.repository;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.mybank.model.CustomerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CustomerRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CustomerRepository repository;

    @Test
    @Sql("classpath:test-data/customer-test-data.sql")
    void shouldFindCustomerByEmail() {
        // When
        Optional<CustomerEntity> result = repository.findByEmail("john.doe@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("John", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
    }

    @Test
    @Sql("classpath:test-data/customer-test-data.sql")
    void shouldFindCustomerByPersonalIdNumber() {
        // When
        Optional<CustomerEntity> result = repository.findByPersonalIdNumber("12345678901");

        // Then
        assertTrue(result.isPresent());
        assertEquals("John", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
    }

    @Test
    @Sql("classpath:test-data/customer-test-data.sql")
    void shouldCheckIfCustomerExistsByEmail() {
        // When
        boolean exists = repository.existsByEmail("john.doe@example.com");
        boolean notExists = repository.existsByEmail("nonexistent@example.com");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @Sql("classpath:test-data/customer-test-data.sql")
    void shouldCheckIfCustomerExistsByPersonalIdNumber() {
        // When
        boolean exists = repository.existsByPersonalIdNumber("12345678901");
        boolean notExists = repository.existsByPersonalIdNumber("99999999999");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void shouldSaveNewCustomer() {
        // Given
        CustomerEntity customer = new CustomerEntity()
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .personalIdNumber("11223344556")
                .phoneNumber("+4799887766");

        // When
        CustomerEntity saved = repository.saveAndFlush(customer);

        // Then
        assertNotNull(saved.id());
        assertEquals("Test", saved.firstName());
        assertEquals("User", saved.lastName());
        assertEquals("test.user@example.com", saved.email());
        assertEquals("11223344556", saved.personalIdNumber());
        assertEquals("+4799887766", saved.phoneNumber());
        assertNotNull(saved.created());
        assertNotNull(saved.updated());
    }
}
