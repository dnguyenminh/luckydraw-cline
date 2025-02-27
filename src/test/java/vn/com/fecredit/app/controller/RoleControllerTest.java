package vn.com.fecredit.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.RoleName;
import vn.com.fecredit.app.service.RoleService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    private Role adminRole;
    private Role staffRole;
    private RoleDTO.Response adminRoleResponse;
    private RoleDTO.Response staffRoleResponse;
    private RoleDTO.CreateRequest createRequest;
    private RoleDTO.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName(RoleName.ROLE_ADMIN);
        adminRole.setDescription("Admin Role");

        staffRole = new Role();
        staffRole.setId(2L);
        staffRole.setName(RoleName.ROLE_STAFF);
        staffRole.setDescription("Staff Role");

        adminRoleResponse = RoleDTO.Response.builder()
            .id(1L)
            .name(RoleName.ROLE_ADMIN)
            .description("Admin Role")
            .build();

        staffRoleResponse = RoleDTO.Response.builder()
            .id(2L)
            .name(RoleName.ROLE_STAFF)
            .description("Staff Role")
            .build();

        createRequest = RoleDTO.CreateRequest.builder()
            .name(RoleName.ROLE_ADMIN)
            .description("Admin Role")
            .build();

        updateRequest = RoleDTO.UpdateRequest.builder()
            .description("Updated Admin Role")
            .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_Success() throws Exception {
        when(roleService.create(any(RoleDTO.CreateRequest.class))).thenReturn(adminRoleResponse);

        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(adminRoleResponse.getId()))
            .andExpect(jsonPath("$.data.name").value(adminRoleResponse.getName().name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole_Success() throws Exception {
        when(roleService.update(eq(1L), any(RoleDTO.UpdateRequest.class))).thenReturn(adminRoleResponse);

        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(adminRoleResponse.getId()));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getRole_Success() throws Exception {
        when(roleService.getById(1L)).thenReturn(adminRoleResponse);

        mockMvc.perform(get("/api/v1/roles/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(adminRoleResponse.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/1"))
            .andExpect(status().isNoContent());

        verify(roleService).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getRoles_Success() throws Exception {
        List<RoleDTO.Summary> summaries = List.of(
            RoleDTO.Summary.builder().id(1L).name(RoleName.ROLE_ADMIN).build(),
            RoleDTO.Summary.builder().id(2L).name(RoleName.ROLE_STAFF).build()
        );
        Page<RoleDTO.Summary> page = new PageImpl<>(summaries);
        when(roleService.findAllSummaries(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRolesToUser_Success() throws Exception {
        Set<RoleName> roleNames = Set.of(RoleName.ROLE_STAFF);

        mockMvc.perform(post("/api/v1/roles/assign/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleNames)))
            .andExpect(status().isNoContent());

        verify(roleService).assignRolesToUser(eq(1L), eq(roleNames));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRolesToParticipant_Success() throws Exception {
        Set<RoleName> roleNames = Set.of(RoleName.ROLE_PARTICIPANT);

        mockMvc.perform(post("/api/v1/roles/assign/participant/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleNames)))
            .andExpect(status().isNoContent());

        verify(roleService).assignRolesToParticipant(eq(1L), eq(roleNames));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void checkUserRole_Success() throws Exception {
        when(roleService.hasUserRole(1L, RoleName.ROLE_STAFF)).thenReturn(true);

        mockMvc.perform(get("/api/v1/roles/user/1/has-role")
                .param("roleName", RoleName.ROLE_STAFF.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void unauthorized_WhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void forbidden_WhenInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }
}
