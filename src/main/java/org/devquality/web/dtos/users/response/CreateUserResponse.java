package org.devquality.web.dtos.users.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter

public class CreateUserResponse {

    private String name;
    private String email;
}
