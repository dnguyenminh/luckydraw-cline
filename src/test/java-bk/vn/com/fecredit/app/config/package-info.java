/**
 * Test configuration and setup classes.
 *
 * This package contains configuration classes that set up the test environment
 * and provide necessary infrastructure for integration tests.
 *
 * Key components:
 * - BaseIntegrationTest: Base class for all integration tests
 * - TestJpaConfig: JPA and database configuration for tests
 * - TestSecurityConfig: Security configuration for tests
 * - MapperTestConfig: MapStruct mapper configuration for tests
 *
 * Features:
 * - In-memory H2 database configuration
 * - Test security setup with JWT support
 * - Test profile configuration
 * - Test data initialization
 *
 * @see org.springframework.boot.test.context.TestConfiguration
 */
package vn.com.fecredit.app.config;