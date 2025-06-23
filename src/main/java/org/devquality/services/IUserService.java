package org.devquality.services;

import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.devquality.web.dtos.users.response.CreateUserResponse;

import java.sql.SQLException;

public interface IUserService {
    CreateUserResponse user(CreaterUserRequest user) throws SQLException;
}
