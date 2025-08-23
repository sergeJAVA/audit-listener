package com.webbee.audit_listener.util;

import com.webbee.audit_listener.converter.LongToLocalDateTimeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Profile("test")
@Configuration
public abstract class TestContainer {

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka"));

    @Container
    private static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("elasticsearch:9.1.2")
                    .withExposedPorts(9200)
                    .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("audit.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    @Configuration
    public static class TestElasticsearchConfig {
        @Bean
        public ElasticsearchCustomConversions elasticsearchCustomConversions() {
            return new ElasticsearchCustomConversions(
                    java.util.List.of(new LongToLocalDateTimeConverter())
            );
        }
    }

}
