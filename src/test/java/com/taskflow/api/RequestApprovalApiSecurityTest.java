package com.taskflow.api;

import com.taskflow.dto.response.ApprovalInboxItemResponse;
import com.taskflow.dto.response.InternalRequestResponse;
import com.taskflow.security.CustomUserDetailsService;
import com.taskflow.security.JwtService;
import com.taskflow.service.ApprovalService;
import com.taskflow.service.InternalRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestApprovalApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InternalRequestService internalRequestService;

    @MockBean
    private ApprovalService approvalService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        given(jwtService.extractUsername("admin-token")).willReturn("admin@test.local");
        given(jwtService.isTokenValid("admin-token", "admin@test.local")).willReturn(true);
        given(customUserDetailsService.loadUserByUsername("admin@test.local")).willReturn(
                User.withUsername("admin@test.local").password("x").roles("ADMIN").build()
        );

        given(jwtService.extractUsername("employee-token")).willReturn("employee@test.local");
        given(jwtService.isTokenValid("employee-token", "employee@test.local")).willReturn(true);
        given(customUserDetailsService.loadUserByUsername("employee@test.local")).willReturn(
                User.withUsername("employee@test.local").password("x").roles("EMPLOYEE").build()
        );

        given(jwtService.extractUsername("manager-token")).willReturn("manager@test.local");
        given(jwtService.isTokenValid("manager-token", "manager@test.local")).willReturn(true);
        given(customUserDetailsService.loadUserByUsername("manager@test.local")).willReturn(
                User.withUsername("manager@test.local").password("x").roles("MANAGER").build()
        );

        given(internalRequestService.list("employee@test.local")).willReturn(List.of(
                InternalRequestResponse.builder().id(1L).requestCode("LEAVE-1").requesterEmail("employee@test.local").build()
        ));

        given(approvalService.inbox("manager@test.local")).willReturn(List.of(
                ApprovalInboxItemResponse.builder().requestId(9L).requestCode("REQ-9").requesterEmail("employee@test.local").build()
        ));
    }

    @Test
    void listRequests_withAdminToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void listRequests_withEmployeeToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer employee-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].requestCode").value("LEAVE-1"));
    }

    @Test
    void approvalInbox_withEmployeeToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/approvals/inbox")
                        .header("Authorization", "Bearer employee-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void approvalInbox_withManagerToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/approvals/inbox")
                        .header("Authorization", "Bearer manager-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].requestCode").value("REQ-9"));
    }
}
