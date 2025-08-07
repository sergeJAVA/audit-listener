package com.webbee.audit_listener.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Класс, который используют для отправки лога о методе в формате JSON в Kafka.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MethodLogEvent {

    private String localDateTime;
    private String logLevel;
    private String correlationId;
    private String methodName;
    private String args;
    private String logType;
    private Object result;
    private String exceptionMessage;

}
