package com.locme.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test configuration for integration tests.
 * This configuration allows all endpoints to be accessed without authentication for testing.
 */
@TestConfiguration
@EnableWebSecurity
public class IntegrationTestConfig {

    @Bean
    @Primary
    public SecurityFilterChain integrationTestSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                // Allow all requests for integration tests
                .anyRequest().permitAll()
            )
            // Disable session management for tests
            .sessionManagement(session -> session.disable());
        return http.build();
    }
}
