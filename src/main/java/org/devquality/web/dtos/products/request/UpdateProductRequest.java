package org.devquality.web.dtos.products.request;

import lombok.Getter;
import lombok.Setter;
import org.devquality.web.validators.groups.ValidationGroups;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {

    @NotNull(message = "El ID es obligatorio", groups = ValidationGroups.Update.class)
    @Positive(message = "El ID debe ser positivo", groups = ValidationGroups.Update.class)
    private Long id;

    @Size(
            min = 2,
            max = 255,
            message = "El nombre debe tener entre 2 y 255 caracteres",
            groups = ValidationGroups.Update.class
    )
    private String name;

    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "El precio debe ser mayor a 0",
            groups = ValidationGroups.Update.class
    )
    @DecimalMax(
            value = "999999.99",
            message = "El precio no puede exceder 999,999.99",
            groups = ValidationGroups.Update.class
    )
    @Digits(
            integer = 8,
            fraction = 2,
            message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales",
            groups = ValidationGroups.Update.class
    )
    private BigDecimal price;

    @Size(
            max = 1000,
            message = "La descripción no puede exceder 1000 caracteres",
            groups = ValidationGroups.Update.class
    )
    private String description;
}