---
applyTo: '**/*.java'
description: 'General instructions for java code in Java projects'
---
# Java Code Instructions

When writing Java code, please adhere to the following guidelines to ensure consistency, readability, and maintainability across the project:

## General Guidelines
- Never change existing code unless explicitly instructed to do so
- Never create custom utility classes or exception classes unless explicitly requested
- Never create custom Exception classes. Use standard Java/Spring exceptions: `IllegalArgumentException`, `UnsupportedOperationException`, `IllegalStateException`, `NullPointerException`
- Never change implementation classes when a test fails unless explicitly requested
- Do not update comments or documentation unless explicitly instructed to do so
- Write clean, readable, and maintainable Java code
- Follow standard Java naming conventions (camelCase for variables/methods, PascalCase for classes, UPPER_SNAKE_CASE for constants)
- Use meaningful names for classes, methods, and variables
- Add Javadoc comments for all public classes and methods
- When you do changes to code ALWAYS add or update tests to cover your changes
- Follow SOLID principles
- Keep classes and methods small and focused (methods under 20 lines)
- Avoid magic numbers and strings - use constants or enums
- - **Never use hyphens (-) or underscores (_) in test method names**

## Lombok Usage
- All service classes must use `@Slf4j` for logging and `@RequiredArgsConstructor` for dependency injection
- Model/DTO classes must use `@Data` annotation with `@Accessors(fluent = false, chain = true)`
- Never manually write getters/setters, equals/hashCode, or toString methods - let Lombok handle them

### Example Service:
```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");
    
    private final TransactionRepository repository;
    private final PaymentGateway gateway;
    
    public TransactionResult process(Transaction transaction) {
        log.debug("Processing transaction: {}", transaction.getId());
        validateTransaction(transaction);
        // ...implementation...
    }
}
```

### Example Model:
```java
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = false, chain = true)
public class Transaction {
    private String id;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}

// Usage:
Transaction tx = new Transaction()
    .setAmount(new BigDecimal("100.00"))
    .setStatus(TransactionStatus.PENDING);
```

## Code Style and Formatting
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- One statement per line
- Use braces for all control structures, even single-line blocks
- Place opening braces on the same line (K&R style)
- Organize imports: java.*, javax.*, then third-party, then project imports
- Remove unused imports
- Prefer early returns to reduce nesting

## Object-Oriented Design
- Make classes immutable when possible
- Declare class members as private
- Use interfaces to define contracts
- Prefer interface types over concrete types for declarations
- Prefer composition over inheritance

## Collections and Streams
- Use appropriate collection interfaces (List, Set, Map) in declarations
- Prefer immutable collections (List.of(), Set.of(), Map.of())
- Use Stream API for functional operations
- Always use try-with-resources for AutoCloseable resources
- Prefer Optional over null for return types
- Return empty collections instead of null

### Example:
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository repository;
    
    public List<User> getActiveUsers() {
        return repository.findAll().stream()
            .filter(User::isActive)
            .sorted(Comparator.comparing(User::getName))
            .collect(Collectors.toUnmodifiableList());
    }
    
    public Optional<User> findById(String id) {
        return Optional.ofNullable(repository.findById(id));
    }
}
```

## Writing Unit Tests

### Standard Unit Tests (XXTest)
- Name test classes as `ClassNameTest` for regular unit tests
- Use JUnit 5 annotations
- Follow AAA pattern: Arrange, Act, Assert
- One assertion focus per test method
- Use @ParameterizedTest for multiple inputs
- Test edge cases and boundary conditions

### Example Unit Test:
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Calculator Tests")
class CalculatorTest {
    
    private Calculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }
    
    @Test
    void shouldAddTwoNumbersCorrectly() {
        // Arrange
        int a = 5;
        int b = 3;
        
        // Act
        int result = calculator.add(a, b);
        
        // Assert
        assertEquals(8, result);
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldHandleNegativeAndZeroValues(int value) {
        int result = calculator.add(value, 10);
        assertEquals(value + 10, result);
    }
}
```

### Mock Tests (XXMockTest)
- Name test classes as `ClassNameMockTest` when using mocks
- Use @MockitoBean for mocking dependencies
- Mock external dependencies, not the class under test
- Use descriptive test method names in camelCase
- **Never use hyphens (-) or underscores (_) in test method names**
- Cover both positive and negative scenarios
- Aim for at least 80% code coverage

### Example Mock Test:
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockitoBean;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionService Mock Tests")
class TransactionServiceMockTest {
    
    @MockitoBean
    private TransactionRepository repository;
    
    @MockitoBean
    private PaymentGateway gateway;
    
    private TransactionService service;
    
    @BeforeEach
    void setUp() {
        service = new TransactionService(repository, gateway);
    }
    
    @Test
    void shouldProcessTransactionSuccessfully() {
        // Arrange
        Transaction transaction = new Transaction()
            .setAmount(new BigDecimal("100.00"))
            .setStatus(TransactionStatus.PENDING);
        when(repository.save(any())).thenReturn(transaction);
        when(gateway.process(any())).thenReturn(PaymentResult.SUCCESS);
        
        // Act
        TransactionResult result = service.process(transaction);
        
        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        verify(repository).save(transaction);
        verify(gateway).process(transaction);
    }
    
    @Test
    void shouldThrowExceptionWhenTransactionIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> service.process(null),
            "Should throw IllegalArgumentException for null transaction");
    }
}
```

## Professional Java Practices
- Use SLF4J logging via Lombok's @Slf4j annotation
- Keep methods focused on single responsibility
- Write immutable classes where possible
- Use constructor injection via @RequiredArgsConstructor
- Validate parameters using Objects.requireNonNull()
- Use StringBuilder for string concatenation in loops
- Initialize variables at declaration when possible

### Example:
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentProcessor {
    
    private final PaymentGateway gateway;
    private final NotificationService notificationService;
    
    public PaymentResult processPayment(Payment payment) {
        Objects.requireNonNull(payment, "Payment cannot be null");
        log.debug("Processing payment: {}", payment.getId());
        
        try {
            PaymentResult result = gateway.process(payment);
            log.info("Payment {} processed successfully", payment.getId());
            notificationService.notifySuccess(payment);
            return result;
        } catch (PaymentException e) {
            log.error("Failed to process payment {}", payment.getId(), e);
            throw new IllegalStateException("Payment processing failed", e);
        }
    }
}
```

## Error Handling
- Never swallow exceptions silently
- Use try-with-resources for AutoCloseable resources
- Throw exceptions early (fail-fast)
- Catch specific exceptions, not generic Exception
- Log exceptions with context before re-throwing
- Use standard Java/Spring exceptions only
- Document thrown exceptions with @throws
- Never create custom utility classes or exception classes unless explicitly requested
- Never create custom Exception classes. Instead, use the standard exceptions provided by java and Spring, such as `IllegalArgumentException`, `UnsupportedOperationException`

### Standard Exceptions:
- `IllegalArgumentException` - invalid method arguments
- `IllegalStateException` - invalid object state
- `UnsupportedOperationException` - unimplemented methods
- `NullPointerException` - via Objects.requireNonNull()
- `IndexOutOfBoundsException` - invalid collection access

## Spring Guidelines
- Use constructor injection via @RequiredArgsConstructor
- Mark beans as @Component, @Service, @Repository appropriately
- Apply @Transactional at service layer only
- Use @Value for configuration with defaults
- Implement exception handling with @ControllerAdvice when needed

## Performance Considerations
- Use StringBuilder in loops
- Cache expensive computations
- Use lazy initialization for expensive objects
- Prefer primitives over wrappers when possible
- Initialize collections with appropriate size
- Profile before optimizing

## Security Best Practices
- Never log sensitive information (passwords, tokens, PII)
- Validate and sanitize all input
- Use parameterized queries
- Don't expose implementation details in errors
- Apply principle of least privilege

## Documentation Standards
- Write clear Javadoc for public APIs
- Include @param, @return, @throws tags
- Document complex business logic
- Keep documentation current with code
- Document thread-safety when relevant