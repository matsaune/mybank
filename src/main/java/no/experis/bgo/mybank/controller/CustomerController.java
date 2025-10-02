package no.experis.bgo.mybank.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.mybank.api.CustomersApi;
import no.experis.bgo.mybank.api.model.Customer;
import no.experis.bgo.mybank.api.model.CustomerRequest;
import no.experis.bgo.mybank.mapper.CustomerMapper;
import no.experis.bgo.mybank.model.CustomerEntity;
import no.experis.bgo.mybank.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Customer operations.
 * Implements the generated OpenAPI interface.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CustomerService customerService;

    @Override
    public ResponseEntity<Customer> createCustomer(CustomerRequest request) {
        log.trace("Creating customer: {}", request);
        CustomerEntity entity = CustomerMapper.toEntity(request);
        CustomerEntity savedEntity = customerService.create(entity);
        return new ResponseEntity<>(CustomerMapper.toApiModel(savedEntity), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(Long id) {
        throw new UnsupportedOperationException("Delete operation not supported");
    }

    @Override
    public ResponseEntity<List<Customer>> getAllCustomers() {
        log.trace("Getting all customers");
        List<CustomerEntity> entities = customerService.findAll();
        return ResponseEntity.ok(CustomerMapper.toApiModelList(entities));
    }

    @Override
    public ResponseEntity<Customer> getCustomerById(Long id) {
        log.trace("Getting customer with id: {}", id);
        return customerService.findById(id)
                .map(CustomerMapper::toApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Customer> updateCustomer(Long id, CustomerRequest request) {
        log.trace("Updating customer with id: {}, request: {}", id, request);
        CustomerEntity entity = CustomerMapper.toEntity(request);
        CustomerEntity updatedEntity = customerService.update(id, entity);
        return ResponseEntity.ok(CustomerMapper.toApiModel(updatedEntity));
    }
}
