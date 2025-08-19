package com.webbee.audit_listener.util;

import com.webbee.audit_listener.document.AuditMethod;
import com.webbee.audit_listener.document.AuditRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * Компонент для инициализации индексов Elasticsearch при запуске приложения.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${elasticsearch.recreate-indices:false}")
    private boolean recreateIndices;

    @PostConstruct
    public void initializeIndices() {
        log.info("Starting Elasticsearch index initialization...");
        processIndex(AuditMethod.class);
        processIndex(AuditRequest.class);
        log.info("Elasticsearch index initialization finished.");
    }

    /**
     * Обрабатывает создание индекса для указанного класса-документа.
     * @param documentClass Класс, аннотированный как @Document.
     */
    private void processIndex(Class<?> documentClass) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
        String indexName = indexOps.getIndexCoordinates().getIndexName();

        if (recreateIndices) {
            if (indexOps.exists()) {
                log.warn("Recreate flag is true. Deleting index: {}", indexName);
                indexOps.delete();
            }
            createIndexAndMapping(indexOps);
        } else {
            if (!indexOps.exists()) {
                log.info("Index {} does not exist. Creating...", indexName);
                createIndexAndMapping(indexOps);
            } else {
                log.info("Index {} already exists. Skipping creation.", indexName);
            }
        }
    }

    /**
     * Создает индекс и применяет к нему маппинг из аннотаций класса.
     * @param indexOps Объект для управления индексом.
     */
    private void createIndexAndMapping(IndexOperations indexOps) {
        String indexName = indexOps.getIndexCoordinates().getIndexName();
        try {
            indexOps.create();
            log.info("Index {} created successfully.", indexName);
            indexOps.putMapping();
            log.info("Mapping for index {} applied successfully.", indexName);
        } catch (Exception e) {
            log.error("Failed to create or map index {}: {}", indexName, e.getMessage());
        }
    }


}
