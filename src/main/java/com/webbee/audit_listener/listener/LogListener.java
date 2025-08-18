package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.document.AuditMethod;
import com.webbee.audit_listener.document.AuditRequest;
import com.webbee.audit_listener.event.HttpLogEvent;
import com.webbee.audit_listener.event.MethodLogEvent;
import com.webbee.audit_listener.repository.AuditMethodRepository;
import com.webbee.audit_listener.repository.AuditRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Слушатель, который слушает "audit-log" топик.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogListener {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final AuditMethodRepository auditMethodRepository;
    private final AuditRequestRepository auditRequestRepository;
    private final ObjectMapper objectMapper;

    /**
     * Метод для обработки сообщений приходящих из Kafka.
     * <p>С ключом 1 приходят логи методов, а с ключом 2 логи HTTP-запросов.</p>
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "audit-log")
    @Transactional
    public void handle(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

        log.info("ConsumerRecord<String, String> is being processed");
        if (consumerRecord.key().equals("1")) {
            MethodLogEvent event = objectMapper.readValue(consumerRecord.value(), MethodLogEvent.class);
            auditMethodRepository.save(toAuditMethod(event));
            log.info("MethodLog saved to database");
        }

        if (consumerRecord.key().equals("2")) {
            HttpLogEvent event = objectMapper.readValue(consumerRecord.value(), HttpLogEvent.class);
            auditRequestRepository.save(toAuditRequest(event));
            log.info("HttpLog saved to database");
        }
        log.info("Processing completed");


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
