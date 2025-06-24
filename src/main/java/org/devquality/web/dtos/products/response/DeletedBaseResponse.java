package org.devquality.web.dtos.products.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DeletedBaseResponse {
    private Long id;
    private String message;
    private boolean success;
    private String deletedAt;

    public static DeletedBaseResponse success(Long id, String message) {
        return DeletedBaseResponse.builder()
                .id(id)
                .message(message)
                .success(true)
                .deletedAt(java.time.LocalDateTime.now().toString())
                .build();
    }

    public static DeletedBaseResponse failure(Long id, String message) {
        return DeletedBaseResponse.builder()
                .id(id)
                .message(message)
                .success(false)
                .build();
    }
}