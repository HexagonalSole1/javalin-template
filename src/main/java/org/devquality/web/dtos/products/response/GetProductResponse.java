package org.devquality.web.dtos.products.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class GetProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String createdAt;
    private String updatedAt;
}