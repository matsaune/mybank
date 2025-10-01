package no.experis.bgo.mybank;


public class LocalSpringApplication {

    public static void main(String[] args) {

        org.springframework.boot.SpringApplication.from(SpringApplication::main)
                .with(TestContainersConfiguration.class, FlywayTestConfiguration.class)
                .run(args);
    }
}
