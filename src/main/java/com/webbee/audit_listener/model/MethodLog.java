package com.webbee.audit_listener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import java.time.LocalDateTime;

/**
 * Сущность MethodLog, которая создаётся на основе HttpLogEvent.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "method_logs")
public class MethodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "logLevel", nullable = false)
    private String logLevel;

    @Column(name = "correlationId", nullable = false)
    private String correlationId ;

    @Column(name = "methodName", nullable = false)
    private String methodName;

    @Column(name = "args")
    private String args;

    @Column(name = "logType", nullable = false)
    private String logType;

    @Type(JsonBinaryType.class)
    @Column(name = "result", columnDefinition = "jsonb")
    private Object result;

    @Column(name = "exceptionMessage")
    private String exceptionMessage;

}
