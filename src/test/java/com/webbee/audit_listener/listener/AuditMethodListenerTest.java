package com.webbee.audit_listener.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.audit_listener.document.AuditMethod;
import com.webbee.audit_listener.event.MethodLogEvent;
import com.webbee.audit_listener.processor.AuditMethodProcessor;
import com.webbee.audit_listener.repository.AuditMethodRepository;
import com.webbee.audit_listener.util.TestContainer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuditMethodListenerTest extends TestContainer{

    @Autowired
    private AuditMethodRepository auditMethodRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private AuditMethodProcessor auditMethodProcessor;

    private KafkaConsumer<String, String> consumer;

    private KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-id" + UUID.randomUUID());

        Properties propsConsumer = new Properties();
        propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());

        consumer = new KafkaConsumer<>(propsConsumer);
        consumer.subscribe(List.of("audit-error"));

        producer = new KafkaProducer<>(props);
        producer.initTransactions();

        auditMethodRepository.deleteAll();
    }

    @Test
    void handle_ExceptionWhileProcessing_ShouldSendToErrorTopic() throws Exception {

        doThrow(new RuntimeException("Тестовая ошибка"))
                .doCallRealMethod()
                .when(auditMethodProcessor)
                .process(any(MethodLogEvent.class));

        MethodLogEvent event = new MethodLogEvent();
        event.setLocalDateTime("2025-08-08 12:00:00.000");
        event.setMethodName("testMethod");
        event.setLogLevel("INFO");
        event.setCorrelationId("corr-123");
        event.setLogType("TEST");

        String messageValue = objectMapper.writeValueAsString(event);

        producer.beginTransaction();
        producer.send(new ProducerRecord<>("audit-methods", "1", messageValue));
        producer.commitTransaction();
        producer.close();

        // проверяем, что ничего не сохранилось
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(auditMethodRepository.count()).isZero()
        );

        // проверяем, что в audit-error пришло сообщение
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                assertThat(record.value()).isEqualTo(messageValue);
            }
            consumer.close();
        });

        // убедимся, что retry'я не было и в Elasticsearch ничего не сохранилось
        assertThat(auditMethodRepository.count()).isZero();
    }

    @Test
    void handle_ShouldProcessExactlyOnce_Success() throws Exception {
        MethodLogEvent event = new MethodLogEvent();
        event.setLocalDateTime("2025-08-08 12:00:00.000");
        event.setMethodName("testMethod");
        event.setLogLevel("INFO");
        event.setCorrelationId("corr-123");
        event.setLogType("TEST");

        String messageValue = objectMapper.writeValueAsString(event);

        producer.beginTransaction();
        producer.send(new ProducerRecord<>("audit-methods", "1", messageValue));
        producer.commitTransaction();
        producer.close();

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(auditMethodProcessor, times(1)).process(any(MethodLogEvent.class));
            assertThat(auditMethodRepository.count()).isOne();
        });

        AuditMethod saved = auditMethodRepository.findAll().iterator().next();
        assertEquals(event.getMethodName(), saved.getMethodName());
        consumer.close();
    }

}