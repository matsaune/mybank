package no.experis.bgo.mybank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.mybank.model.CustomerEntity;
import no.experis.bgo.mybank.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link CustomerEntity} entities.
 * Provides business logic and data access operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Creates a new customer.
     *
     * @param customer the customer to create
     * @return the created customer
     * @throws IllegalArgumentException if a customer with the same email or personal ID number already exists
     */
    @Transactional
    public CustomerEntity create(CustomerEntity customer) {
        log.debug("Creating new customer: {}", customer);

        validateUniqueFields(customer);

        CustomerEntity savedCustomer = customerRepository.save(customer);
        log.info("Created customer with id: {}", savedCustomer.id());
        return savedCustomer;
    }

    /**
     * Retrieves a customer by ID.
     *
     * @param id the customer ID
     * @return the customer if found
     */
    @Transactional(readOnly = true)
    public Optional<CustomerEntity> findById(Long id) {
        log.debug("Finding customer by id: {}", id);
        return customerRepository.findById(id);
    }

    /**
     * Retrieves all customers.
     *
     * @return list of all customers
     */
    @Transactional(readOnly = true)
    public List<CustomerEntity> findAll() {
        log.debug("Finding all customers");
        return customerRepository.findAll();
    }

    /**
     * Updates an existing customer.
     *
     * @param id the customer ID
     * @param customer the customer with updated data
     * @return the updated customer
     * @throws IllegalArgumentException if customer not found or if unique constraints are violated
     */
    @Transactional
    public CustomerEntity update(Long id, CustomerEntity customer) {
        log.debug("Updating customer with id: {}", id);

        CustomerEntity existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));

        // Only validate unique fields if they have changed
        if (!customer.email().equals(existingCustomer.email()) ||
            !customer.personalIdNumber().equals(existingCustomer.personalIdNumber())) {
            validateUniqueFields(customer);
        }

        existingCustomer
                .firstName(customer.firstName())
                .lastName(customer.lastName())
                .email(customer.email())
                .personalIdNumber(customer.personalIdNumber())
                .phoneNumber(customer.phoneNumber());

        CustomerEntity updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Updated customer with id: {}", updatedCustomer.id());
        return updatedCustomer;
    }

    /**
     * Finds a customer by email address.
     *
     * @param email the email address to search for
     * @return the customer if found
     */
    @Transactional(readOnly = true)
    public Optional<CustomerEntity> findByEmail(String email) {
        log.debug("Finding customer by email: {}", email);
        return customerRepository.findByEmail(email);
    }

    /**
     * Finds a customer by personal ID number.
     *
     * @param personalIdNumber the personal ID number to search for
     * @return the customer if found
     */
    @Transactional(readOnly = true)
    public Optional<CustomerEntity> findByPersonalIdNumber(String personalIdNumber) {
        log.debug("Finding customer by personal ID number: {}", personalIdNumber);
        return customerRepository.findByPersonalIdNumber(personalIdNumber);
    }

    private void validateUniqueFields(CustomerEntity customer) {
        customerRepository.findByEmail(customer.email()).ifPresent(existing -> {
            if (!existing.id().equals(customer.id())) {
                throw new IllegalArgumentException("Email already exists: " + customer.email());
            }
        });

        customerRepository.findByPersonalIdNumber(customer.personalIdNumber()).ifPresent(existing -> {
            if (!existing.id().equals(customer.id())) {
                throw new IllegalArgumentException("Personal ID number already exists: " + customer.personalIdNumber());
            }
        });
    }
}
