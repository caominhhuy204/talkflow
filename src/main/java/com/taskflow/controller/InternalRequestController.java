package com.taskflow.controller;

import com.taskflow.dto.request.CreateAttachmentRequest;
import com.taskflow.dto.request.CreateInternalRequestRequest;
import com.taskflow.dto.request.CreateRequestCommentRequest;
import com.taskflow.dto.request.UpdateInternalRequestRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.InternalRequestResponse;
import com.taskflow.dto.response.TimelineItemResponse;
import com.taskflow.service.InternalRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class InternalRequestController {

    private final InternalRequestService internalRequestService;

    public InternalRequestController(InternalRequestService internalRequestService) {
        this.internalRequestService = internalRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<InternalRequestResponse>> create(Principal principal,
                                                                       @Valid @RequestBody CreateInternalRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<InternalRequestResponse>builder()
                        .message("Request created")
                        .data(internalRequestService.create(principal.getName(), request))
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<List<InternalRequestResponse>>> list(Principal principal) {
        return ResponseEntity.ok(
                ApiResponse.<List<InternalRequestResponse>>builder()
                        .message("Request list fetched")
                        .data(internalRequestService.list(principal.getName()))
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<InternalRequestResponse>> getById(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<InternalRequestResponse>builder()
                        .message("Request fetched")
                        .data(internalRequestService.getById(principal.getName(), id))
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<InternalRequestResponse>> update(Principal principal,
                                                                       @PathVariable Long id,
                                                                       @Valid @RequestBody UpdateInternalRequestRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<InternalRequestResponse>builder()
                        .message("Request updated")
                        .data(internalRequestService.update(principal.getName(), id, request))
                        .build()
        );
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<InternalRequestResponse>> submit(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<InternalRequestResponse>builder()
                        .message("Request submitted")
                        .data(internalRequestService.submit(principal.getName(), id))
                        .build()
        );
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<InternalRequestResponse>> cancel(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<InternalRequestResponse>builder()
                        .message("Request cancelled")
                        .data(internalRequestService.cancel(principal.getName(), id))
                        .build()
        );
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<TimelineItemResponse>> addComment(Principal principal,
                                                                        @PathVariable Long id,
                                                                        @Valid @RequestBody CreateRequestCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TimelineItemResponse>builder()
                        .message("Comment created")
                        .data(internalRequestService.addComment(principal.getName(), id, request))
                        .build()
        );
    }

    @PostMapping("/{id}/attachments")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<TimelineItemResponse>> addAttachment(Principal principal,
                                                                           @PathVariable Long id,
                                                                           @Valid @RequestBody CreateAttachmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TimelineItemResponse>builder()
                        .message("Attachment created")
                        .data(internalRequestService.addAttachment(principal.getName(), id, request))
                        .build()
        );
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','HR')")
    public ResponseEntity<ApiResponse<List<TimelineItemResponse>>> timeline(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<List<TimelineItemResponse>>builder()
                        .message("Timeline fetched")
                        .data(internalRequestService.timeline(principal.getName(), id))
                        .build()
        );
    }
}
