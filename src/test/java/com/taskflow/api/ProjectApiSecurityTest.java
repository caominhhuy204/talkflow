package com.taskflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.security.CustomUserDetailsService;
import com.taskflow.security.JwtService;
import com.taskflow.service.AuthService;
import com.taskflow.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        given(projectService.getAllProjects(anyString())).willReturn(List.of(
                ProjectResponse.builder()
                        .id(1L)
                        .name("Demo")
                        .description("Demo project")
                        .createdAt(LocalDateTime.now())
                        .build()
        ));

        given(projectService.createProject(anyString(), any(CreateProjectRequest.class))).willReturn(
                ProjectResponse.builder()
                        .id(2L)
                        .name("Created")
                        .description("Created from test")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        given(jwtService.extractUsername("admin-token")).willReturn("admin@test.local");
        given(jwtService.isTokenValid("admin-token", "admin@test.local")).willReturn(true);
        given(customUserDetailsService.loadUserByUsername("admin@test.local")).willReturn(
                User.withUsername("admin@test.local").password("x").roles("ADMIN").build()
        );

        given(jwtService.extractUsername("member-token")).willReturn("member@test.local");
        given(jwtService.isTokenValid("member-token", "member@test.local")).willReturn(true);
        given(customUserDetailsService.loadUserByUsername("member@test.local")).willReturn(
                User.withUsername("member@test.local").password("x").roles("EMPLOYEE").build()
        );

        given(jwtService.extractUsername("manager-token")).willReturn("manager@test.local");
        given(jwtService.isTokenValid("manager-token", "manager@test.local")).willReturn(true);
        given(customUserDetailsService.loadUserByUsername("manager@test.local")).willReturn(
                User.withUsername("manager@test.local").password("x").roles("MANAGER").build()
        );

        given(jwtService.extractUsername("hr-token")).willReturn("hr@test.local");
        given(jwtService.isTokenValid("hr-token", "hr@test.local")).willReturn(true);
        given(customUserDetailsService.loadUserByUsername("hr@test.local")).willReturn(
                User.withUsername("hr@test.local").password("x").roles("HR").build()
        );

        given(projectService.assignEmployee(anyString(), anyLong(), any())).willReturn(
                ProjectResponse.builder()
                        .id(2L)
                        .name("Created")
                        .description("Created from test")
                        .build()
        );
    }

    @Test
    void register_shouldBePublic() throws Exception {
        given(authService.register(any())).willReturn(
                AuthResponse.builder().token("abc").email("new@test.local").role("EMPLOYEE").build()
        );

        String payload = """
                {
                  "fullName":"New User",
                  "email":"new@test.local",
                  "password":"Secret123!",
                  "role":"EMPLOYEE"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc"));
    }

    @Test
    void getProjects_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void getProjects_withMemberToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer member-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Demo"));
    }

    @Test
    void getProjects_withAdminToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void createProject_withMemberToken_shouldReturn403() throws Exception {
        String payload = objectMapper.writeValueAsString(
                CreateProjectRequest.builder().name("X").description("Y").build()
        );

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer member-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void createProject_withManagerToken_shouldReturn201() throws Exception {
        String payload = objectMapper.writeValueAsString(
                CreateProjectRequest.builder().name("X").description("Y").build()
        );

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer manager-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Project created successfully"));
    }

    @Test
    void createProject_withAdminToken_shouldReturn403() throws Exception {
        String payload = objectMapper.writeValueAsString(
                CreateProjectRequest.builder().name("X").description("Y").build()
        );

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void corsPreflight_shouldBeAllowed() throws Exception {
        mockMvc.perform(options("/api/projects")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    void assignEmployee_withHrToken_shouldReturn200() throws Exception {
        String payload = """
                {
                  "employeeEmail":"employee@test.local"
                }
                """;

        mockMvc.perform(post("/api/projects/2/assign-employee")
                        .header("Authorization", "Bearer hr-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee assigned to project successfully"));
    }

    @Test
    void assignEmployee_withManagerToken_shouldReturn403() throws Exception {
        String payload = """
                {
                  "employeeEmail":"employee@test.local"
                }
                """;

        mockMvc.perform(post("/api/projects/2/assign-employee")
                        .header("Authorization", "Bearer manager-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }
}
