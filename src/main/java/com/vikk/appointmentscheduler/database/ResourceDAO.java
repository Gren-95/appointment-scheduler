package com.vikk.appointmentscheduler.database;

import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.ResourceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Data Access Object for Resource entities.
 */
public class ResourceDAO {
    private static final Logger logger = Logger.getLogger(ResourceDAO.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DatabaseManager databaseManager;

    public ResourceDAO() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void save(Resource resource) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO resources 
            (id, name, resource_type, cost_per_hour, capacity, is_active, 
             available_from, available_to, setup_time_minutes, cleanup_time_minutes, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, resource.getId());
            stmt.setString(2, resource.getName());
            stmt.setString(3, resource.getType() != null ? 
                resource.getType().name() : null);
            stmt.setDouble(4, resource.getCostPerHour());
            stmt.setInt(5, resource.getCapacity());
            stmt.setBoolean(6, resource.isActive());
            stmt.setString(7, resource.getAvailableFrom() != null ? 
                resource.getAvailableFrom().format(FORMATTER) : null);
            stmt.setString(8, resource.getAvailableTo() != null ? 
                resource.getAvailableTo().format(FORMATTER) : null);
            stmt.setLong(9, resource.getSetupTime() != null ? 
                resource.getSetupTime().toMinutes() : 0);
            stmt.setLong(10, resource.getCleanupTime() != null ? 
                resource.getCleanupTime().toMinutes() : 0);

            stmt.executeUpdate();
            
            // Save capabilities
            saveCapabilities(resource.getId(), resource.getCapabilities());
            
            logger.info("Saved resource: " + resource.getId());
        }
    }

    public Resource findById(String id) throws SQLException {
        String sql = "SELECT * FROM resources WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToResource(rs);
            }
        }
        return null;
    }

    public List<Resource> findAll() throws SQLException {
        String sql = "SELECT * FROM resources ORDER BY name";
        List<Resource> resources = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Resource resource = mapResultSetToResource(rs);
                // Load capabilities using the same connection
                resource.setCapabilities(getCapabilities(conn, resource.getId()));
                resources.add(resource);
            }
        }
        return resources;
    }

    public List<Resource> findByType(ResourceType type) throws SQLException {
        String sql = "SELECT * FROM resources WHERE resource_type = ? ORDER BY name";
        List<Resource> resources = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Resource resource = mapResultSetToResource(rs);
                // Load capabilities using the same connection
                resource.setCapabilities(getCapabilities(conn, resource.getId()));
                resources.add(resource);
            }
        }
        return resources;
    }

    public void update(Resource resource) throws SQLException {
        String sql = """
            UPDATE resources SET 
                name = ?, resource_type = ?, cost_per_hour = ?, capacity = ?, 
                is_active = ?, available_from = ?, available_to = ?, 
                setup_time_minutes = ?, cleanup_time_minutes = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, resource.getName());
            stmt.setString(2, resource.getType() != null ? 
                resource.getType().name() : null);
            stmt.setDouble(3, resource.getCostPerHour());
            stmt.setInt(4, resource.getCapacity());
            stmt.setBoolean(5, resource.isActive());
            stmt.setString(6, resource.getAvailableFrom() != null ? 
                resource.getAvailableFrom().format(FORMATTER) : null);
            stmt.setString(7, resource.getAvailableTo() != null ? 
                resource.getAvailableTo().format(FORMATTER) : null);
            stmt.setLong(8, resource.getSetupTime() != null ? 
                resource.getSetupTime().toMinutes() : 0);
            stmt.setLong(9, resource.getCleanupTime() != null ? 
                resource.getCleanupTime().toMinutes() : 0);
            stmt.setString(10, resource.getId());

            stmt.executeUpdate();
            
            // Update capabilities
            deleteCapabilities(resource.getId());
            saveCapabilities(resource.getId(), resource.getCapabilities());
            
            logger.info("Updated resource: " + resource.getId());
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM resources WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.executeUpdate();
            
            // Delete capabilities
            deleteCapabilities(id);
            
            logger.info("Deleted resource: " + id);
        }
    }

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM resources";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.executeUpdate();
            
            // Delete all capabilities
            String deleteCapabilitiesSql = "DELETE FROM resource_capabilities";
            try (PreparedStatement capStmt = conn.prepareStatement(deleteCapabilitiesSql)) {
                capStmt.executeUpdate();
            }
            
            logger.info("Deleted all resources");
        }
    }

    private void saveCapabilities(String resourceId, Set<String> capabilities) throws SQLException {
        if (capabilities == null || capabilities.isEmpty()) return;
        
        String sql = "INSERT OR IGNORE INTO resource_capabilities (resource_id, capability) VALUES (?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (String capability : capabilities) {
                stmt.setString(1, resourceId);
                stmt.setString(2, capability);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void deleteCapabilities(String resourceId) throws SQLException {
        String sql = "DELETE FROM resource_capabilities WHERE resource_id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, resourceId);
            stmt.executeUpdate();
        }
    }

    private Set<String> getCapabilities(String resourceId) throws SQLException {
        try (Connection conn = databaseManager.getConnection()) {
            return getCapabilities(conn, resourceId);
        }
    }
    
    private Set<String> getCapabilities(Connection conn, String resourceId) throws SQLException {
        String sql = "SELECT capability FROM resource_capabilities WHERE resource_id = ?";
        Set<String> capabilities = new HashSet<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, resourceId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                capabilities.add(rs.getString("capability"));
            }
        }
        return capabilities;
    }

    private Resource mapResultSetToResource(ResultSet rs) throws SQLException {
        Resource resource = new Resource();
        
        resource.setId(rs.getString("id"));
        resource.setName(rs.getString("name"));
        
        String typeStr = rs.getString("resource_type");
        if (typeStr != null) {
            resource.setType(ResourceType.valueOf(typeStr));
        }
        
        resource.setCostPerHour(rs.getDouble("cost_per_hour"));
        resource.setCapacity(rs.getInt("capacity"));
        resource.setActive(rs.getBoolean("is_active"));
        
        String availableFromStr = rs.getString("available_from");
        if (availableFromStr != null) {
            resource.setAvailableFrom(LocalDateTime.parse(availableFromStr, FORMATTER));
        }
        
        String availableToStr = rs.getString("available_to");
        if (availableToStr != null) {
            resource.setAvailableTo(LocalDateTime.parse(availableToStr, FORMATTER));
        }
        
        long setupMinutes = rs.getLong("setup_time_minutes");
        resource.setSetupTime(Duration.ofMinutes(setupMinutes));
        
        long cleanupMinutes = rs.getLong("cleanup_time_minutes");
        resource.setCleanupTime(Duration.ofMinutes(cleanupMinutes));
        
        // Load capabilities
        resource.setCapabilities(getCapabilities(resource.getId()));
        
        return resource;
    }
}
