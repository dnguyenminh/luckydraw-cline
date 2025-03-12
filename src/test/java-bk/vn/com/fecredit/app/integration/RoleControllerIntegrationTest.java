package vn.com.fecredit.app.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.RoleName;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.util.TestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestUtils testUtils;

    private Role adminRole;
    private Role staffRole;
    private RoleDTO.CreateRequest createRequest;
    private RoleDTO.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();

        adminRole = new Role();
        adminRole.setName(RoleName.ROLE_ADMIN);
        adminRole.setDescription("Admin Role");
        adminRole = roleRepository.save(adminRole);

        staffRole = new Role();
        staffRole.setName(RoleName.ROLE_STAFF);
        staffRole.setDescription("Staff Role");
        staffRole = roleRepository.save(staffRole);

        createRequest = RoleDTO.CreateRequest.builder()
            .name(RoleName.ROLE_MANAGER)
            .description("Manager Role")
            .build();

        updateRequest = RoleDTO.UpdateRequest.builder()
            .description("Updated Role")
            .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_Success() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtils.asJsonString(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value(RoleName.ROLE_MANAGER.name()));

        assertThat(roleRepository.findByName(RoleName.ROLE_MANAGER)).isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole_Success() throws Exception {
        mockMvc.perform(put("/api/v1/roles/{id}", adminRole.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtils.asJsonString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.description").value("Updated Role"));

        Role updated = roleRepository.findById(adminRole.getId()).orElseThrow();
        assertThat(updated.getDescription()).isEqualTo("Updated Role");
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getAllRoles_Success() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getActiveRoles_Success() throws Exception {
        mockMvc.perform(get("/api/v1/roles/active"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignAndRemoveRoles_Success() throws Exception {
        // Create a test user and assign roles
        Long userId = testUtils.createTestUser().getId();
        Set<RoleName> roleNames = Set.of(RoleName.ROLE_STAFF);

        // Assign roles
        mockMvc.perform(post("/api/v1/roles/assign/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtils.asJsonString(roleNames)))
            .andExpect(status().isNoContent());

        // Verify role assignment
        mockMvc.perform(get("/api/v1/roles/user/{userId}/has-role", userId)
                .param("roleName", RoleName.ROLE_STAFF.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        // Remove roles
        mockMvc.perform(post("/api/v1/roles/remove/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtils.asJsonString(roleNames)))
            .andExpect(status().isNoContent());

        // Verify role removal
        mockMvc.perform(get("/api/v1/roles/user/{userId}/has-role", userId)
                .param("roleName", RoleName.ROLE_STAFF.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/{id}", staffRole.getId()))
            .andExpect(status().isNoContent());

        assertThat(roleRepository.findById(staffRole.getId())).isEmpty();
    }

    @Test
    void accessDenied_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void accessDenied_WhenInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtils.asJsonString(createRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void validationError_WhenInvalidRequest() throws Exception {
        createRequest.setName(null);

        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtils.asJsonString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }
}
