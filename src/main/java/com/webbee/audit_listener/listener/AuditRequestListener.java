package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.document.AuditRequest;
import com.webbee.audit_listener.event.HttpLogEvent;
import com.webbee.audit_listener.processor.AuditRequestProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Слушатель, который слушает "audit-requests" topic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditRequestListener {

    private final AuditRequestProcessor auditRequestProcessor;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "audit-requests")
    @Transactional
    public void handle(ConsumerRecord<String, String> consumerRecord) {
        log.info("ConsumerRecord<String, String> is being processed from AuditRequestListener");
        try {
            HttpLogEvent event = objectMapper.readValue(consumerRecord.value(), HttpLogEvent.class);
            AuditRequest auditRequest = auditRequestProcessor.process(event);
            log.info("AuditRequest saved to Elasticsearch with id {}", auditRequest.getId());
        } catch (Exception e) {
            log.error("An error occurred in AuditRequestListener when processing ConsumerRecord.", e);
            kafkaTemplate.send("audit-error", consumerRecord.key(), consumerRecord.value());
        }

        log.info("Processing completed in AuditRequestListener");
    }

}
