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
        logger.debug("🔍 Obteniendo todos los usuarios de la base de datos");

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_USERS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setCreatedAt(String.valueOf(rs.getTimestamp("created_at").toLocalDateTime()));
                user.setUpdatedAt(String.valueOf(rs.getTimestamp("updated_at").toLocalDateTime()));

                users.add(user);
                logger.debug("📋 Usuario cargado: {} - {}", user.getId(), user.getEmail());
            }

            logger.info("✅ {} usuarios obtenidos de la base de datos", users.size());
            return users;

        } catch (SQLException e) {
            logger.error("❌ Error al obtener usuarios: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public User save(CreaterUserRequest userRequest) throws SQLException {
        logger.debug("💾 Guardando usuario: {}", userRequest.getEmail());

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

    // 🆕 Métodos adicionales útiles

    /**
     * Buscar usuario por ID
     */
    @Override
    public User findById(Long id) throws SQLException {
        logger.debug("🔍 Buscando usuario por ID: {}", id);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_BY_ID)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setCreatedAt(String.valueOf(rs.getTimestamp("created_at").toLocalDateTime()));
                    user.setUpdatedAt(String.valueOf(rs.getTimestamp("updated_at").toLocalDateTime()));

                    logger.debug("✅ Usuario encontrado: {}", user.getEmail());
                    return user;
                } else {
                    logger.debug("❌ Usuario con ID {} no encontrado", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error al buscar usuario por ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Verificar si existe un usuario por email
     */
    @Override
    public boolean existsByEmail(String email) throws SQLException {
        logger.debug("🔍 Verificando si existe usuario con email: {}", email);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_BY_EMAIL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("📧 Email {} {} existe", email, exists ? "SÍ" : "NO");
                return exists;
            }
        } catch (SQLException e) {
            logger.error("❌ Error al verificar email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * Contar total de usuarios
     */
    @Override
    public long countUsers() throws SQLException {
        logger.debug("📊 Contando total de usuarios");

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_USERS);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                long count = rs.getLong(1);
                logger.debug("📊 Total de usuarios: {}", count);
                return count;
            }
            return 0;

        } catch (SQLException e) {
            logger.error("❌ Error al contar usuarios: {}", e.getMessage());
            throw e;
        }
    }
}