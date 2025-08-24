package com.webbee.audit_listener.repository;

import com.webbee.audit_listener.document.AuditRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AuditRequestRepository extends ElasticsearchRepository<AuditRequest, String> {
}
