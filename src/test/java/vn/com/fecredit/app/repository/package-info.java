/**
 * Integration tests for JPA repositories.
 *
 * This package contains tests that verify the correct operation of JPA repositories
 * against an H2 in-memory database. Tests use predefined test data and verify
 * CRUD operations and custom query methods.
 *
 * Test classes:
 * - UserRepositoryTest: Tests user repository operations
 * - RoleRepositoryTest: Tests role repository operations
 *
 * Testing approach:
 * - Uses H2 in-memory database
 * - Initializes with schema.sql and data.sql
 * - Runs in transaction (rolls back after each test)
 * - Uses custom test data utilities
 *
 * @see org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
 */
package vn.com.fecredit.app.repository;