package com.locme.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Classe alternative pour exécuter tous les tests d'intégration.
 * Cette classe peut être utilisée si JUnit Platform Suite pose des problèmes.
 * 
 * Pour exécuter tous les tests d'intégration, utilisez:
 * mvn test -Dtest=AllIntegrationTests
 * 
 * Ou exécutez les tests individuellement:
 * mvn test -Dtest=AuthIntegrationTest
 * mvn test -Dtest=VoitureIntegrationTest
 * mvn test -Dtest=ReservationIntegrationTest
 * mvn test -Dtest=PaiementIntegrationTest
 * mvn test -Dtest=FavoriteIntegrationTest
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AllIntegrationTests {

    @Test
    void runAllIntegrationTests() {
        // Cette méthode sert de point d'entrée pour exécuter tous les tests
        // Les tests individuels sont exécutés via les classes de test séparées
        
        System.out.println("Pour exécuter tous les tests d'intégration:");
        System.out.println("1. mvn test -Dtest=AuthIntegrationTest");
        System.out.println("2. mvn test -Dtest=VoitureIntegrationTest");
        System.out.println("3. mvn test -Dtest=ReservationIntegrationTest");
        System.out.println("4. mvn test -Dtest=PaiementIntegrationTest");
        System.out.println("5. mvn test -Dtest=FavoriteIntegrationTest");
        System.out.println();
        System.out.println("Ou utilisez le pattern pour exécuter tous les tests d'intégration:");
        System.out.println("mvn test -Dtest=*IntegrationTest");
    }
}
