package com.webbee.audit_listener.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.document.AuditMethod;
import com.webbee.audit_listener.event.MethodLogEvent;
import com.webbee.audit_listener.repository.AuditMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuditMethodProcessor {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final AuditMethodRepository auditMethodRepository;
    private final ObjectMapper objectMapper;

    public AuditMethod process(MethodLogEvent event) throws JsonProcessingException {
        return saveAuditMethod(event);
    }

    private AuditMethod toAuditMethod(MethodLogEvent event) throws JsonProcessingException {
        AuditMethod auditMethod = new AuditMethod();
        auditMethod.setTimestamp(LocalDateTime.parse(event.getLocalDateTime(), DATE_TIME_FORMATTER));
        auditMethod.setLogLevel(event.getLogLevel());
        auditMethod.setCorrelationId(event.getCorrelationId());
        auditMethod.setMethodName(event.getMethodName());
        auditMethod.setArgs(event.getArgs());
        auditMethod.setLogType(event.getLogType());
        auditMethod.setResult(event.getResult() != null ? objectMapper.writeValueAsString(event.getResult()) : null);
        auditMethod.setExceptionMessage(event.getExceptionMessage() != null ? event.getExceptionMessage() : null);

        return auditMethod;
    }

    protected AuditMethod saveAuditMethod(MethodLogEvent event) throws JsonProcessingException {
        AuditMethod auditMethod = toAuditMethod(event);
        return auditMethodRepository.save(auditMethod);
    }

}
