---
mode: "agent"
description: 'This context file provides detailed guidance for creating Service layer implementations with business logic, following established coding patterns and conventions.'
---

# Service Layer Development Context


- **Important**: Replace `app` in all package names with your specific application name (e.g., `mybank`, `trading`, `risk`, etc.)
    - Example: `no.experis.bgo.app.service` becomes `no.experis.bgo.mybank.service` for the mybank project
    - - 
- Follow the patterns and conventions outlined below
- When done, validate that all the points under "Checklist for New Service Classes" are all checked off

## Service Class Patterns

### Service Class Structure

All Service classes in this project must follow a consistent pattern:

```java
package no.experis.bgo.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.model.EntityName;
import no.experis.bgo.app.repository.EntityNameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link EntityName} entities.
 * Provides business logic and data access operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntityNameService {

    private final EntityNameRepository entityNameRepository;
    // Add other required repositories or services here

    /**
     * Creates a new entity.
     *
     * @param entity the entity to create
     * @return the created entity
     */
    @Transactional
    public EntityName create(EntityName entity) {
        log.debug("Creating new entity: {}", entity);
        // Add business logic validation here
        EntityName savedEntity = entityNameRepository.save(entity);
        log.info("Created entity with id: {}", savedEntity.id());
        return savedEntity;
    }

    /**
     * Retrieves an entity by ID.
     *
     * @param id the entity ID
     * @return the entity if found
     */
    @Transactional(readOnly = true)
    public Optional<EntityName> findById(Long id) {
        log.debug("Finding entity by id: {}", id);
        return entityNameRepository.findById(id);
    }

    /**
     * Retrieves all entities.
     *
     * @return list of all entities
     */
    @Transactional(readOnly = true)
    public List<EntityName> findAll() {
        log.debug("Finding all entities");
        return entityNameRepository.findAll();
    }

    /**
     * Updates an existing entity.
     *
     * @param id the entity ID
     * @param entity the entity with updated data
     * @return the updated entity
     * @throws IllegalArgumentException if entity not found
     */
    @Transactional
    public EntityName update(Long id, EntityName entity) {
        log.debug("Updating entity with id: {}", id);
        
        EntityName existingEntity = entityNameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found with id: " + id));
        
        // Update fields
        existingEntity
                .field1(entity.field1())
                .field2(entity.field2());
        
        EntityName updatedEntity = entityNameRepository.save(existingEntity);
        log.info("Updated entity with id: {}", id);
        return updatedEntity;
    }

    /**
     * Deletes an entity by ID.
     *
     * @param id the entity ID
     * @throws IllegalArgumentException if entity not found
     */
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting entity with id: {}", id);
        
        if (!entityNameRepository.existsById(id)) {
            throw new IllegalArgumentException("Entity not found with id: " + id);
        }
        
        entityNameRepository.deleteById(id);
        log.info("Deleted entity with id: {}", id);
    }
}
```

### Key Conventions

- **Package**: All services are in `no.experis.bgo.app.service` (replace `app` with your application name)
- **Annotations**:
    - `@Slf4j` from Lombok for logging
    - `@Service` for Spring service component
    - `@RequiredArgsConstructor` from Lombok for dependency injection
- **Transaction Management**:
    - Use `@Transactional` for write operations
    - Use `@Transactional(readOnly = true)` for read-only operations
- **CRUD Methods**: Always implement standard CRUD operations when creating a service for a domain object:
    - `create(Entity entity)` - Create new entity
    - `findById(ID id)` - Find by primary key
    - `findAll()` - List all entities
    - `update(ID id, Entity entity)` - Update existing entity
    - `deleteById(ID id)` - Delete by primary key
- **Logging**:
    - Use `log.debug()` for method entry
    - Use `log.info()` for significant business events
    - Use `log.error()` for error scenarios
- **Error Handling**:
    - Throw `IllegalArgumentException` for invalid input or entity not found
    - Never return null, use Optional for single entity queries
- **Documentation**: Add comprehensive Javadoc for all public methods

### Service with Multiple Dependencies

```java
package no.experis.bgo.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.model.Order;
import no.experis.bgo.app.model.OrderItem;
import no.experis.bgo.app.model.OrderStatus;
import no.experis.bgo.app.repository.OrderRepository;
import no.experis.bgo.app.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link Order} entities.
 * Handles order processing and business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    /**
     * Creates a new order with items.
     *
     * @param order the order to create
     * @param items the order items
     * @return the created order
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Order createOrder(Order order, List<OrderItem> items) {
        log.debug("Creating new order with {} items", items.size());
        
        // Validate inventory
        for (OrderItem item : items) {
            if (!inventoryService.checkAvailability(item.productId(), item.quantity())) {
                throw new IllegalArgumentException("Insufficient inventory for product: " + item.productId());
            }
        }
        
        // Calculate total
        BigDecimal total = calculateTotal(items);
        order.total(total).status(OrderStatus.PENDING);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Save items
        items.forEach(item -> {
            item.orderId(savedOrder.id());
            orderItemRepository.save(item);
        });
        
        // Update inventory
        items.forEach(item -> 
            inventoryService.decreaseStock(item.productId(), item.quantity())
        );
        
        // Send notification
        notificationService.sendOrderConfirmation(savedOrder);
        
        log.info("Created order with id: {}", savedOrder.id());
        return savedOrder;
    }

    /**
     * Processes an order.
     *
     * @param orderId the order ID to process
     * @return the processed order
     * @throws IllegalArgumentException if order not found or invalid state
     */
    @Transactional
    public Order processOrder(Long orderId) {
        log.debug("Processing order: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        if (order.status() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order cannot be processed in status: " + order.status());
        }
        
        order.status(OrderStatus.PROCESSING);
        Order updatedOrder = orderRepository.save(order);
        
        notificationService.sendProcessingNotification(updatedOrder);
        
        log.info("Processed order: {}", orderId);
        return updatedOrder;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

## Integration Test Patterns

### Service Integration Test Structure

All service integration tests should use `@SpringBootTest` and test with real database using TestContainers and Flyway migrations. Use `@Sql` annotations to load test data at class level (for all tests) or method level (for specific tests).

#### Creating SQL Test Data Files

Create test data files in `src/test/resources/` with naming convention:
- Class-level data: `/test-data/{service-name}-test-data.sql`
- Method-specific data: `/test-data/{service-name}-{test-scenario}-data.sql`

Example SQL test data file:
```sql
-- /test-data/entity-name-service-test-data.sql
INSERT INTO ENTITY_NAME (ID, FIELD1, FIELD2, STATUS, CREATED, UPDATED) 
VALUES 
    (100, 'test1', 'value1', 'ACTIVE', NOW(), NOW()),
    (101, 'test2', 'value2', 'INACTIVE', NOW(), NOW()),
    (102, 'test3', 'value3', 'ACTIVE', NOW(), NOW());

-- Reset sequence if needed
ALTER SEQUENCE entity_name_id_seq RESTART WITH 200;
```

#### Integration Test Implementation

```java
package no.experis.bgo.app.service;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.FlywayTestConfiguration;
import no.experis.bgo.app.TestContainersConfiguration;
import no.experis.bgo.app.model.EntityName;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link EntityNameService}.
 * Tests service methods with real database.
 */
@Slf4j
@SpringBootTest
@Import({FlywayTestConfiguration.class, TestContainersConfiguration.class})
@Transactional
@Tag("integration-test")
@Sql(scripts = "/test-data/entity-name-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class EntityNameServiceIntegrationTest {

    @Autowired
    private EntityNameService entityNameService;

    @BeforeEach
    void setUp() {
        // Any test-specific setup if needed
    }

    @Test
    void shouldCreateEntity() {
        // Given
        EntityName entity = createTestEntity();

        // When
        EntityName result = entityNameService.create(entity);

        // Then
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(entity.field1(), result.field1());
    }

    @Test
    void shouldFindEntityById() {
        // Given - using data from class-level @Sql
        Long existingId = 100L;

        // When
        Optional<EntityName> result = entityNameService.findById(existingId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(existingId, result.get().id());
        assertEquals("test1", result.get().field1());
    }

    @Test
    void shouldReturnEmptyWhenEntityNotFound() {
        // When
        Optional<EntityName> result = entityNameService.findById(999L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllEntities() {
        // When - using data from class-level @Sql
        List<EntityName> results = entityNameService.findAll();

        // Then
        assertFalse(results.isEmpty());
        assertTrue(results.size() >= 3); // At least the 3 from test data
    }

    @Test
    void shouldUpdateEntity() {
        // Given - using data from class-level @Sql
        Long existingId = 100L;
        EntityName updateData = new EntityName()
                .field1("updated")
                .field2("newValue");

        // When
        EntityName result = entityNameService.update(existingId, updateData);

        // Then
        assertEquals("updated", result.field1());
        assertEquals("newValue", result.field2());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentEntity() {
        // Given
        EntityName updateData = createTestEntity();

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> entityNameService.update(999L, updateData));
    }

    @Test
    void shouldDeleteEntity() {
        // Given - using data from class-level @Sql
        Long existingId = 101L;

        // When
        entityNameService.deleteById(existingId);

        // Then
        Optional<EntityName> result = entityNameService.findById(existingId);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentEntity() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> entityNameService.deleteById(999L));
    }

    @Test
    @Sql("/test-data/entity-name-complex-scenario-data.sql")
    void shouldHandleComplexBusinessScenario() {
        // Test with additional method-specific test data
        List<EntityName> results = entityNameService.findAll();
        
        assertFalse(results.isEmpty());
        // Add specific assertions based on complex scenario test data
    }

    private EntityName createTestEntity() {
        return new EntityName()
                .field1("testValue1")
                .field2("testValue2");
    }
}
```

## Mock Test Patterns

### Service Mock Test Structure

When a service depends on other services, create a separate mock test using `@MockitoBean`:

```java
package no.experis.bgo.app.service;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.model.Order;
import no.experis.bgo.app.model.OrderItem;
import no.experis.bgo.app.model.OrderStatus;
import no.experis.bgo.app.repository.OrderRepository;
import no.experis.bgo.app.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderService}.
 * Tests service methods with mocked dependencies.
 */
@Slf4j
@SpringBootTest
class OrderServiceMockTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Any additional setup if needed
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        Order order = createTestOrder();
        List<OrderItem> items = createTestOrderItems();
        
        when(inventoryService.checkAvailability(anyString(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order.id(1L));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(inventoryService).decreaseStock(anyString(), anyInt());
        doNothing().when(notificationService).sendOrderConfirmation(any(Order.class));

        // When
        Order result = orderService.createOrder(order, items);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(OrderStatus.PENDING, result.status());
        
        // Verify interactions
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(items.size())).save(any(OrderItem.class));
        verify(inventoryService, times(items.size())).checkAvailability(anyString(), anyInt());
        verify(inventoryService, times(items.size())).decreaseStock(anyString(), anyInt());
        verify(notificationService, times(1)).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenInventoryInsufficient() {
        // Given
        Order order = createTestOrder();
        List<OrderItem> items = createTestOrderItems();
        
        when(inventoryService.checkAvailability(anyString(), anyInt())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> orderService.createOrder(order, items));
        
        // Verify no save operations occurred
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
        verify(notificationService, never()).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void shouldProcessOrderSuccessfully() {
        // Given
        Long orderId = 1L;
        Order order = createTestOrder()
                .id(orderId)
                .status(OrderStatus.PENDING);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(notificationService).sendProcessingNotification(any(Order.class));

        // When
        Order result = orderService.processOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.status());
        
        // Verify interactions
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(1)).sendProcessingNotification(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> orderService.processOrder(orderId));
        
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderInInvalidStatus() {
        // Given
        Long orderId = 1L;
        Order order = createTestOrder()
                .id(orderId)
                .status(OrderStatus.COMPLETED);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> orderService.processOrder(orderId));
        
        verify(orderRepository, never()).save(any(Order.class));
        verify(notificationService, never()).sendProcessingNotification(any(Order.class));
    }

    private Order createTestOrder() {
        return new Order()
                .customerId("CUST123")
                .total(BigDecimal.valueOf(100.00));
    }

    private List<OrderItem> createTestOrderItems() {
        return List.of(
                new OrderItem()
                        .productId("PROD1")
                        .quantity(2)
                        .price(BigDecimal.valueOf(25.00)),
                new OrderItem()
                        .productId("PROD2")
                        .quantity(1)
                        .price(BigDecimal.valueOf(50.00))
        );
    }
}
```

## Business Logic Patterns

### Validation and Error Handling

```java
@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Transfers money between accounts.
     *
     * @param fromAccountId source account ID
     * @param toAccountId destination account ID
     * @param amount transfer amount
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if business rules are violated
     */
    @Transactional
    public void transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        log.debug("Transferring {} from account {} to account {}", amount, fromAccountId, toAccountId);
        
        // Validation
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }
        
        // Load accounts
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        
        // Business rules
        if (!fromAccount.active()) {
            throw new IllegalStateException("Source account is not active");
        }
        
        if (!toAccount.active()) {
            throw new IllegalStateException("Destination account is not active");
        }
        
        if (fromAccount.balance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        // Perform transfer
        fromAccount.balance(fromAccount.balance().subtract(amount));
        toAccount.balance(toAccount.balance().add(amount));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        log.info("Successfully transferred {} from account {} to account {}", 
                amount, fromAccountId, toAccountId);
    }
}
```

## Required Dependencies

When creating new service classes and tests, ensure these dependencies are available:

```java
// Spring Framework
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

// Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Testing - Integration
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.context.annotation.Import;

// Testing - Mocking with @MockitoBean
import org.springframework.boot.test.mock.mockito.MockitoBean;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

// Assertions
import static org.junit.jupiter.api.Assertions.*;
```

## Checklist for New Service Classes

When creating a new service class, ensure all items are completed:

### Service Class Creation
- [ ] Create service class in `no.experis.bgo.app.service` package (replace `app` with your application name)
- [ ] Add required annotations: `@Slf4j`, `@Service`, `@RequiredArgsConstructor`
- [ ] Inject repository dependencies using final fields
- [ ] Inject other service dependencies if needed
- [ ] Implement CRUD methods: create, findById, findAll, update, deleteById
- [ ] Add `@Transactional` annotations appropriately
- [ ] Use proper logging levels (debug for entry, info for business events)
- [ ] Handle errors with appropriate exceptions
- [ ] Return Optional for single entity queries
- [ ] Add comprehensive Javadoc for all public methods
- [ ] **Never use hyphens (-) or underscores (_) in test method names**

### Business Logic Implementation
- [ ] Validate input parameters
- [ ] Check business rules and constraints
- [ ] Handle entity state transitions correctly
- [ ] Implement any domain-specific operations
- [ ] Ensure transactional consistency
- [ ] Add audit logging for important operations

### Integration Test Creation
- [ ] Create `{ServiceName}IntegrationTest` class
- [ ] Add `@SpringBootTest` annotation
- [ ] Add `@Import({FlywayTestConfiguration.class, TestContainersConfiguration.class})`
- [ ] Add `@Transactional` annotation for test rollback
- [ ] Add `@Tag("integration-test")` annotation
- [ ] Create SQL test data files in `/test-data/` directory
- [ ] Add `@Sql` annotations at class or method level as needed
- [ ] Test all CRUD operations
- [ ] Test successful scenarios
- [ ] Test error scenarios and exceptions
- [ ] Verify expected outcomes with assertions

### Mock Test Creation (if service has other service dependencies)
- [ ] Create `{ServiceName}MockTest` class
- [ ] Use `@SpringBootTest` annotation
- [ ] Mock all dependencies with `@MockitoBean`
- [ ] Use `@Autowired` for service under test
- [ ] Test business logic in isolation
- [ ] Verify correct interactions with mocked dependencies
- [ ] Test exception scenarios
- [ ] Use ArgumentMatchers for flexible verification
- [ ] Assert on method return values
- [ ] Verify number of invocations

### SQL Test Data Files
- [ ] Create test data files in `src/test/resources/test-data/`
- [ ] Use naming convention: `{service-name}-test-data.sql`
- [ ] Include INSERT statements with test data
- [ ] Reset sequences if needed
- [ ] Add comments to explain test scenarios
- [ ] Use consistent test data IDs (e.g., 100+ for test data)

### Code Quality
- [ ] Follow naming conventions consistently
- [ ] Ensure methods are focused and single-purpose
- [ ] Extract complex logic to private methods
- [ ] Use meaningful variable and method names
- [ ] Add logging at appropriate points
- [ ] Handle null values appropriately
- [ ] Follow DRY principle (Don't Repeat Yourself)
- [ ] Ensure thread safety if needed

### Performance Considerations
- [ ] Use `@Transactional(readOnly = true)` for read operations
- [ ] Consider pagination for list operations
- [ ] Use batch operations for bulk updates
- [ ] Implement caching if appropriate
- [ ] Optimize database queries
- [ ] Avoid N+1 query problems

### Documentation
- [ ] Document service class purpose
- [ ] Add Javadoc for all public methods
- [ ] Document complex business logic
- [ ] Include @param and @return tags
- [ ] Document thrown exceptions
- [ ] Add examples in Javadoc where helpful

---

This context file provides comprehensive guidance for creating Service layer implementations following established patterns in this project. All generated code should follow these conventions for consistency and maintainability.