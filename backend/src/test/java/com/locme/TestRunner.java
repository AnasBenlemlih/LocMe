package com.locme;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test runner simple pour vérifier que le contexte Spring se charge correctement
 */
@SpringBootTest
@ActiveProfiles("test")
class TestRunner {

    @Test
    void contextLoads() {
        // Ce test vérifie simplement que le contexte Spring se charge sans erreur
        System.out.println("✅ Contexte Spring chargé avec succès !");
    }
}
