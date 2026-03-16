package com.taskflow.controller;

import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.UserOptionResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/employee-options")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<List<UserOptionResponse>>> employeeOptions(
            @RequestParam(value = "query", required = false) String query
    ) {
        String normalizedQuery = query == null ? null : query.trim();

        List<User> employees = (normalizedQuery == null || normalizedQuery.isBlank())
                ? userRepository.findByRoleOrderByFullNameAsc(Role.EMPLOYEE)
                : userRepository.searchEmployeesByQuery(normalizedQuery);

        List<UserOptionResponse> data = employees.stream()
                .limit(50)
                .map(user -> UserOptionResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .toList();

        return ResponseEntity.ok(
                ApiResponse.<List<UserOptionResponse>>builder()
                        .message("Employee options fetched")
                        .data(data)
                        .build()
        );
    }
}
