# Integration Tests Documentation

This document describes the comprehensive integration tests for the LocMe Spring Boot backend application.

## Overview

The integration tests use **JUnit 5 + SpringBootTest + Testcontainers** to provide full end-to-end testing with a real PostgreSQL database. Each test runs in isolation with a clean database state.

## Test Architecture

### Base Configuration
- **BaseIntegrationTest**: Base class providing common setup and utilities
- **Testcontainers PostgreSQL**: Real PostgreSQL database in Docker container
- **SpringBootTest**: Full Spring context with random port
- **Database Isolation**: Each test starts with a clean database

### Test Categories

#### 1. Auth Integration Tests (`AuthIntegrationTest`)
- ✅ User registration with database persistence
- ✅ User login with JWT token validation
- ✅ Invalid credentials handling (401)
- ✅ Duplicate email registration
- ✅ Current user profile retrieval
- ✅ Unauthorized access handling

#### 2. Voiture Integration Tests (`VoitureIntegrationTest`)
- ✅ Voiture creation by SOCIETE users
- ✅ Fetch all voitures
- ✅ Fetch available voitures with filtering
- ✅ Voiture retrieval by ID
- ✅ Authorization checks (CLIENT cannot create voitures)
- ✅ Price and brand filtering

#### 3. Reservation Integration Tests (`ReservationIntegrationTest`)
- ✅ Reservation creation by CLIENT users
- ✅ Reservation confirmation by SOCIETE users
- ✅ Reservation cancellation
- ✅ Reservation completion
- ✅ My reservations retrieval
- ✅ Authorization and access control

#### 4. Paiement Integration Tests (`PaiementIntegrationTest`)
- ✅ Payment checkout creation
- ✅ Payment processing (success simulation)
- ✅ Payment failure handling
- ✅ Payment retrieval
- ✅ Refund processing (ADMIN only)
- ✅ Database persistence verification

#### 5. Favorite Integration Tests (`FavoriteIntegrationTest`)
- ✅ Toggle favorite (add/remove)
- ✅ Fetch user favorites
- ✅ Check if voiture is favorite
- ✅ Remove favorite
- ✅ Access control and authorization

## Running the Tests

### Prerequisites
- Docker installed and running
- Java 21+
- Maven 3.6+

### Run All Integration Tests
```bash
# Run all integration tests
mvn test -Dtest=IntegrationTestSuite

# Run specific test class
mvn test -Dtest=AuthIntegrationTest

# Run with verbose output
mvn test -Dtest=IntegrationTestSuite -X
```

### Run Individual Test Classes
```bash
# Auth tests
mvn test -Dtest=AuthIntegrationTest

# Voiture tests
mvn test -Dtest=VoitureIntegrationTest

# Reservation tests
mvn test -Dtest=ReservationIntegrationTest

# Payment tests
mvn test -Dtest=PaiementIntegrationTest

# Favorite tests
mvn test -Dtest=FavoriteIntegrationTest
```

### Run with Testcontainers
```bash
# Ensure Docker is running
docker --version

# Run tests (Testcontainers will automatically start PostgreSQL)
mvn test -Dtest=IntegrationTestSuite
```

## Test Configuration

### Testcontainers Setup
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test")
    .withReuse(true);
```

### Database Configuration
- **Database**: PostgreSQL 15 Alpine
- **Isolation**: Each test starts with clean database
- **DDL**: `create-drop` for complete isolation
- **Connection**: Dynamic property configuration

### Security Configuration
- **JWT Secret**: Test-specific secret key
- **Authentication**: Real JWT token generation and validation
- **Authorization**: Role-based access control testing

## Test Data Management

### Database Cleanup
Each test method starts with a clean database:
```java
@BeforeEach
void setUp() {
    clearDatabase(); // Removes all test data
}
```

### Test Data Creation
Helper methods create consistent test data:
- Users (CLIENT, SOCIETE, ADMIN roles)
- Societes with proper relationships
- Voitures with various attributes
- Reservations with different statuses
- Payments with different states

## Key Features Tested

### 1. Full HTTP Stack
- Real HTTP requests via `TestRestTemplate`
- Complete request/response cycle
- Status code validation
- Response body verification

### 2. Database Persistence
- Entity creation and retrieval
- Relationship mapping
- Transaction handling
- Data integrity

### 3. Security Integration
- JWT token generation and validation
- Role-based authorization
- Authentication flow
- Access control

### 4. Business Logic
- Reservation workflow
- Payment processing
- Favorite management
- Status transitions

## Troubleshooting

### Common Issues

1. **Docker not running**
   ```
   Error: Could not find a valid Docker environment
   ```
   Solution: Start Docker Desktop

2. **Port conflicts**
   ```
   Error: Port already in use
   ```
   Solution: Tests use random ports, restart if needed

3. **Database connection issues**
   ```
   Error: Connection refused
   ```
   Solution: Ensure Testcontainers can access Docker

### Debug Mode
```bash
# Run with debug logging
mvn test -Dtest=IntegrationTestSuite -Dlogging.level.com.locme=DEBUG
```

## Test Coverage

The integration tests provide comprehensive coverage of:
- ✅ All major API endpoints
- ✅ Authentication and authorization
- ✅ Database operations
- ✅ Business logic workflows
- ✅ Error handling scenarios
- ✅ Data validation
- ✅ Security constraints

## Performance Notes

- Tests use container reuse for faster startup
- Database cleanup is optimized for speed
- Each test runs in ~2-5 seconds
- Full suite completes in ~2-3 minutes

## Maintenance

### Adding New Tests
1. Extend `BaseIntegrationTest`
2. Use helper methods for data creation
3. Follow naming convention: `test{Method}_{Scenario}_{ExpectedResult}`
4. Verify both HTTP response and database state

### Updating Tests
- Update when API contracts change
- Maintain test data consistency
- Keep helper methods up to date
- Document any new test scenarios
