package com.webbee.audit_listener.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Класс, который используют для отправки HTTP-лога в формате JSON в Kafka.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HttpLogEvent {

    private LocalDateTime timestamp;
    private String type;
    private String method;
    private int status;
    private String path;
    private Map<String, String> queryParams;
    private String requestBody;
    private String responseBody;

}
