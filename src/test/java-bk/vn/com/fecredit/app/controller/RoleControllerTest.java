package vn.com.fecredit.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.service.RoleService;
import vn.com.fecredit.app.util.TestDataBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    private RoleDTO.Response roleResponse;
    private RoleDTO.CreateRequest createRequest;
    private RoleDTO.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        
        roleResponse = RoleDTO.Response.builder()
            .id(1L)
            .name(RoleName.MANAGER)
            .description("Test Role")
            .permissions(Set.of("READ", "WRITE"))
            .userCount(5)
            .active(true)
            .build();

        createRequest = RoleDTO.CreateRequest.builder()
            .name(RoleName.MANAGER)
            .description("Test Role")
            .permissions(Set.of("READ", "WRITE"))
            .build();

        updateRequest = RoleDTO.UpdateRequest.builder()
            .description("Updated Role")
            .permissions(Set.of("READ"))
            .active(true)
            .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_ShouldCreateNewRole() throws Exception {
        given(roleService.createRole(any(RoleDTO.CreateRequest.class)))
            .willReturn(roleResponse);

        mockMvc.perform(post("/api/v1/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleResponse.getId()))
                .andExpect(jsonPath("$.name").value(roleResponse.getName().name()))
                .andExpect(jsonPath("$.description").value(roleResponse.getDescription()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole_ShouldUpdateExistingRole() throws Exception {
        given(roleService.updateRole(eq(1L), any(RoleDTO.UpdateRequest.class)))
            .willReturn(roleResponse);

        mockMvc.perform(put("/api/v1/roles/{id}", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(roleResponse.getDescription()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole_ShouldDeleteRole() throws Exception {
        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/api/v1/roles/{id}", 1)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(roleService).deleteRole(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MANAGER"})
    void getRole_ShouldReturnRole() throws Exception {
        given(roleService.getRole(1L)).willReturn(roleResponse);

        mockMvc.perform(get("/api/v1/roles/{id}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleResponse.getId()))
                .andExpect(jsonPath("$.name").value(roleResponse.getName().name()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MANAGER"})
    void findByName_ShouldReturnRole() throws Exception {
        given(roleService.findByName(RoleName.MANAGER))
            .willReturn(Optional.of(roleResponse));

        mockMvc.perform(get("/api/v1/roles/name/{name}", RoleName.MANAGER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(RoleName.MANAGER.name()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MANAGER"})
    void getRoles_ShouldReturnPageOfRoles() throws Exception {
        PageImpl<RoleDTO.Summary> page = new PageImpl<>(List.of(
            RoleDTO.Summary.builder()
                .id(1L)
                .name(RoleName.MANAGER)
                .description("Test Role")
                .userCount(5)
                .active(true)
                .build()
        ));

        given(roleService.getRoles(any(), any(EntityStatus.class), any(PageRequest.class)))
            .willReturn(page);

        mockMvc.perform(get("/api/v1/roles")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value(RoleName.MANAGER.name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRole_ShouldAssignRoleToUser() throws Exception {
        RoleDTO.Assignment request = new RoleDTO.Assignment(1L, RoleName.MANAGER);
        given(roleService.assignRole(any(RoleDTO.Assignment.class)))
            .willReturn(roleResponse);

        mockMvc.perform(post("/api/v1/roles/assign")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(RoleName.MANAGER.name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePermissions_ShouldUpdateRolePermissions() throws Exception {
        Set<String> permissions = Set.of("READ", "WRITE");
        given(roleService.updatePermissions(eq(1L), any()))
            .willReturn(roleResponse);

        mockMvc.perform(put("/api/v1/roles/{id}/permissions", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permissions)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions").isArray());
    }
}
