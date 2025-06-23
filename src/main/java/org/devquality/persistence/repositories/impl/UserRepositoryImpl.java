package org.devquality.persistence.repositories.impl;

import org.devquality.Main;
import org.devquality.config.DatabaseConfig;
import org.devquality.persistence.entites.User;
import org.devquality.persistence.repositories.IUserRepository;
import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;



public class UserRepositoryImpl implements IUserRepository {
    private final DatabaseConfig databaseConfig;
    // SQL Queries
    private static final String INSERT_USER =
            "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id, created_at, updated_at";

    private static final String SELECT_ALL_USERS =
            "SELECT id, name, email, created_at, updated_at FROM users ORDER BY created_at DESC";

    private static final String SELECT_USER_BY_ID =
            "SELECT id, name, email, created_at, updated_at FROM users WHERE id = ?";

    private static final String SELECT_USER_BY_EMAIL =
            "SELECT id, name, email, created_at, updated_at FROM users WHERE email = ?";

    private static final String UPDATE_USER =
            "UPDATE users SET name = ?, email = ? WHERE id = ? RETURNING updated_at";

    private static final String DELETE_USER_BY_ID =
            "DELETE FROM users WHERE id = ?";

    private static final String EXISTS_BY_ID =
            "SELECT 1 FROM users WHERE id = ? LIMIT 1";

    private static final String EXISTS_BY_EMAIL =
            "SELECT 1 FROM users WHERE email = ? LIMIT 1";

    private static final String COUNT_USERS =
            "SELECT COUNT(*) FROM users";

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public UserRepositoryImpl(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }


    @Override
    public ArrayList<User> findAllUsers() throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        Connection connection =  databaseConfig.getConnection();
        return users;
    }

    @Override
    public User save(CreaterUserRequest userRequest) throws SQLException {
        logger.debug("Guardando usuario: {}", userRequest.getEmail());

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {

            stmt.setString(1, userRequest.getName());
            stmt.setString(2, userRequest.getEmail());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // ✅ Crear el objeto User con los datos devueltos por la BD
                    User savedUser = new User();
                    savedUser.setId(rs.getLong("id"));
                    savedUser.setName(userRequest.getName());
                    savedUser.setEmail(userRequest.getEmail());
                    savedUser.setCreatedAt(String.valueOf(rs.getTimestamp("created_at").toLocalDateTime()));
                    savedUser.setUpdatedAt(String.valueOf(rs.getTimestamp("updated_at").toLocalDateTime()));

                    logger.info("✅ Usuario guardado con ID: {}", savedUser.getId());
                    return savedUser; // ✅ Retornar el User, no el request
                } else {
                    throw new SQLException("No se pudo obtener el ID del usuario creado");
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error al guardar usuario: {}", e.getMessage());
            throw e;
        }
    }
}
