---
mode: "agent"
description: 'This context file provides detailed guidance for creating JPA entities, database schemas, repositories, and associated tests following established coding patterns and conventions.'
---
Always create JPA entities, repositories, and tests in the current context.
Create SQL to define the database schema, including tables, sequences, and constraints
All enums should be in separate java classes in the model package.

Follow the patterns and conventions outlined below.

When done validate that all the points under "Checklist for New JPA Entities" are all checked off.


## JPA Entity Patterns

### Entity Class Structure

All JPA entities in this project must follow a consistent pattern:
The name of the TABLE_NAME should be in uppercase.

```java
package no.experis.bgo.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity(name = "TABLE_NAME")
@Accessors(fluent = true, chain = true)
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_seq")
    @SequenceGenerator(name = "entity_seq", sequenceName = "table_name_id_seq", allocationSize = 1)
    private Long id;
    
    @CreationTimestamp
    private LocalDateTime created;
    
    @UpdateTimestamp
    private LocalDateTime updated;
    
    // Other fields...
}
```

### Key Conventions

1. **Package**: All entities are in `no.experis.bgo.app.model` (replace `app` with your application name)
2. **Annotations**:
    - `@Data` from Lombok for getters/setters
    - `@Entity(name = "TABLE_NAME")` with uppercase table name
    - `@Accessors(fluent = true, chain = true)` for fluent API
3. **Primary Keys**:
    - Use `@GeneratedValue(strategy = GenerationType.SEQUENCE)`
    - Define custom sequence generator with pattern: `table_name_id_seq`
    - Use `Long` type for IDs (except UUIDs where appropriate)
4. **Timestamps**:
    - `@CreationTimestamp` for `created` field
    - `@UpdateTimestamp` for `updated` field
    - Use `LocalDateTime` type
5. **Enums**:
    - Use `@Enumerated(EnumType.STRING)` for enum fields
    - Create separate enum classes in the model package
6. **DomainEntity Name**:
    - The name of the entity class should be in PascalCase and reflect the table name in singular form appended with "Entity" (e.g., `ExpectedEventEntity` for `EXPECTED_EVENT` table)

### Entity Examples

#### Simple Entity with Sequence ID
```java
@Data
@Entity(name = "EXPECTED_EVENT")
@Accessors(fluent = true, chain = true)
public class ExpectedEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expected_event_seq")
    @SequenceGenerator(name = "expected_event_seq", sequenceName = "expected_event_internal_id_seq", allocationSize = 1)
    private Long internalId;
    
    private String eventName;
    private LocalTime expectedTime;
    private String description;
    private String currency;
    private String timeZone;
    private boolean enabled;
    
    @CreationTimestamp
    private LocalDateTime created;
    
    @UpdateTimestamp
    private LocalDateTime updated;
    
    private LocalDateTime lastEventTime;
    
    @Enumerated(EnumType.STRING)
    private EventAlarmState state;
}
```

#### Entity with UUID Primary Key
```java
@Data
@Entity(name = "TRADE_FILES")
@Accessors(fluent = true, chain = true)
public class TradeFile {
    @Id
    @GeneratedValue
    private UUID id;
    
    @CreationTimestamp
    private LocalDateTime created;
    
    @UpdateTimestamp
    private LocalDateTime updated;
    
    private String fileName;
    
    @Enumerated(EnumType.STRING)
    private TradeFileStatus status;
    
    private Long jobExecutionId;
    private Integer numberOfTrades;
    private String correlationId;
}
```

#### Entity with Column Mapping
```java
@Data
@Entity
@Table(name = "INTEGRATION_STATE")
@Accessors(fluent = true, chain = true)
public class IntegrationState {
    @Id
    @Column(name = "INTEGRATION_NAME")
    private String integrationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "INPUT_STATE")
    private StateType inputState;

    @Enumerated(EnumType.STRING)
    @Column(name = "OUTPUT_STATE")
    private StateType outputState;

    public enum StateType {
        ENABLED, PAUSED, DISCARD
    }
}
```

## Database Schema Patterns

### Flyway Migration Files

All database changes are managed through Flyway migrations in the `db/` folder in project root with naming pattern:
`V{major}_{minor}_{patch}__{description}.sql`

#### Table Creation Pattern
```sql
CREATE TABLE TABLE_NAME (
    ID BIGSERIAL PRIMARY KEY,
    CREATED TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED TIMESTAMP NOT NULL DEFAULT NOW(),
    -- Other columns...
    CONSTRAINT chk_table_name_status CHECK (STATUS IN ('VALUE1', 'VALUE2', 'VALUE3'))
);

-- Add comments
COMMENT ON TABLE TABLE_NAME IS 'Description of the table purpose';
COMMENT ON COLUMN TABLE_NAME.ID IS 'Primary key identifier';
```

#### Sequence Creation for Custom IDs
```sql
CREATE SEQUENCE table_name_internal_id_seq START 1;

CREATE TABLE TABLE_NAME (
    INTERNAL_ID BIGSERIAL PRIMARY KEY,
    -- Other columns...
);
```

#### Enum Constraints

Add constraints for enum fields to ensure valid values. For example
```sql
-- For status fields
STATUS VARCHAR(20) CHECK (STATUS IN ('REGISTERED', 'WAITING_FOR_ACK', 'MQ_ERROR', 'ERROR', 'ABENDED', 'CANCELLED'))

```

## Repository Patterns

### Repository Interface Structure

All repositories follow Spring Data JPA patterns:

```java
package no.experis.bgo.app.repository;

import no.experis.bgo.app.model.EntityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityNameRepository extends JpaRepository<EntityName, Long>, JpaSpecificationExecutor<EntityName> {
    
    // Query methods following Spring Data conventions
    List<EntityName> findAllByStatusOrderById(Status status);
    Optional<EntityName> findByExternalId(String externalId);
    
    // Custom queries using @Query
    @Query("SELECT e FROM ENTITY_NAME e WHERE e.field = :value")
    List<EntityName> findByCustomCriteria(@Param("value") String value);
    
    // Existence checks
    boolean existsByCorrelationId(String correlationId);
}
```

### Repository Conventions

1. **Package**: `no.experis.bgo.app.repository` (replace `app` with your application name)
2. **Naming**: `{EntityName}Repository`
3. **Annotations**: Always use `@Repository`
4. **Inheritance**:
    - Extend `JpaRepository<Entity, IdType>`
    - Add `JpaSpecificationExecutor<Entity>` for complex queries
5. **Query Methods**:
    - Use Spring Data naming conventions
    - Add `@Query` for complex JPQL queries
    - Use `@Param` for named parameters
6. **Documentation**: Add Javadoc for complex methods

### Repository Examples

#### Simple Repository
```java
@Repository
public interface ExpectedEventEntityRepository extends JpaRepository<ExpectedEventEntity, Long> {
    Optional<ExpectedEventEntity> findExpectedEventEntityByEventName(String eventName);
    List<ExpectedEventEntity> findByEnabledTrueAndCurrency(String currency);
}
```

#### Repository with Custom Queries
```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    
    List<Transaction> findAllByJobExecutionIdOrderByInternalId(Long executionId);
    Optional<Transaction> findTransactionByCorrelationId(String externalId);
    
    @Query("SELECT t FROM TRANSACTION t " +
           "WHERE t.jobExecutionId = :jobExecutionId " +
           "AND t.status IN :statuses " +
           "ORDER BY t.internalId ASC")
    List<Transaction> findTransactionsByJobExecutionIdAndStatus(
            @Param("jobExecutionId") Long jobExecutionId,
            @Param("statuses") List<TransactionStatus> statuses);
            
    /**
     * Checks if a transaction exists with the given correlation ID.
     * @param correlationId the correlation ID to check
     * @return true if a transaction exists with the correlation ID, false otherwise
     */
    boolean existsByCorrelationId(String correlationId);
}
```

## Test Patterns

### Repository Test Structure

All repository tests must extend `AbstractRepositoryTest` and follow integration test patterns:

If not present create `AbstractRepositoryTest`:

```java
@DataJpaTest
@Import( {FlywayTestConfiguration.class, TestContainersConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("integration-test")
public abstract class AbstractRepositoryTest { }
```
All tests should use @SQL to inject test data on class level or method level. The class level should be used for all tests in the class, and the method level should be used for specific tests that require different test data.
```java
package no.experis.bgo.app.repository;

import lombok.extern.slf4j.Slf4j;
import no.experis.bgo.app.model.EntityName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class EntityNameRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private EntityNameRepository repository;

    @Test
    void shouldFindEntityByField() {
        // Given
        EntityName entity = createTestEntity();
        repository.save(entity);

        // When
        Optional<EntityName> result = repository.findByField("value");

        // Then
        assertTrue(result.isPresent());
        assertEquals("expected", result.get().getField());
    }

    @Test
    @Sql("classpath:test-data/entity-test-data.sql")
    void shouldFindEntitiesWithTestData() {
        // Test with pre-loaded test data
        List<EntityName> results = repository.findAll();
        assertFalse(results.isEmpty());
    }

    private EntityName createTestEntity() {
        return new EntityName()
                .field1("value1")
                .field2("value2");
    }
}
```
## Test Data Management

### SQL Test Data Files

Create test data files in `src/test/resources/`:

```sql
-- entity-test-data.sql
INSERT INTO ENTITY_NAME (ID, FIELD1, FIELD2, CREATED, UPDATED) 
VALUES 
    (1, 'test1', 'value1', NOW(), NOW()),
    (2, 'test2', 'value2', NOW(), NOW());
```


## Required Dependencies and Annotations

When creating new JPA entities and repositories, ensure these dependencies are available:

```java
// JPA and Hibernate
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.Data;
import lombok.experimental.Accessors;

// Spring Data
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Testing
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
```

## Checklist for New JPA Entities

When creating a new JPA entity, repository, and tests:

### Entity Creation
- [ ] Create entity class in `no.experis.bgo.app.model` (replace `app` with your application name)
- [ ] Add required annotations: `@Data`, `@Entity`, `@Accessors`
- [ ] Define primary key strategy (sequence or UUID)
- [ ] Add `@CreationTimestamp` and `@UpdateTimestamp` fields
- [ ] Create any required enum classes
- [ ] Add column mappings if table names differ from field names

### Database Schema
- [ ] Create Flyway migration file with proper naming
- [ ] Define table with appropriate constraints
- [ ] Create sequences if using custom ID generation
- [ ] Add table and column comments
- [ ] Include enum constraints for status fields

### Repository Creation
- [ ] Create repository interface in `no.experis.bgo.app.repository` (replace `app` with your application name)
- [ ] Extend `JpaRepository` and optionally `JpaSpecificationExecutor`
- [ ] Add `@Repository` annotation
- [ ] Define query methods following Spring Data conventions
- [ ] Add custom `@Query` methods if needed
- [ ] Add Javadoc for complex methods

### Test Creation
- [ ] Create repository test extending `AbstractRepositoryTest`
- [ ] Add `@Tag("integration-test")` annotation
- [ ] Create service test with mocked dependencies
- [ ] Add test data SQL files if needed
- [ ] Create test data builders for complex entities
- [ ] Add integration tests if service interacts with external systems

### Additional Considerations
- [ ] Update any related services to handle the new entity
- [ ] Add error handling if the entity can fail processing
- [ ] Update API controllers if REST endpoints are required
- [ ] Add metrics/monitoring if the entity represents business events

