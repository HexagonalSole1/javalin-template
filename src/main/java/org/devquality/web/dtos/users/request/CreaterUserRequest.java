package org.devquality.web.dtos.users.request;

import lombok.Getter;
import lombok.Setter;
import org.devquality.web.validators.groups.ValidationGroups;

import jakarta.validation.constraints.*;

@Getter
@Setter
public class CreaterUserRequest {

    @NotNull(message = "El nombre es obligatorio", groups = ValidationGroups.Basic.class)
    @NotBlank(message = "El nombre no puede estar vacío", groups = ValidationGroups.Basic.class)
    @Size(
            min = 2,
            max = 255,
            message = "El nombre debe tener entre 2 y 255 caracteres",
            groups = ValidationGroups.Create.class
    )
    @Pattern(
            regexp = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1\\s'-]+$",
            message = "El nombre solo puede contener letras, espacios, guiones y apostrofes",
            groups = ValidationGroups.Create.class
    )
    private String name;

    @NotNull(message = "El email es obligatorio", groups = ValidationGroups.Basic.class)
    @NotBlank(message = "El email no puede estar vacío", groups = ValidationGroups.Basic.class)
    @Email(
            message = "El email debe tener un formato válido",
            groups = ValidationGroups.Create.class
    )
    @Size(
            max = 255,
            message = "El email no puede exceder 255 caracteres",
            groups = ValidationGroups.Create.class
    )
    private String email;
}