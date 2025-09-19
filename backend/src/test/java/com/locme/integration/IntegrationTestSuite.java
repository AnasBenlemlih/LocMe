package com.locme.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Integration test suite that runs all integration tests.
 * This suite can be executed to run all integration tests together.
 */
@Suite
@SelectClasses({
    AuthIntegrationTest.class,
    VoitureIntegrationTest.class,
    ReservationIntegrationTest.class,
    PaiementIntegrationTest.class,
    FavoriteIntegrationTest.class
})
public class IntegrationTestSuite {
    // This class serves as a test suite container
    // All tests are defined in the @SelectClasses annotation
}
