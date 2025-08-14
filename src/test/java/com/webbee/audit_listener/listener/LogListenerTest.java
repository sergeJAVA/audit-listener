package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.event.MethodLogEvent;
import com.webbee.audit_listener.model.MethodLog;
import com.webbee.audit_listener.repository.MethodLogRepository;
import com.webbee.audit_listener.service.MethodLogService;
import com.webbee.audit_listener.util.TestContainer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


@SpringBootTest
@ActiveProfiles("test")
class LogListenerTest extends TestContainer{

    @Autowired
    private MethodLogRepository methodLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private MethodLogService methodLogService;

    private KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-id");

        producer = new KafkaProducer<>(props);
        producer.initTransactions();

        methodLogRepository.deleteAll();
    }

    @Test
    void handle_ShouldProcessExactlyOnce() throws Exception {

        doThrow(new RuntimeException("Тестовая ошибка при обработке"))
                .doCallRealMethod()
                .when(methodLogService).saveMethodLog(any(MethodLog.class));

        MethodLogEvent event = new MethodLogEvent();
        event.setLocalDateTime("2025-08-08 12:00:00.000");
        event.setMethodName("testMethod");
        event.setLogLevel("INFO");
        event.setCorrelationId("corr-123");
        event.setLogType("TEST");

        String messageValue = objectMapper.writeValueAsString(event);

        producer.beginTransaction();
        producer.send(new ProducerRecord<>("audit-log", "1", messageValue));
        producer.commitTransaction();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(methodLogService, times(1)).saveMethodLog(any(MethodLog.class));
        });
        // Проверка, что БД пустая
        assertThat(methodLogRepository.count()).isZero();

        // Кафка должна доставить сообщение повторно, а сервис его обработать
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(methodLogRepository.count()).isEqualTo(1);
        });

        verify(methodLogService, times(2)).saveMethodLog(any(MethodLog.class));

        MethodLog savedLog = methodLogRepository.findAll().getFirst();
        assertThat(savedLog.getMethodName()).isEqualTo("testMethod");
    }

}