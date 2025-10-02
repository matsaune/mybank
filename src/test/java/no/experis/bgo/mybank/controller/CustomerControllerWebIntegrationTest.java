package no.experis.bgo.mybank.controller;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.mybank.FlywayTestConfiguration;
import no.experis.bgo.mybank.TestContainersConfiguration;
import no.experis.bgo.mybank.api.model.Customer;
import no.experis.bgo.mybank.api.model.CustomerRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CustomerController.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("integration-test")
@Import({FlywayTestConfiguration.class, TestContainersConfiguration.class})
@Sql(scripts = "/test-data/customer-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CustomerControllerWebIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldGetAllCustomers() {
        ResponseEntity<List<Customer>> response = restTemplate.exchange(
                "/customers",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());

        // Verify first customer matches test data
        Customer firstCustomer = response.getBody().get(0);
        assertEquals("Test", firstCustomer.getFirstName());
        assertEquals("User", firstCustomer.getLastName());
        assertEquals("test.user@example.com", firstCustomer.getEmail());
        assertEquals("12345678901", firstCustomer.getPersonalIdNumber());
        assertEquals("+4711223344", firstCustomer.getPhoneNumber());
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        CustomerRequest request = new CustomerRequest()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .personalIdNumber("45678901234")
                .phoneNumber("+4712345678");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<Customer> response = restTemplate.exchange(
                "/customers",
                HttpMethod.POST,
                requestEntity,
                Customer.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        assertEquals("45678901234", response.getBody().getPersonalIdNumber());
        assertEquals("+4712345678", response.getBody().getPhoneNumber());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void shouldGetCustomerById() {
        ResponseEntity<Customer> response = restTemplate.getForEntity(
                "/customers/1",
                Customer.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test", response.getBody().getFirstName());
        assertEquals("User", response.getBody().getLastName());
        assertEquals("test.user@example.com", response.getBody().getEmail());
        assertEquals("12345678901", response.getBody().getPersonalIdNumber());
        assertEquals("+4711223344", response.getBody().getPhoneNumber());
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        CustomerRequest request = new CustomerRequest()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .personalIdNumber("56789012345")
                .phoneNumber("+4787654321");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<Customer> response = restTemplate.exchange(
                "/customers/1",
                HttpMethod.PUT,
                requestEntity,
                Customer.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        assertEquals("56789012345", response.getBody().getPersonalIdNumber());
        assertEquals("+4787654321", response.getBody().getPhoneNumber());
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/customers/1",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundForNonExistentCustomer() {
        ResponseEntity<Customer> response = restTemplate.getForEntity(
                "/customers/999",
                Customer.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() {
        CustomerRequest request = new CustomerRequest(); // Missing required fields

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/customers",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestForMissingPersonalIdNumber() {
        CustomerRequest request = new CustomerRequest()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+4712345678");
                // Missing personalIdNumber

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/customers",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestForDuplicatePersonalIdNumber() {
        CustomerRequest request = new CustomerRequest()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .personalIdNumber("12345678901") // Using existing personalIdNumber from test data
                .phoneNumber("+4712345678");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/customers",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
