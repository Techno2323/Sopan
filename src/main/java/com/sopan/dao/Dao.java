package com.sopan.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Generic DAO interface defining standard CRUD operations with connection-aware overloads.
 *
 * @param <T> entity type
 * @param <ID> identifier type
 */
public interface Dao<T, ID> {

    Optional<T> findById(ID id);
    Optional<T> findById(Connection conn, ID id);

    List<T> findAll();
    List<T> findAll(Connection conn);

    ID save(T t);
    ID save(Connection conn, T t);

    void update(T t);
    void update(Connection conn, T t);

    void delete(ID id);
    void delete(Connection conn, ID id);
}
