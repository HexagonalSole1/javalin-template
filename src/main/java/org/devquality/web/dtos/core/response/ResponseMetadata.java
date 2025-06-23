package org.devquality.web.dtos.core.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class ResponseMetadata {
    private String type;
    private Long totalElements;
    private Long executionTimeMs;
    private String version;
    private Long timestamp;

    public static ResponseMetadata basic(String type) {
        return ResponseMetadata.builder()
                .type(type)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ResponseMetadata withExecutionTime(String type, long executionTimeMs) {
        return ResponseMetadata.builder()
                .type(type)
                .executionTimeMs(executionTimeMs)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
