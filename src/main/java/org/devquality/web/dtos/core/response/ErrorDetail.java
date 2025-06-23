package org.devquality.web.dtos.core.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ErrorDetail {
    private String field;
    private String code;
    private String message;
    private Object rejectedValue;
    private String type;

    public static ErrorDetail field(String field, String message) {
        return ErrorDetail.builder()
                .field(field)
                .message(message)
                .type("FIELD_ERROR")
                .build();
    }

    public static ErrorDetail validation(String field, String code, String message, Object rejectedValue) {
        return ErrorDetail.builder()
                .field(field)
                .code(code)
                .message(message)
                .rejectedValue(rejectedValue)
                .type("VALIDATION_ERROR")
                .build();
    }

    public static ErrorDetail simple(String message) {
        return ErrorDetail.builder()
                .message(message)
                .type("GENERAL_ERROR")
                .build();
    }
}