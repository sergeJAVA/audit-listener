package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.event.HttpLogEvent;
import com.webbee.audit_listener.event.MethodLogEvent;
import com.webbee.audit_listener.model.HttpLog;
import com.webbee.audit_listener.model.MethodLog;
import com.webbee.audit_listener.service.HttpLogService;
import com.webbee.audit_listener.service.MethodLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final HttpLogService httpLogService;
    private final MethodLogService methodLogService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Метод для обработки сообщений приходящих из Kafka.
     * <p>С ключом 1 приходят логи методов, а с ключом 2 логи HTTP-запросов.</p>
     * @param consumerRecord
     * @throws JsonProcessingException
     */
    @KafkaListener(topics = "audit-log")
    @Transactional
    public void handle(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        try {
            log.info("ConsumerRecord<String, String> is being processed");
            if (consumerRecord.key().equals("1")) {
                MethodLogEvent event = objectMapper.readValue(consumerRecord.value(), MethodLogEvent.class);
                methodLogService.saveMethodLog(toMethodLog(event));
                log.info("MethodLog saved to database");
            }

            if (consumerRecord.key().equals("2")) {
                HttpLogEvent event = objectMapper.readValue(consumerRecord.value(), HttpLogEvent.class);
                httpLogService.saveHttpLog(toHttpLog(event));
                log.info("HttpLog saved to database");
            }
            log.info("Processing completed");
        }catch (JsonProcessingException ex) {
            log.error("An error occurred while processing the event.");
            throw ex;
        }

    }

    /**
     * Вспомогательный метод для преобразования ивента в сущность {@link MethodLog}
     * @param event
     * @return объект типа {@link MethodLog}
     */
    private MethodLog toMethodLog(MethodLogEvent event) {
        MethodLog methodLog = new MethodLog();

        methodLog.setTimestamp(LocalDateTime.parse(event.getLocalDateTime(), DATE_TIME_FORMATTER));
        methodLog.setMethodName(event.getMethodName());
        methodLog.setLogLevel(event.getLogLevel());
        methodLog.setCorrelationId(event.getCorrelationId());
        methodLog.setArgs(event.getArgs() != null ? event.getArgs() : null);
        methodLog.setLogType(event.getLogType());
        methodLog.setResult(event.getResult() != null ? event.getResult() : null);
        methodLog.setExceptionMessage(event.getExceptionMessage() != null ? event.getExceptionMessage() : null);

        return methodLog;
    }

    /**
     * Вспомогательный метод для преобразования ивента в сущность {@link HttpLog}
     * @param event
     * @return объект типа {@link HttpLog}
     */
    private HttpLog toHttpLog(HttpLogEvent event) {
        HttpLog httpLog = new HttpLog();

        httpLog.setTimestamp(event.getTimestamp());
        httpLog.setType(event.getType());
        httpLog.setMethod(event.getMethod());
        httpLog.setStatus(event.getStatus());
        httpLog.setPath(event.getPath());
        httpLog.setQueryParams(event.getQueryParams());
        httpLog.setRequestBody(event.getRequestBody());
        httpLog.setResponseBody(event.getResponseBody());

        return httpLog;
    }

}
