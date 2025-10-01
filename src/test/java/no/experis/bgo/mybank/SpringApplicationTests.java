package no.experis.bgo.mybank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({FlywayTestConfiguration.class, TestContainersConfiguration.class})
class SpringApplicationTests {

	@Test
	void contextLoads() {
	}

}
