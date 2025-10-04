package com.vikk.appointmentscheduler.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseManager.
 */
class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        databaseManager = DatabaseManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    @Test
    @DisplayName("Test singleton pattern")
    void testSingletonPattern() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();
        
        assertSame(instance1, instance2, "DatabaseManager should be singleton");
    }

    @Test
    @DisplayName("Test database connection")
    void testGetConnection() throws Exception {
        try (Connection connection = databaseManager.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
        }
    }

    @Test
    @DisplayName("Test database initialization")
    void testDatabaseInitialization() throws Exception {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Test appointments table
            ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='appointments'");
            assertTrue(rs.next(), "Appointments table should exist");
            
            // Test resources table
            rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='resources'");
            assertTrue(rs.next(), "Resources table should exist");
            
            // Test appointment_capabilities table
            rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='appointment_capabilities'");
            assertTrue(rs.next(), "Appointment capabilities table should exist");
            
            // Test resource_capabilities table
            rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='resource_capabilities'");
            assertTrue(rs.next(), "Resource capabilities table should exist");
        }
    }

    @Test
    @DisplayName("Test multiple connections")
    void testMultipleConnections() throws Exception {
        try (Connection conn1 = databaseManager.getConnection();
             Connection conn2 = databaseManager.getConnection()) {
            
            assertNotNull(conn1, "First connection should not be null");
            assertNotNull(conn2, "Second connection should not be null");
            assertNotSame(conn1, conn2, "Connections should be different instances");
            assertFalse(conn1.isClosed(), "First connection should be open");
            assertFalse(conn2.isClosed(), "Second connection should be open");
        }
    }

    @Test
    @DisplayName("Test connection after shutdown")
    void testConnectionAfterShutdown() throws Exception {
        databaseManager.shutdown();
        
        // Should still be able to get new connections after shutdown
        try (Connection connection = databaseManager.getConnection()) {
            assertNotNull(connection, "Should be able to get connection after shutdown");
            assertFalse(connection.isClosed(), "New connection should be open");
        }
    }

    @Test
    @DisplayName("Test database schema integrity")
    void testDatabaseSchemaIntegrity() throws Exception {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Test appointments table structure
            ResultSet rs = statement.executeQuery("PRAGMA table_info(appointments)");
            int columnCount = 0;
            while (rs.next()) {
                columnCount++;
            }
            assertTrue(columnCount > 0, "Appointments table should have columns");
            
            // Test resources table structure
            rs = statement.executeQuery("PRAGMA table_info(resources)");
            columnCount = 0;
            while (rs.next()) {
                columnCount++;
            }
            assertTrue(columnCount > 0, "Resources table should have columns");
        }
    }
}
