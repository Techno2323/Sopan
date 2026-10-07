package com.sopan.config;

import com.sopan.exception.DaoException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Runs a unit of work inside a single JDBC transaction.
 *
 * <p>The Connection is set to manual commit, passed to the work function,
 * committed on success, rolled back on any exception, and always closed.
 * Supports throwing custom domain-specific checked exceptions.</p>
 */
public final class TransactionManager {

    @FunctionalInterface
    public interface TxCallable<R, E extends Exception> {
        R execute(Connection conn) throws E, SQLException;
    }

    @FunctionalInterface
    public interface TxRunnable<E extends Exception> {
        void execute(Connection conn) throws E, SQLException;
    }

    private final ConnectionProvider provider;

    public TransactionManager(ConnectionProvider provider) {
        this.provider = provider;
    }

    /**
     * Executes work inside a transaction, returning its result.
     *
     * @throws DaoException wrapping any SQLException
     * @throws E any checked exception from the work function
     */
    public <R, E extends Exception> R inTransaction(TxCallable<R, E> work) throws E {
        Connection conn = null;
        try {
            conn = provider.getConnection();
            conn.setAutoCommit(false);

            R result = work.execute(conn);

            conn.commit();
            return result;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new DaoException("Transaction failed", e);
        } catch (RuntimeException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(conn);
            @SuppressWarnings("unchecked")
            E checked = (E) e;
            throw checked;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Executes void work inside a transaction.
     */
    public <E extends Exception> void inTransactionVoid(TxRunnable<E> work) throws E {
        inTransaction(conn -> {
            work.execute(conn);
            return null;
        });
    }

    /** Returns a fresh connection for read-only, single-statement work. */
    public Connection getConnection() {
        try {
            return provider.getConnection();
        } catch (SQLException e) {
            throw new DaoException("Could not obtain connection", e);
        }
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) { /* logged elsewhere */ }
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) { /* logged elsewhere */ }
        }
    }
}
