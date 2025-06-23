package org.devquality.services;

import org.devquality.persistence.entites.User;
import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.devquality.web.dtos.users.response.CreateUserResponse;

import java.sql.SQLException;
import java.util.List;

public interface IUserService {
    CreateUserResponse user(CreaterUserRequest user) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    User getUserById(Long id) throws SQLException;}
