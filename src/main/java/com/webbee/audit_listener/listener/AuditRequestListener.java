package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.document.AuditRequest;
import com.webbee.audit_listener.event.HttpLogEvent;
import com.webbee.audit_listener.repository.AuditRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Слушатель, который слушает "audit-requests" topic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditRequestListener {

    private final AuditRequestRepository auditRequestRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "audit-requests")
    @Transactional
    public void handle(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        log.info("ConsumerRecord<String, String> is being processed from AuditRequestListener");

        HttpLogEvent event = objectMapper.readValue(consumerRecord.value(), HttpLogEvent.class);
        AuditRequest auditRequest = auditRequestRepository.save(toAuditRequest(event));
        log.info("AuditRequest saved to Elasticsearch with id {}", auditRequest.getId());

        log.info("Processing completed in AuditRequestListener");
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

}
