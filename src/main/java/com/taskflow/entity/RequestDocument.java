package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDocument {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    private InternalRequest request;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "content_url", nullable = false, length = 1000)
    private String contentUrl;

    @Column(nullable = false, length = 30)
    private String version;
}
