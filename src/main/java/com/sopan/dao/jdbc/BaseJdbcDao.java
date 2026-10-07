package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.exception.DaoException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base class for JDBC DAOs providing connection lifecycle management for non-transactional calls.
 */
public abstract class BaseJdbcDao {

    protected final ConnectionProvider connectionProvider;

    protected BaseJdbcDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(Connection conn) throws SQLException;
    }

    protected <T> T execute(SqlFunction<T> function, String errorMessage) {
        try (Connection conn = connectionProvider.getConnection()) {
            return function.apply(conn);
        } catch (SQLException e) {
            throw new DaoException(errorMessage, e);
        }
    }

    protected void executeVoid(SqlConsumer consumer, String errorMessage) {
        try (Connection conn = connectionProvider.getConnection()) {
            consumer.accept(conn);
        } catch (SQLException e) {
            throw new DaoException(errorMessage, e);
        }
    }
}
