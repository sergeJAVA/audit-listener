package com.webbee.audit_listener.processor;

import com.webbee.audit_listener.document.AuditRequest;
import com.webbee.audit_listener.event.HttpLogEvent;
import com.webbee.audit_listener.repository.AuditRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditRequestProcessor {

    private final AuditRequestRepository auditRequestRepository;

    public AuditRequest process(HttpLogEvent event) {
        return saveAuditRequest(event);
    }

    private AuditRequest toAuditRequest(HttpLogEvent event) {
        AuditRequest auditRequest = new AuditRequest();

        auditRequest.setTimestamp(event.getTimestamp());
        auditRequest.setRequestType(event.getType());
        auditRequest.setMethod(event.getMethod());
        auditRequest.setStatusCode(String.valueOf(event.getStatus()));
        auditRequest.setPath(event.getPath());
        auditRequest.setRequestBody(event.getRequestBody().isEmpty() ? "{}" : event.getRequestBody());
        auditRequest.setResponseBody(event.getResponseBody().isEmpty() ? "{}" : event.getResponseBody());

        return auditRequest;
    }

    protected AuditRequest saveAuditRequest(HttpLogEvent event) {
        AuditRequest auditRequest = toAuditRequest(event);
        return auditRequestRepository.save(auditRequest);
    }

}
