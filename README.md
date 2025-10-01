# MyBank Demo Application

This application demonstrates the use of GitHub Copilot in developing a Spring Boot banking application. The project showcases how to effectively use GitHub Copilot with prompt engineering and instruction files placed in the `.gitlab` folder.

## 🔗 Relevant Links

- [Spring Boot](https://spring.io/projects/spring-boot)
- [GitHub Copilot](https://github.com/features/copilot)
- [PostgreSQL](https://www.postgresql.org/)
- [Flyway Database Migrations](https://flywaydb.org/)
- [Swagger UI (Local)](http://localhost:8080/swagger-ui/index.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Lombok](https://projectlombok.org/)

## 📝 Project Overview

This is a demo banking application built with Spring Boot that implements basic banking functionalities. The project serves as a practical example of using GitHub Copilot to accelerate development while maintaining code quality and following best practices.

### Key Features

- Customer management with JPA
- Database migrations with Flyway
- API documentation with OpenAPI/Swagger
- Containerized testing with TestContainers
- Structured prompt engineering

## 🚀 Getting Started

### Prerequisites

- Java 21
- PostgreSQL
- Docker (for TestContainers)
- IntelliJ IDEA

### Running the Application Locally in IntelliJ

1. Open the project in IntelliJ IDEA
2. Navigate to `src/test/java/no/experis/bgo/mybank/LocalSpringApplication.java`
3. Click the green play button next to the `main` method, or right-click and select "Run 'LocalSpringApplication.main()'"

This will start the application with:
- TestContainers configuration (automatically starts a PostgreSQL container)
- Flyway migrations for database setup
- The application will be available at `http://localhost:8080`


### Alternative: Running with Gradle

```bash
./gradlew bootRun
```


The application will be available at `http://localhost:8080`
Swagger UI documentation: `http://localhost:8080/swagger-ui/index.html`

## 🤖 Base Prompts

The following base prompts are used with GitHub Copilot to guide development:

### 1. Entity Creation
```
SOMETHING
```

### 2. Repository Layer with Spring Data JPA and Domain object
```
SOMETHING
```

### 3. Service Layer
```
SOMETHING
```

### 4. OpenAPI Endpoint and Controller
```
SOMETHING
```

## 📚 Documentation

Detailed documentation can be found in the following locations:
- API Documentation: `/swagger-ui/index.html`
- GitHub Copilot Instructions: `.github/instructions/`
- GitHub Copilot Instructions: `.github/prompts/`

## 🛠 Technical Stack

- Spring Boot 3.4+
- Java 21
- PostgreSQL
- Flyway
- TestContainers
- OpenAPI/Swagger
- GitHub Copilot

## 👥 Contributing

When contributing to this project:
1. Use the provided prompt templates
2. Follow the established code structure
3. Include appropriate tests
4. Update documentation as needed
