package com.webbee.audit_listener.repository;

import com.webbee.audit_listener.document.AuditMethod;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AuditMethodRepository extends ElasticsearchRepository<AuditMethod, String> {
}
