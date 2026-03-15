package com.taskflow.repository;

import com.taskflow.entity.RequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestDocumentRepository extends JpaRepository<RequestDocument, Long> {
}
