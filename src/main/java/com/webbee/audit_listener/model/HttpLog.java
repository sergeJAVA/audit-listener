package com.webbee.audit_listener.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Сущность HttpLog, которая создаётся на основе HttpLogEvent.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "http_logs")
public class HttpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "type")
    private String type;

    @Column(name = "method")
    private String method;

    @Column(name = "status")
    private Integer status;

    @Column(name = "path")
    private String path;

    @ElementCollection
    @CollectionTable(name = "http_log_query_params", joinColumns = @JoinColumn(name = "http_log_id"))
    @MapKeyColumn(name = "param_key")
    @Column(name = "param_value")
    private Map<String, String> queryParams;

    @Column(name = "requestBody", columnDefinition = "text")
    private String requestBody;

    @Column(name = "responseBody", columnDefinition = "text")
    private String responseBody;

}
