package no.experis.bgo.mybank.service;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.mybank.FlywayTestConfiguration;
import no.experis.bgo.mybank.TestContainersConfiguration;
import no.experis.bgo.mybank.model.CustomerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link CustomerService}.
 * Tests service methods with real database.
 */
@Slf4j
@SpringBootTest
@Import({FlywayTestConfiguration.class, TestContainersConfiguration.class})
@Transactional
@Tag("integration-test")
@Sql(scripts = "/test-data/customer-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService customerService;

    private CustomerEntity testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new CustomerEntity()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .personalIdNumber("12345678901")
                .phoneNumber("+4712345678");
    }

    @Test
    void shouldCreateCustomer() {
        // Given - using testCustomer from setUp

        // When
        CustomerEntity savedCustomer = customerService.create(testCustomer);

        // Then
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.id()).isNotNull();
        assertThat(savedCustomer.email()).isEqualTo(testCustomer.email());
        assertThat(savedCustomer.personalIdNumber()).isEqualTo(testCustomer.personalIdNumber());
    }

    @Test
    void shouldNotCreateCustomerWithDuplicateEmail() {
        // Given
        CustomerEntity duplicateEmailCustomer = new CustomerEntity()
                .firstName("Jane")
                .lastName("Doe")
                .email("test1@example.com") // Email from test data
                .personalIdNumber("99999999999")
                .phoneNumber("+4798765432");

        // When & Then
        assertThatThrownBy(() -> customerService.create(duplicateEmailCustomer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void shouldNotCreateCustomerWithDuplicatePersonalIdNumber() {
        // Given
        CustomerEntity duplicatePidCustomer = new CustomerEntity()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .personalIdNumber("10000000001") // PID from test data
                .phoneNumber("+4798765432");

        // When & Then
        assertThatThrownBy(() -> customerService.create(duplicatePidCustomer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Personal ID number already exists");
    }

    @Test
    void shouldFindCustomerById() {
        // Given - using data from class-level @Sql
        Long existingId = 100L;

        // When
        Optional<CustomerEntity> result = customerService.findById(existingId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(existingId);
        assertThat(result.get().email()).isEqualTo("test1@example.com");
    }

    @Test
    void shouldReturnEmptyWhenFindingNonExistentCustomerById() {
        // When
        Optional<CustomerEntity> result = customerService.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindCustomerByEmail() {
        // Given
        String existingEmail = "test1@example.com";

        // When
        Optional<CustomerEntity> result = customerService.findByEmail(existingEmail);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo(existingEmail);
    }

    @Test
    void shouldReturnEmptyWhenFindingNonExistentCustomerByEmail() {
        // When
        Optional<CustomerEntity> result = customerService.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindCustomerByPersonalIdNumber() {
        // Given
        String existingPid = "10000000001";

        // When
        Optional<CustomerEntity> result = customerService.findByPersonalIdNumber(existingPid);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().personalIdNumber()).isEqualTo(existingPid);
    }

    @Test
    void shouldReturnEmptyWhenFindingNonExistentCustomerByPersonalIdNumber() {
        // When
        Optional<CustomerEntity> result = customerService.findByPersonalIdNumber("99999999999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllCustomers() {
        // When
        List<CustomerEntity> results = customerService.findAll();

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSizeGreaterThanOrEqualTo(3); // At least the 3 from test data
    }

    @Test
    void shouldUpdateCustomer() {
        // Given
        Long existingId = 100L;
        Optional<CustomerEntity> existing = customerService.findById(existingId);
        assertThat(existing).isPresent();

        CustomerEntity updateData = new CustomerEntity()
                .id(existingId)  // Set the ID to match existing
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .personalIdNumber(existing.get().personalIdNumber())  // Keep existing PID
                .phoneNumber("+4799999999");

        // When
        CustomerEntity result = customerService.update(existingId, updateData);

        // Then
        assertThat(result.firstName()).isEqualTo("Updated");
        assertThat(result.email()).isEqualTo("updated@example.com");
        assertThat(result.personalIdNumber()).isEqualTo(existing.get().personalIdNumber());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
        // Given
        CustomerEntity updateData = testCustomer;

        // When & Then
        assertThatThrownBy(() -> customerService.update(999L, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithDuplicateEmail() {
        // Given
        Long existingId = 102L;
        CustomerEntity updateData = new CustomerEntity()
                .firstName("Updated")
                .lastName("Name")
                .email("test1@example.com") // Email from another test customer
                .personalIdNumber("10000000003")
                .phoneNumber("+4799999999");

        // When & Then
        assertThatThrownBy(() -> customerService.update(existingId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithDuplicatePersonalIdNumber() {
        // Given
        Long existingId = 102L;
        CustomerEntity updateData = new CustomerEntity()
                .firstName("Updated")
                .lastName("Name")
                .email("updated102@example.com")  // Use a new unique email
                .personalIdNumber("10000000001")  // PID from customer 100
                .phoneNumber("+4799999999");

        // When & Then
        assertThatThrownBy(() -> customerService.update(existingId, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Personal ID number already exists");
    }
}
