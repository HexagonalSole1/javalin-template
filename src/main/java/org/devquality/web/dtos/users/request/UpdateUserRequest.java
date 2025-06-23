package org.devquality.web.dtos.users.request;

import lombok.Getter;
import lombok.Setter;
import org.devquality.web.validators.groups.ValidationGroups;

import jakarta.validation.constraints.*;

@Getter
@Setter
public class UpdateUserRequest {

    @NotNull(message = "El ID es obligatorio", groups = ValidationGroups.Update.class)
    @Positive(message = "El ID debe ser positivo", groups = ValidationGroups.Update.class)
    private Long id;

    @Size(
            min = 2,
            max = 255,
            message = "El nombre debe tener entre 2 y 255 caracteres",
            groups = ValidationGroups.Update.class
    )
    @Pattern(
            regexp = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1\\s'-]+$",
            message = "El nombre solo puede contener letras, espacios, guiones y apostrofes",
            groups = ValidationGroups.Update.class
    )
    private String name;

    @Email(
            message = "El email debe tener un formato válido",
            groups = ValidationGroups.Update.class
    )
    @Size(
            max = 255,
            message = "El email no puede exceder 255 caracteres",
            groups = ValidationGroups.Update.class
    )
    private String email;
}