package com.locme.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de base pour vérifier que la configuration fonctionne.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class BasicSetupTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Test
    void testPostgreSQLContainer() {
        assertTrue(postgres.isRunning(), "Le conteneur PostgreSQL devrait être en cours d'exécution");
        assertNotNull(postgres.getJdbcUrl(), "L'URL JDBC devrait être définie");
        assertTrue(postgres.getJdbcUrl().contains("postgresql"), "L'URL devrait contenir 'postgresql'");
    }

    @Test
    void testDatabaseConnection() {
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();
        
        assertNotNull(jdbcUrl, "URL JDBC ne devrait pas être null");
        assertNotNull(username, "Nom d'utilisateur ne devrait pas être null");
        assertNotNull(password, "Mot de passe ne devrait pas être null");
        
        assertEquals("test", username, "Le nom d'utilisateur devrait être 'test'");
        assertEquals("test", password, "Le mot de passe devrait être 'test'");
    }
}
