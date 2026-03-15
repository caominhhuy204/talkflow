package com.taskflow.service;

import com.taskflow.dto.request.CreateAttachmentRequest;
import com.taskflow.dto.request.CreateInternalRequestRequest;
import com.taskflow.dto.request.CreateRequestCommentRequest;
import com.taskflow.dto.request.UpdateInternalRequestRequest;
import com.taskflow.dto.response.InternalRequestResponse;
import com.taskflow.dto.response.TimelineItemResponse;

import java.util.List;

public interface InternalRequestService {
    InternalRequestResponse create(String currentUserEmail, CreateInternalRequestRequest request);
    List<InternalRequestResponse> list(String currentUserEmail);
    InternalRequestResponse getById(String currentUserEmail, Long id);
    InternalRequestResponse update(String currentUserEmail, Long id, UpdateInternalRequestRequest request);
    InternalRequestResponse submit(String currentUserEmail, Long id);
    InternalRequestResponse cancel(String currentUserEmail, Long id);
    TimelineItemResponse addComment(String currentUserEmail, Long id, CreateRequestCommentRequest request);
    TimelineItemResponse addAttachment(String currentUserEmail, Long id, CreateAttachmentRequest request);
    List<TimelineItemResponse> timeline(String currentUserEmail, Long id);
}
