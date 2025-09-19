package com.locme.integration;

import com.locme.LocmeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test pour vérifier que le contexte Spring Boot se charge correctement.
 */
@SpringBootTest(classes = LocmeApplication.class)
@ActiveProfiles("test")
public class SpringContextTest {

    @Test
    void contextLoads() {
        // Ce test vérifie que le contexte Spring se charge sans erreur
        // Si ce test passe, cela signifie que la configuration de base est correcte
    }
}
