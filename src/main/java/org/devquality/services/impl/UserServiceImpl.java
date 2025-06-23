package org.devquality.services.impl;

import org.devquality.persistence.entites.User;
import org.devquality.persistence.repositories.IUserRepository;
import org.devquality.persistence.repositories.impl.UserRepositoryImpl;
import org.devquality.services.IUserService;
import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.devquality.web.dtos.users.response.CreateUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class UserServiceImpl implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final IUserRepository userRepository;

    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CreateUserResponse createUser(CreaterUserRequest user) throws SQLException {
        logger.debug("📝 Procesando creación de usuario: {}", user.getEmail());

        // Validación adicional a nivel de servicio
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }

        // Verificar si el usuario ya existe (si el repository tiene este método)
        if (userRepository instanceof UserRepositoryImpl) {
            UserRepositoryImpl repo = (UserRepositoryImpl) userRepository;
            if (repo.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Ya existe un usuario con ese email");
            }
        }

        // Guardar usuario
        User userSaved = userRepository.save(user);

        // Crear respuesta
        CreateUserResponse response = CreateUserResponse.builder()
                .email(userSaved.getEmail())
                .name(userSaved.getName())
                .build();

        logger.info("✅ Usuario creado exitosamente: {}", userSaved.getEmail());
        return response;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        logger.debug("📋 Obteniendo todos los usuarios desde el servicio");

        List<User> users = userRepository.findAllUsers();

        logger.info("✅ {} usuarios obtenidos desde el servicio", users.size());
        return users;
    }

    @Override
    public User getUserById(Long id) throws SQLException {
        logger.debug("🔍 Obteniendo usuario por ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo");
        }

        // Verificar si el repository tiene el método findById
        if (userRepository instanceof UserRepositoryImpl) {
            UserRepositoryImpl repo = (UserRepositoryImpl) userRepository;
            User user = repo.findById(id);

            if (user == null) {
                logger.warn("❌ Usuario con ID {} no encontrado", id);
                return null;
            }

            logger.info("✅ Usuario encontrado: {} - {}", user.getId(), user.getEmail());
            return user;
        } else {
            throw new UnsupportedOperationException("Método findById no disponible en esta implementación del repository");
        }
    }
}