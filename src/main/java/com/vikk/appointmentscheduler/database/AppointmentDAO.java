package com.vikk.appointmentscheduler.database;

import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.AppointmentStatus;
import com.vikk.appointmentscheduler.model.AppointmentType;
import com.vikk.appointmentscheduler.model.Priority;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Data Access Object for Appointment entities.
 */
public class AppointmentDAO {
    private static final Logger logger = Logger.getLogger(AppointmentDAO.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DatabaseManager databaseManager;

    public AppointmentDAO() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void save(Appointment appointment) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO appointments 
            (id, title, description, start_time, end_time, duration_minutes, 
             appointment_type, priority, status, resource_id, client_id, 
             is_flexible, flexibility_window_minutes, importance_score, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, appointment.getId());
            stmt.setString(2, appointment.getTitle());
            stmt.setString(3, appointment.getDescription());
            stmt.setString(4, appointment.getStartTime() != null ? 
                appointment.getStartTime().format(FORMATTER) : null);
            stmt.setString(5, appointment.getEndTime() != null ? 
                appointment.getEndTime().format(FORMATTER) : null);
            stmt.setLong(6, appointment.getDurationMinutes());
            stmt.setString(7, appointment.getType() != null ? 
                appointment.getType().name() : null);
            stmt.setString(8, appointment.getPriority() != null ? 
                appointment.getPriority().name() : null);
            stmt.setString(9, appointment.getStatus() != null ? 
                appointment.getStatus().name() : AppointmentStatus.PENDING.name());
            stmt.setString(10, appointment.getResourceId());
            stmt.setString(11, appointment.getClientId());
            stmt.setBoolean(12, appointment.isFlexible());
            stmt.setLong(13, appointment.getFlexibilityWindow() != null ? 
                appointment.getFlexibilityWindow().toMinutes() : 0);
            stmt.setDouble(14, appointment.getImportanceScore());

            stmt.executeUpdate();
            
            // Save required capabilities
            saveCapabilities(appointment.getId(), appointment.getRequiredCapabilities(), true);
            saveCapabilities(appointment.getId(), appointment.getPreferredCapabilities(), false);
            
            logger.info("Saved appointment: " + appointment.getId());
        }
    }

    public Appointment findById(String id) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAppointment(rs);
            }
        }
        return null;
    }

    public List<Appointment> findAll() throws SQLException {
        String sql = "SELECT * FROM appointments ORDER BY start_time";
        List<Appointment> appointments = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Appointment appointment = mapResultSetToAppointment(rs);
                // Load capabilities using the same connection
                appointment.setRequiredCapabilities(getCapabilities(conn, appointment.getId(), true));
                appointment.setPreferredCapabilities(getCapabilities(conn, appointment.getId(), false));
                appointments.add(appointment);
            }
        }
        return appointments;
    }

    public void update(Appointment appointment) throws SQLException {
        String sql = """
            UPDATE appointments SET 
                title = ?, description = ?, start_time = ?, end_time = ?, 
                duration_minutes = ?, appointment_type = ?, priority = ?, 
                status = ?, resource_id = ?, client_id = ?, is_flexible = ?, 
                flexibility_window_minutes = ?, importance_score = ?, 
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, appointment.getTitle());
            stmt.setString(2, appointment.getDescription());
            stmt.setString(3, appointment.getStartTime() != null ? 
                appointment.getStartTime().format(FORMATTER) : null);
            stmt.setString(4, appointment.getEndTime() != null ? 
                appointment.getEndTime().format(FORMATTER) : null);
            stmt.setLong(5, appointment.getDurationMinutes());
            stmt.setString(6, appointment.getType() != null ? 
                appointment.getType().name() : null);
            stmt.setString(7, appointment.getPriority() != null ? 
                appointment.getPriority().name() : null);
            stmt.setString(8, appointment.getStatus() != null ? 
                appointment.getStatus().name() : AppointmentStatus.PENDING.name());
            stmt.setString(9, appointment.getResourceId());
            stmt.setString(10, appointment.getClientId());
            stmt.setBoolean(11, appointment.isFlexible());
            stmt.setLong(12, appointment.getFlexibilityWindow() != null ? 
                appointment.getFlexibilityWindow().toMinutes() : 0);
            stmt.setDouble(13, appointment.getImportanceScore());
            stmt.setString(14, appointment.getId());

            stmt.executeUpdate();
            
            // Update capabilities
            deleteCapabilities(appointment.getId());
            saveCapabilities(appointment.getId(), appointment.getRequiredCapabilities(), true);
            saveCapabilities(appointment.getId(), appointment.getPreferredCapabilities(), false);
            
            logger.info("Updated appointment: " + appointment.getId());
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.executeUpdate();
            
            // Delete capabilities
            deleteCapabilities(id);
            
            logger.info("Deleted appointment: " + id);
        }
    }

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM appointments";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.executeUpdate();
            
            // Delete all capabilities
            String deleteCapabilitiesSql = "DELETE FROM appointment_capabilities";
            try (PreparedStatement capStmt = conn.prepareStatement(deleteCapabilitiesSql)) {
                capStmt.executeUpdate();
            }
            
            logger.info("Deleted all appointments");
        }
    }

    private void saveCapabilities(String appointmentId, Set<String> capabilities, boolean isRequired) throws SQLException {
        if (capabilities == null || capabilities.isEmpty()) return;
        
        String sql = "INSERT OR IGNORE INTO appointment_capabilities (appointment_id, capability, is_required) VALUES (?, ?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (String capability : capabilities) {
                stmt.setString(1, appointmentId);
                stmt.setString(2, capability);
                stmt.setBoolean(3, isRequired);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void deleteCapabilities(String appointmentId) throws SQLException {
        String sql = "DELETE FROM appointment_capabilities WHERE appointment_id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, appointmentId);
            stmt.executeUpdate();
        }
    }

    private Set<String> getCapabilities(String appointmentId, boolean isRequired) throws SQLException {
        try (Connection conn = databaseManager.getConnection()) {
            return getCapabilities(conn, appointmentId, isRequired);
        }
    }
    
    private Set<String> getCapabilities(Connection conn, String appointmentId, boolean isRequired) throws SQLException {
        String sql = "SELECT capability FROM appointment_capabilities WHERE appointment_id = ? AND is_required = ?";
        Set<String> capabilities = new HashSet<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, appointmentId);
            stmt.setBoolean(2, isRequired);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                capabilities.add(rs.getString("capability"));
            }
        }
        return capabilities;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        
        appointment.setId(rs.getString("id"));
        appointment.setTitle(rs.getString("title"));
        appointment.setDescription(rs.getString("description"));
        
        String startTimeStr = rs.getString("start_time");
        if (startTimeStr != null) {
            appointment.setStartTime(LocalDateTime.parse(startTimeStr, FORMATTER));
        }
        
        String endTimeStr = rs.getString("end_time");
        if (endTimeStr != null) {
            appointment.setEndTime(LocalDateTime.parse(endTimeStr, FORMATTER));
        }
        
        long durationMinutes = rs.getLong("duration_minutes");
        appointment.setDuration(Duration.ofMinutes(durationMinutes));
        
        String typeStr = rs.getString("appointment_type");
        if (typeStr != null) {
            appointment.setType(AppointmentType.valueOf(typeStr));
        }
        
        String priorityStr = rs.getString("priority");
        if (priorityStr != null) {
            appointment.setPriority(Priority.valueOf(priorityStr));
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            appointment.setStatus(AppointmentStatus.valueOf(statusStr));
        }
        
        appointment.setResourceId(rs.getString("resource_id"));
        appointment.setClientId(rs.getString("client_id"));
        appointment.setFlexible(rs.getBoolean("is_flexible"));
        
        long flexibilityMinutes = rs.getLong("flexibility_window_minutes");
        appointment.setFlexibilityWindow(Duration.ofMinutes(flexibilityMinutes));
        
        appointment.setImportanceScore(rs.getDouble("importance_score"));
        
        // Load capabilities
        appointment.setRequiredCapabilities(getCapabilities(appointment.getId(), true));
        appointment.setPreferredCapabilities(getCapabilities(appointment.getId(), false));
        
        return appointment;
    }
}
