package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.document.AuditMethod;
import com.webbee.audit_listener.event.MethodLogEvent;
import com.webbee.audit_listener.processor.AuditMethodProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Слушатель, который слушает "audit-methods" topic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditMethodListener {

    private final AuditMethodProcessor auditMethodProcessor;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "audit-methods")
    @Transactional
    public void handle(ConsumerRecord<String, String> consumerRecord) {
        log.info("ConsumerRecord<String, String> is being processed from AuditMethodListener");
        try {
            MethodLogEvent event = objectMapper.readValue(consumerRecord.value(), MethodLogEvent.class);
            AuditMethod auditMethod = auditMethodProcessor.process(event);
            log.info("AuditMethod saved to Elasticsearch with id {}", auditMethod.getId());
        } catch (Exception e) {
            log.error("An error occurred in AuditMethodListener when processing ConsumerRecord.", e);
            kafkaTemplate.send("audit-error", consumerRecord.key(), consumerRecord.value());
        }

        log.info("Processing completed in AuditMethodListener");
    }

}
