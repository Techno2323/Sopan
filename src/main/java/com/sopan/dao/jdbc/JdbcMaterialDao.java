package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.MaterialDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Material;
import com.sopan.model.enums.MaterialType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMaterialDao extends BaseJdbcDao implements MaterialDao {

    public JdbcMaterialDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Material> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find material by id");
    }

    @Override
    public Optional<Material> findById(Connection conn, Integer id) {
        String sql = "SELECT material_id, concept_id, title, type, location, created_at FROM materials WHERE material_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMaterial(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find material: " + id, e);
        }
    }

    @Override
    public List<Material> findByConceptId(int conceptId) {
        return execute(conn -> findByConceptId(conn, conceptId), "Failed to find concept materials");
    }

    @Override
    public List<Material> findByConceptId(Connection conn, int conceptId) {
        String sql = "SELECT material_id, concept_id, title, type, location, created_at FROM materials WHERE concept_id = ? ORDER BY created_at ASC";
        List<Material> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMaterial(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find materials for concept: " + conceptId, e);
        }
    }

    @Override
    public List<Material> findAll() {
        return execute(this::findAll, "Failed to find all materials");
    }

    @Override
    public List<Material> findAll(Connection conn) {
        String sql = "SELECT material_id, concept_id, title, type, location, created_at FROM materials ORDER BY created_at DESC";
        List<Material> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapMaterial(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list materials", e);
        }
    }

    @Override
    public Integer save(Material material) {
        return execute(conn -> save(conn, material), "Failed to save material");
    }

    @Override
    public Integer save(Connection conn, Material material) {
        String sql = "INSERT INTO materials (concept_id, title, type, location) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, material.getConceptId());
            stmt.setString(2, material.getTitle());
            stmt.setString(3, material.getType().name());
            stmt.setString(4, material.getLocation());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    material.setMaterialId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain material ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert material: " + material.getTitle(), e);
        }
    }

    @Override
    public void update(Material material) {
        executeVoid(conn -> update(conn, material), "Failed to update material");
    }

    @Override
    public void update(Connection conn, Material material) {
        String sql = "UPDATE materials SET title = ?, type = ?, location = ? WHERE material_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, material.getTitle());
            stmt.setString(2, material.getType().name());
            stmt.setString(3, material.getLocation());
            stmt.setInt(4, material.getMaterialId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update material: " + material.getMaterialId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete material");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM materials WHERE material_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete material: " + id, e);
        }
    }

    private Material mapMaterial(ResultSet rs) throws SQLException {
        Material m = new Material();
        m.setMaterialId(rs.getInt("material_id"));
        m.setConceptId(rs.getInt("concept_id"));
        m.setTitle(rs.getString("title"));
        m.setType(MaterialType.valueOf(rs.getString("type")));
        m.setLocation(rs.getString("location"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            m.setCreatedAt(ts.toLocalDateTime());
        }
        return m;
    }
}
