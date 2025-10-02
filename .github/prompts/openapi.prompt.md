---
mode: "agent"
description: 'This context file provides detailed guidance for creating REST services based on OpenAPI specifications, following established coding patterns and conventions.'
---

# OpenAPI REST Service Development Context

## Package Naming
- **Important**: Replace `app` in all package names with your specific application name (e.g., `mybank`, `trading`, `risk`, etc.)
- Example: `no.experis.bgo.app.controller` becomes `no.experis.bgo.mybank.controller` for the mybank project
- Follow these conventions to ensure consistency and maintainability across all services in this project

## General Rules
- Don't create code not explicitly asked for, such as:
    - Custom exception classes
    - Utility classes
- When generating code from OpenAPI specifications, always follow the patterns and conventions outlined in this document
- Never deviate from these unless explicitly instructed to do so
- Never create custom utility classes or exception classes unless explicitly requested

## Exception Handling
- Never create custom Exception classes for REST controllers
- Instead, use the standard exceptions provided by Java and Spring, such as:
    - `IllegalArgumentException`
    - `UnsupportedOperationException`

## OpenAPI Specifications
- Always use the provided OpenAPI specification files for code generation
- Never create or modify OpenAPI specifications unless explicitly instructed to do so
- Never change the OpenAPI specification file name, content, or location, unless explicitly told to do so

## REST Controllers
- Create REST controllers that implement the generated OpenAPI interfaces, following the provided patterns
- Always also create a ControllerWebIntegrationTest as specified in this document

## OpenAPI Code Generation Setup

### Build Configuration (build.gradle.kts)

When adding a new OpenAPI specification, configure the code generation in your `build.gradle.kts`:

```kotlin
tasks {
    register<GenerateTask>("openApiGenerate{YourApiName}Api") {
        generatorName.set("spring")
        inputSpec.set("$rootDir/src/main/resources/api/{your_api_spec}.yaml")
        outputDir.set("$buildDir/generated")
        apiPackage.set("no.experis.bgo.app.{api_name}.api")
        modelPackage.set("no.experis.bgo.app.{api_name}.model")
        configOptions.set(mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "requestMappingMode" to "api_interface",
            "useSwaggerUI" to "true",
            "delegatePattern" to "false",
            "useSpringBoot3" to "true"
        ))
    }

    val openApiGenerate{YourApiName}Api by existing

    compileJava {
        dependsOn(openApiGenerate{YourApiName}Api)
    }

    withType<io.freefair.gradle.plugins.lombok.tasks.LombokTask>().configureEach {
        mustRunAfter(openApiGenerate{YourApiName}Api)
    }
}
```

### Key Configuration Options

1. **interfaceOnly**: `true` - Generates only interfaces, not implementations
2. **skipDefaultInterface**: `true` - Skips default interface methods
3. **useSpringBoot3**: `true` - Uses Spring Boot 3 annotations
4. **delegatePattern**: `false` - Disables delegate pattern
5. **dateLibrary**: `java8` - Uses Java 8 time API

## REST Controller Patterns

### Controller Class Structure

All REST controllers in this project follow a consistent pattern:

```java
package no.experis.bgo.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.{api_name}.api.{GeneratedApiInterface};
import no.experis.bgo.app.{api_name}.model.*;
import no.experis.bgo.app.service.{RelatedService};
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for {description}.
 * Implements the generated OpenAPI interface.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class {YourApiName}Controller implements {GeneratedApiInterface} {

    private final {RelatedService} {relatedService};

    @Override
    public ResponseEntity<{ResponseType}> {methodName}({parameters}) {
        log.trace("Received: {}", {parameters});
        
        // TODO: Implement method logic
        throw new UnsupportedOperationException("Method not yet implemented");
    }
}
```

### Key Conventions

- **Package**: All controllers are in `no.experis.bgo.app.controller` (replace `app` with your application name)
- **Annotations**:
    - `@Slf4j` from Lombok for logging
    - `@RestController` for Spring REST controller
    - `@RequiredArgsConstructor` from Lombok for dependency injection
- **Interface Implementation**: Controllers must implement the generated OpenAPI interface
- **Logging**: Use `log.trace()` for method entry with parameters
- **Default Implementation**: All methods should throw `UnsupportedOperationException` initially
- **Documentation**: Add comprehensive Javadoc for the class

### Complete Controller Example

```java
package com.acme.reporting.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.acme.reporting.connector.api.ConnectorApi;
import com.acme.reporting.connector.model.*;
import com.acme.reporting.service.DataSyncService;
import com.acme.reporting.repository.IssueRepository;
import com.acme.reporting.repository.SyncStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Data Connector API.
 * Provides endpoints for managing sync status, records, and workflow actions.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DataConnectorController implements ConnectorApi {

    private final DataSyncService dataSyncService;
    private final SyncStatusRepository syncStatusRepository;
    private final IssueRepository issueRepository;

    @Override
    public ResponseEntity<List<Record>> getDatasetRecords(String datasetKey, String filter, String orderBy, Integer offset, Integer limit) {
        log.trace("Received: datasetKey={}, filter={}, orderBy={}, offset={}, limit={}", datasetKey, filter, orderBy, offset, limit);

        List<Record> recordList = switch (datasetKey) {
            case "invoices" -> {
                List<Invoice> invoiceList = dataSyncService.getInvoices();
                yield InvoiceRecordMapper.mapRecords(invoiceList);
            }
            case "issues" -> {
                List<Issue> issueList = issueRepository.findAll();
                yield IssueRecordMapper.mapRecords(issueList);
            }
            default -> throw new IllegalArgumentException("No dataset defined for: " + datasetKey);
        };

        return ResponseEntity.ok(recordList);
    }

    @Override
    public ResponseEntity<ConnectorConfig> getConnectorConfiguration() {
        log.trace("Getting connector configuration");

        ConnectorConfig config = new ConnectorConfig()
                .connector("acme-invoice-sync")
                .recordType("Invoices")
                .features(List.of(/* features */))
                .availableStatuses(List.of(/* statuses */))
                .standardFields(List.of(/* fields */));

        return ResponseEntity.ok(config);
    }

    @Override
    public ResponseEntity<Void> executeWorkflowAction(String actionKey, Object payload) {
        log.trace("Received workflow action: actionKey={}", actionKey);

        // TODO: Implement action logic
        throw new UnsupportedOperationException("Action not yet implemented: " + actionKey);
    }
}
```

## Mapper Classes

### Purpose and Usage

Mapper classes are used to convert between:
- **API Models** (generated from OpenAPI specifications) → **Domain Entities** (JPA entities)
- **Domain Entities** → **API Models** (for responses)

Always create mapper classes when you need to transform data between the API layer and the domain/persistence layer.

### Mapper Class Structure

All mapper classes follow a consistent pattern:

```java
package no.experis.bgo.app.mapper;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.{api_name}.model.{ApiModel};
import no.experis.bgo.app.entity.{DomainEntity};

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between {ApiModel} (API layer) and {DomainEntity} (domain layer).
 */
@Slf4j
public class {EntityName}Mapper {

    /**
     * Converts a domain entity to an API model.
     *
     * @param entity the domain entity
     * @return the API model representation
     */
    public static {ApiModel} toApiModel({DomainEntity} entity) {
        if (entity == null) {
            return null;
        }

        return new {ApiModel}()
                .id(entity.getId())
                .field1(entity.getField1())
                .field2(entity.getField2())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
    }

    /**
     * Converts an API model to a domain entity.
     *
     * @param apiModel the API model
     * @return the domain entity
     */
    public static {DomainEntity} toEntity({ApiModel} apiModel) {
        if (apiModel == null) {
            return null;
        }

        {DomainEntity} entity = new {DomainEntity}();
        entity.setId(apiModel.getId());
        entity.setField1(apiModel.getField1());
        entity.setField2(apiModel.getField2());
        
        return entity;
    }

    /**
     * Updates an existing domain entity with data from an API model.
     * Only updates fields that are present in the API model.
     *
     * @param entity the existing domain entity to update
     * @param apiModel the API model containing new data
     * @return the updated entity
     */
    public static {DomainEntity} updateEntity({DomainEntity} entity, {ApiModel} apiModel) {
        if (entity == null || apiModel == null) {
            return entity;
        }

        if (apiModel.getField1() != null) {
            entity.setField1(apiModel.getField1());
        }
        if (apiModel.getField2() != null) {
            entity.setField2(apiModel.getField2());
        }

        return entity;
    }

    /**
     * Converts a list of domain entities to a list of API models.
     *
     * @param entities the list of domain entities
     * @return the list of API models
     */
    public static List<{ApiModel}> toApiModelList(List<{DomainEntity}> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map({EntityName}Mapper::toApiModel)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of API models to a list of domain entities.
     *
     * @param apiModels the list of API models
     * @return the list of domain entities
     */
    public static List<{DomainEntity}> toEntityList(List<{ApiModel}> apiModels) {
        if (apiModels == null) {
            return List.of();
        }

        return apiModels.stream()
                .map({EntityName}Mapper::toEntity)
                .collect(Collectors.toList());
    }
}
```

### Key Conventions

- **Package**: All mappers are in `no.experis.bgo.app.mapper` (replace `app` with your application name)
- **Naming**: `{EntityName}Mapper` (e.g., `InvoiceMapper`, `CustomerMapper`)
- **Static Methods**: All mapper methods are static utility methods
- **Null Safety**: Always check for null inputs and return appropriate values
- **Annotations**: Use `@Slf4j` for logging when needed
- **Method Naming**:
    - `toApiModel()` - converts entity to API model
    - `toEntity()` - converts API model to entity
    - `toApiModelList()` - converts list of entities to API models
    - `toEntityList()` - converts list of API models to entities

### Complete Mapper Example

```java
package com.acme.reporting.mapper;

import lombok.extern.slf4j.Slf4j;
import com.acme.reporting.invoice.model.Invoice;
import com.acme.reporting.invoice.model.InvoiceStatus;
import com.acme.reporting.entity.InvoiceEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Invoice (API layer) and InvoiceEntity (domain layer).
 */
@Slf4j
public class InvoiceMapper {

    /**
     * Converts an InvoiceEntity to an Invoice API model.
     *
     * @param entity the invoice entity
     * @return the invoice API model
     */
    public static Invoice toApiModel(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Invoice()
                .invoiceId(entity.getInvoiceId())
                .invoiceNumber(entity.getInvoiceNumber())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .status(InvoiceStatus.fromValue(entity.getStatus()))
                .issueDate(entity.getIssueDate())
                .dueDate(entity.getDueDate())
                .paidDate(entity.getPaidDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
    }

    /**
     * Converts an Invoice API model to an InvoiceEntity.
     *
     * @param invoice the invoice API model
     * @return the invoice entity
     */
    public static InvoiceEntity toEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceEntity entity = new InvoiceEntity();
        entity.setInvoiceId(invoice.getInvoiceId());
        entity.setInvoiceNumber(invoice.getInvoiceNumber());
        entity.setCustomerId(invoice.getCustomerId());
        entity.setCustomerName(invoice.getCustomerName());
        entity.setAmount(invoice.getAmount());
        entity.setCurrency(invoice.getCurrency());
        entity.setStatus(invoice.getStatus() != null ? invoice.getStatus().getValue() : null);
        entity.setIssueDate(invoice.getIssueDate());
        entity.setDueDate(invoice.getDueDate());
        entity.setPaidDate(invoice.getPaidDate());

        return entity;
    }
    
    /**
     * Converts a list of InvoiceEntity to a list of Invoice API models.
     *
     * @param entities the list of invoice entities
     * @return the list of invoice API models
     */
    public static List<Invoice> toApiModelList(List<InvoiceEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(InvoiceMapper::toApiModel)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of Invoice API models to a list of InvoiceEntity.
     *
     * @param invoices the list of invoice API models
     * @return the list of invoice entities
     */
    public static List<InvoiceEntity> toEntityList(List<Invoice> invoices) {
        if (invoices == null) {
            return List.of();
        }

        return invoices.stream()
                .map(InvoiceMapper::toEntity)
                .collect(Collectors.toList());
    }
}
```

### Using Mappers in Controllers

Controllers should use mapper classes to convert between API models and domain entities:

```java
@RestController
@RequiredArgsConstructor
public class InvoiceController implements InvoiceApi {

    private final InvoiceService invoiceService;

    @Override
    public ResponseEntity<Invoice> getInvoice(UUID invoiceId) {
        log.trace("Received: invoiceId={}", invoiceId);
        
        InvoiceEntity entity = invoiceService.findById(invoiceId);
        Invoice apiModel = InvoiceMapper.toApiModel(entity);
        
        return ResponseEntity.ok(apiModel);
    }

    @Override
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        log.trace("Getting all invoices");
        
        List<InvoiceEntity> entities = invoiceService.findAll();
        List<Invoice> apiModels = InvoiceMapper.toApiModelList(entities);
        
        return ResponseEntity.ok(apiModels);
    }

    @Override
    public ResponseEntity<Invoice> createInvoice(Invoice invoice) {
        log.trace("Received: invoice={}", invoice);
        
        InvoiceEntity entity = InvoiceMapper.toEntity(invoice);
        InvoiceEntity savedEntity = invoiceService.save(entity);
        Invoice apiModel = InvoiceMapper.toApiModel(savedEntity);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiModel);
    }
}
```

### Mapper Best Practices

- **Immutability**: API models (from OpenAPI) are often immutable with builder pattern, entities are mutable POJOs
- **Null Handling**: Always check for null inputs and return appropriate defaults
- **Field Mapping**: Map all relevant fields between models, handle type conversions (e.g., enum values)
- **Timestamps**: Don't map timestamps from API model to entity during creation - let the database handle these
- **Collections**: Provide list conversion methods for batch operations
- **Documentation**: Document all methods with clear Javadoc
- **No Business Logic**: Mappers should only perform data transformation, no business logic
- **Enum Handling**: Convert between API enums and entity string/enum values appropriately
- **Validation**: Don't add validation in mappers - handle this in controllers or service layer

### Mapper Testing

Create unit tests for mapper classes:

```java
package no.experis.bgo.app.mapper;

import com.acme.reporting.invoice.model.Invoice;
import com.acme.reporting.invoice.model.InvoiceStatus;
import com.acme.reporting.entity.InvoiceEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceMapperTest {

    @Test
    void shouldMapEntityToApiModel() {
        InvoiceEntity entity = createTestEntity();

        Invoice apiModel = InvoiceMapper.toApiModel(entity);

        assertNotNull(apiModel);
        assertEquals(entity.getInvoiceId(), apiModel.getInvoiceId());
        assertEquals(entity.getInvoiceNumber(), apiModel.getInvoiceNumber());
        assertEquals(entity.getAmount(), apiModel.getAmount());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        Invoice apiModel = InvoiceMapper.toApiModel(null);
        assertNull(apiModel);
    }

    @Test
    void shouldMapApiModelToEntity() {
        Invoice apiModel = createTestApiModel();

        InvoiceEntity entity = InvoiceMapper.toEntity(apiModel);

        assertNotNull(entity);
        assertEquals(apiModel.getInvoiceId(), entity.getInvoiceId());
        assertEquals(apiModel.getInvoiceNumber(), entity.getInvoiceNumber());
        assertEquals(apiModel.getAmount(), entity.getAmount());
    }
    

    @Test
    void shouldMapEntityListToApiModelList() {
        List<InvoiceEntity> entities = List.of(createTestEntity(), createTestEntity());

        List<Invoice> apiModels = InvoiceMapper.toApiModelList(entities);

        assertNotNull(apiModels);
        assertEquals(2, apiModels.size());
    }

    private InvoiceEntity createTestEntity() {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setInvoiceId(UUID.randomUUID());
        entity.setInvoiceNumber("INV-2024-001");
        entity.setCustomerId(UUID.randomUUID());
        entity.setCustomerName("Test Customer");
        entity.setAmount(new BigDecimal("1000.00"));
        entity.setCurrency("USD");
        entity.setStatus("PENDING");
        entity.setIssueDate(LocalDate.now());
        entity.setDueDate(LocalDate.now().plusDays(30));
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private Invoice createTestApiModel() {
        return new Invoice()
                .invoiceId(UUID.randomUUID())
                .invoiceNumber("INV-2024-001")
                .customerId(UUID.randomUUID())
                .customerName("Test Customer")
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .status(InvoiceStatus.PENDING)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30));
    }
}
```

### When to Create Mappers

Always Create mapper classes in controller classes: 

1. Converting between OpenAPI-generated models and JPA entities
2. The API model and domain model have different structures
3. You need to transform data types (e.g., enums, dates, nested objects)
4. You want to keep the API layer separate from the persistence layer



## Exception Handling

Never create custom Exception classes for REST controllers. Instead, use the standard exceptions provided by java and Spring, such as `IllegalArgumentException`, `UnsupportedOperationException`

### Custom Exception Handler

All projects should have a global exception handler create this if it does not exist. This handler will catch exceptions thrown by REST controllers and return appropriate HTTP responses:
The ExceptionHandler class should be placed in the `no.experis.bgo.app.controller` package (replace `app` with your application name).
The class should be named `CustomExceptionHandler` and should implement the following methods in code below:
It uses Error objects from Star API

```java
package no.experis.bgo.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import no.experis.bgo.app.star.model.Error;

/**
 * Global exception handler for REST controllers.
 */
@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

   @ExceptionHandler(UnsupportedOperationException.class)
   public ResponseEntity<Error> unsupportedOperationExceptionHandler(UnsupportedOperationException exception) {
      log.error("Caught exception handling request, type={}",exception.getClass(),exception);
      Error error = new Error()
              .permanent(false)
              .description("Operation not currently implemented");

      return new ResponseEntity<>(error,HttpStatus.NOT_IMPLEMENTED);
   }


   @ExceptionHandler(Exception.class)
   public ResponseEntity<String> defaultExceptionHandler(Exception exception) {
      log.error("Caught exception handling request, type={}",exception.getClass(),exception);
      return new ResponseEntity<>("Caught Exception: "+exception.getClass(),HttpStatus.INTERNAL_SERVER_ERROR);
   }

   @ExceptionHandler(FileAlreadyExistsException.class)
   public ResponseEntity<Error> fileAlreadyExistsExceptionHandler(FileAlreadyExistsException exception) {
      log.error("Caught exception handling request, type={}",exception.getClass(),exception);
      Error error = new Error()
              .permanent(true)
              .description("FileAlreadyExistsException: FileName="+exception.getFile())
              .temporaryMessage("Can use force=true to override");

      return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
   }

   @ExceptionHandler(IllegalArgumentException.class)
   public ResponseEntity<Error> illegalArgumentExceptionHandler(IllegalArgumentException exception) {
      log.error("Caught exception handling request, type={}",exception.getClass(),exception);
      Error error = new Error()
              .permanent(false)
              .description(exception.getMessage());

      return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
   }

   @ExceptionHandler(ConversionException.class)
   public ResponseEntity<Error> conversionExceptionExceptionHandler(ConversionException exception) {
      log.error("Caught exception handling request, type={}",exception.getClass(),exception);
      Error error = new Error()
              .permanent(false)
              .description(exception.getMessage());

      return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
   }


   /**
    * Handles ConstraintViolationException thrown by validation annotations.
    * @param e the exception
    * @return ResponseEntity with 400 Bad Request status
    */
   @ExceptionHandler(ConstraintViolationException.class)
   public ResponseEntity<Error> handleConstraintViolationException(ConstraintViolationException e) {
      log.error("Caught exception handling request, type={}",exception.getClass(),exception);
      Error error = new Error()
              .permanent(false)
              .description(exception.getMessage());      
      return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
   }

   /**
    * Handles HttpRequestMethodNotSupportedException thrown by Spring when unsupported HTTP method is used.
    * @param e the exception
    * @return ResponseEntity with 405 Method Not Allowed status
    */
   @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
   public ResponseEntity<Error> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
      log.error("Caught exception handling request, type={}",exception.getClass(),exception);
      Error error = new Error()
              .permanent(false)
              .description(exception.getMessage());
      return new ResponseEntity<>(error,HttpStatus.METHOD_NOT_ALLOWED);
   }


}
```

## Test Patterns

### REST Controller Integration Tests

All REST controllers must have comprehensive integration tests:

```java
package no.experis.bgo.app.controller;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.FlywayTestConfiguration;
import no.experis.bgo.app.TestContainersConfiguration;
import no.experis.bgo.app.{api_name}.model.*;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {YourApiName}Controller.
 */
@Slf4j
@Import({FlywayTestConfiguration.class, TestContainersConfiguration.class, CustomExceptionHandler.class})
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/no/experis/bgo/app/controller/{your-api-name}-testdata.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class {YourApiName}ControllerWebIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldGetResourcesSuccessfully() {
        ResponseEntity<List<{ResponseType}>> response = restTemplate.exchange(
                "/your-api/resources",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        log.debug("Response: {}", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    public void shouldCreateResourceSuccessfully() {
        {RequestType} request = new {RequestType}()
                .field1("value1")
                .field2("value2");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<{RequestType}> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<{ResponseType}> response = restTemplate.exchange(
                "/your-api/resources",
                HttpMethod.POST,
                requestEntity,
                {ResponseType}.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("value1", response.getBody().getField1());
    }

    @Test
    public void shouldGetResourceByIdSuccessfully() {
        UUID resourceId = UUID.randomUUID();
        
        ResponseEntity<{ResponseType}> response = restTemplate.getForEntity(
                "/your-api/resources/" + resourceId,
                {ResponseType}.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void shouldUpdateResourceSuccessfully() {
        UUID resourceId = UUID.randomUUID();
        {RequestType} request = new {RequestType}()
                .field1("updatedValue");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<{RequestType}> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<{ResponseType}> response = restTemplate.exchange(
                "/your-api/resources/" + resourceId,
                HttpMethod.PUT,
                requestEntity,
                {ResponseType}.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void shouldDeleteResourceSuccessfully() {
        UUID resourceId = UUID.randomUUID();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/your-api/resources/" + resourceId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnNotFoundForNonExistentResource() {
        UUID nonExistentId = UUID.randomUUID();

        ResponseEntity<{ResponseType}> response = restTemplate.getForEntity(
                "/your-api/resources/" + nonExistentId,
                {ResponseType}.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void shouldReturnBadRequestForInvalidInput() {
        {RequestType} invalidRequest = new {RequestType}(); // Missing required fields

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<{RequestType}> requestEntity = new HttpEntity<>(invalidRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/your-api/resources",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldReturnNotImplementedForUnsupportedOperation() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/your-api/unsupported-operation",
                HttpMethod.POST,
                null,
                String.class
        );

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }
}
```

### Test Conventions

1. **Package**: All controller tests are in `no.experis.bgo.app.controller` (replace `app` with your application name)
2. **Naming**: `{ControllerName}WebIntegrationTest`
3. **Annotations**:
    - `@Slf4j` for logging
    - `@Tag("integration-test")` for test categorization
    - `@Import` for test configurations
    - `@AutoConfigureMockMvc` for web layer testing
    - `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` for full integration testing
    - `@Sql` for test data setup
4. **Test Coverage**:
    - Test all HTTP methods (GET, POST, PUT, DELETE)
    - Test successful scenarios (2xx responses)
    - Test error scenarios (4xx, 5xx responses)
    - Test validation and edge cases
5. **Test Method Naming**: Use camelCase with descriptive names starting with "should"

## API Documentation

### OpenAPI Specification Guidelines

When creating OpenAPI specifications:

1. **File Location**: Place YAML files in `src/main/resources/api/`
2. **Naming Convention**: Use descriptive names like `{service-name}_api.yaml`
3. **Version**: Include version in the specification
4. **Documentation**: Add comprehensive descriptions for all endpoints, parameters, and models
5. **Examples**: Include request/response examples
6. **Error Responses**: Define standard error response schemas

### Example OpenAPI Specification Structure

```yaml
openapi: 3.0.3
info:
  title: Your Service API
  description: API for managing your service resources
  version: 1.0.0
  contact:
    name: Your Team
    email: your-team@company.com

servers:
  - url: /your-api
    description: Your API base path

paths:
  /resources:
    get:
      summary: Get all resources
      description: Retrieves a list of all resources
      operationId: getAllResources
      parameters:
        - name: search
          in: query
          description: Filter search result
          required: false
          schema:
            type: string
        - name: sort
          in: query
          description: Sort search result by field
          required: false
          schema:
            type: string
      responses:
        '200':
          description: Successful operation
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Resource'
        '400':
          description: Invalid operation
        '404':
          description: Not found

components:
  schemas:
    Resource:
      type: object
      required:
        - id
        - name
      properties:
        id:
          type: string
          format: uuid
          description: Unique identifier
        name:
          type: string
          description: Resource name
        created:
          type: string
          format: date-time
          description: Creation timestamp
```

## Best Practices

### Code Quality

- **Logging**: Always use SLF4J with appropriate log levels
- **Documentation**: Add Javadoc for all public methods and classes
- **Error Handling**: Handle exceptions appropriately, never swallow them silently
- **Validation**: Validate input parameters and request bodies
- **Performance**: Use appropriate HTTP status codes and response types

### Security Considerations

- **Input Validation**: Always validate and sanitize input data
- **Error Messages**: Don't expose sensitive information in error messages
- **Authentication**: Implement appropriate authentication and authorization
- **CORS**: Configure CORS policies appropriately

### Testing

- **Test Coverage**: Aim for high test coverage of all endpoints
- **Integration Tests**: Test the complete request-response cycle
- **Error Scenarios**: Test all error conditions and edge cases
- **Performance**: Include performance tests for critical endpoints

---

This context file provides comprehensive guidance for creating OpenAPI-based REST services following the established patterns in this project. All generated code should follow these conventions for consistency and maintainability