package no.experis.bgo.mybank.repository;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Tag;
import no.experis.bgo.mybank.FlywayTestConfiguration;
import no.experis.bgo.mybank.TestContainersConfiguration;

@DataJpaTest
@Import({FlywayTestConfiguration.class, TestContainersConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("integration-test")
public abstract class AbstractRepositoryTest {
}
