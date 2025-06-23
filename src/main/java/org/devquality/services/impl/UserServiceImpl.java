package org.devquality.services.impl;

import org.devquality.persistence.entites.User;
import org.devquality.persistence.repositories.IUserRepository;
import org.devquality.services.IUserService;
import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.devquality.web.dtos.users.response.CreateUserResponse;

import java.sql.SQLException;

public class UserServiceImpl implements IUserService {
    private IUserRepository userRepository;

    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CreateUserResponse user(CreaterUserRequest user) throws SQLException {

       User userSaved =  userRepository.save(user);

       CreateUserResponse response = CreateUserResponse.builder()
               .email(userSaved.getEmail())
               .name(userSaved.getName()).build();

        return response;
    }
}
