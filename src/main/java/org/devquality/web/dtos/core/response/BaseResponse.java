package org.devquality.web.dtos.core.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ErrorDetail> errors;
    private ResponseMetadata metadata;
    private Long timestamp;

    // Respuesta exitosa sin metadata
    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Respuesta exitosa con metadata
    public static <T> BaseResponse<T> success(T data, String message, ResponseMetadata metadata) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Respuesta de error simple
    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Respuesta de error con detalles
    public static <T> BaseResponse<T> error(String message, List<ErrorDetail> errors) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Respuesta de error de validación
    public static <T> BaseResponse<T> validationError(String message, List<ErrorDetail> errors) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .metadata(ResponseMetadata.basic("VALIDATION_ERROR"))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Respuesta exitosa sin datos
    public static BaseResponse<Void> success(String message) {
        return BaseResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}