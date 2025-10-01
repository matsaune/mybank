import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
plugins {
    java
    id("org.springframework.boot") version "3.4.+"
    id("io.spring.dependency-management") version "1.1.+"
    id("io.freefair.lombok") version "8.+"
    id("org.openapi.generator") version "7.4.0"

}

group = "no.experis.bgo.mybank"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot using OpenAPI, JPA, Flyway and Testcontainers using GitHub Copilot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.postgresql:postgresql")

    // OpenAPI and Swagger dependencies
    implementation("io.swagger.core.v3:swagger-annotations:2.+")
    implementation("io.swagger.core.v3:swagger-core:2.+")
    implementation("io.swagger.core.v3:swagger-models:2.+")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("org.webjars:webjars-locator-lite:1.1.0")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.+")
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    register<GenerateTask>("openApiGenerateCustomerApi") {
        generatorName.set("spring")
        inputSpec.set("$rootDir/src/main/resources/api/customer-api.yaml")
        outputDir.set("${layout.buildDirectory.get()}/generated")
        apiPackage.set("no.experis.bgo.mybank.api")
        modelPackage.set("no.experis.bgo.mybank.api.model")
        configOptions.set(mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "useTags" to "true",
            "apiSuffix" to "Api",
            "requestMappingMode" to "api_interface",
            "useSpringBoot3" to "true",
            "delegatePattern" to "false"
        ))
    }

    val openApiGenerateCustomerApi by existing

    compileJava {
        dependsOn(openApiGenerateCustomerApi)
    }

    withType<io.freefair.gradle.plugins.lombok.tasks.LombokTask>().configureEach {
        mustRunAfter(openApiGenerateCustomerApi)
    }
}

sourceSets {
    main {
        java {
            srcDirs("${layout.buildDirectory.get()}/generated/src/main/java")
        }
    }
}