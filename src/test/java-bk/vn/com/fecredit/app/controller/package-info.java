/**
 * Integration tests for REST controllers.
 *
 * This package contains tests that verify the behavior of REST endpoints,
 * including authentication, authorization, request/response handling,
 * and integration with the service layer.
 *
 * Test classes:
 * - AuthControllerTest: Tests authentication endpoints
 * - UserControllerTest: Tests user management endpoints
 *
 * Testing approach:
 * - Uses MockMvc for endpoint testing
 * - Verifies security constraints
 * - Tests request validation
 * - Validates response formats
 * - Tests error handling
 * - Uses custom assertions
 * - Integrates with test security configuration
 *
 * @see org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
 */
package vn.com.fecredit.app.controller;