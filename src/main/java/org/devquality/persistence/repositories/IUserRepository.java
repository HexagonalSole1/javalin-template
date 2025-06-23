package org.devquality.persistence.repositories;

import org.devquality.persistence.entites.User;
import org.devquality.web.dtos.users.request.CreaterUserRequest;

import java.sql.SQLException;
import java.util.ArrayList;

public interface IUserRepository {
    ArrayList<User> findAllUsers() throws SQLException;

    User save(CreaterUserRequest user) throws SQLException;
}
