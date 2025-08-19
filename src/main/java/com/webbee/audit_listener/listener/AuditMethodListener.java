package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.document.AuditMethod;
import com.webbee.audit_listener.event.MethodLogEvent;
import com.webbee.audit_listener.repository.AuditMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Слушатель, который слушает "audit-methods" topic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditMethodListener {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final AuditMethodRepository auditMethodRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "audit-methods")
    @Transactional
    public void handle(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        log.info("ConsumerRecord<String, String> is being processed from AuditMethodListener");

        MethodLogEvent event = objectMapper.readValue(consumerRecord.value(), MethodLogEvent.class);
        AuditMethod auditMethod = auditMethodRepository.save(toAuditMethod(event));
        log.info("AuditMethod saved to Elasticsearch with id {}", auditMethod.getId());

        log.info("Processing completed in AuditMethodListener");
    }

    private AuditMethod toAuditMethod(MethodLogEvent event) {
        AuditMethod auditMethod = new AuditMethod();
        try {
            auditMethod.setTimestamp(LocalDateTime.parse(event.getLocalDateTime(), DATE_TIME_FORMATTER));
            auditMethod.setLogLevel(event.getLogLevel());
            auditMethod.setCorrelationId(event.getCorrelationId());
            auditMethod.setMethodName(event.getMethodName());
            auditMethod.setArgs(event.getArgs());
            auditMethod.setLogType(event.getLogType());
            auditMethod.setResult(event.getResult() != null ? objectMapper.writeValueAsString(event.getResult()) : null);
            auditMethod.setExceptionMessage(event.getExceptionMessage() != null ? event.getExceptionMessage() : null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return auditMethod;
    }

}
