package com.vikk.appointmentscheduler.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Manages database connections and initialization.
 */
public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:appointment_scheduler.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        // Always return a fresh connection to avoid "connection closed" issues
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Create appointments table
            String createAppointmentsTable = """
                CREATE TABLE IF NOT EXISTS appointments (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT,
                    start_time TEXT NOT NULL,
                    end_time TEXT,
                    duration_minutes INTEGER NOT NULL,
                    appointment_type TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    resource_id TEXT,
                    client_id TEXT,
                    is_flexible BOOLEAN DEFAULT FALSE,
                    flexibility_window_minutes INTEGER DEFAULT 0,
                    importance_score REAL DEFAULT 1.0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """;

            // Create resources table
            String createResourcesTable = """
                CREATE TABLE IF NOT EXISTS resources (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    resource_type TEXT NOT NULL,
                    cost_per_hour REAL NOT NULL DEFAULT 0.0,
                    capacity INTEGER DEFAULT 1,
                    is_active BOOLEAN DEFAULT TRUE,
                    available_from TEXT,
                    available_to TEXT,
                    setup_time_minutes INTEGER DEFAULT 0,
                    cleanup_time_minutes INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """;

            // Create resource capabilities table
            String createResourceCapabilitiesTable = """
                CREATE TABLE IF NOT EXISTS resource_capabilities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    resource_id TEXT NOT NULL,
                    capability TEXT NOT NULL,
                    FOREIGN KEY (resource_id) REFERENCES resources (id) ON DELETE CASCADE,
                    UNIQUE(resource_id, capability)
                )
            """;

            // Create appointment required capabilities table
            String createAppointmentCapabilitiesTable = """
                CREATE TABLE IF NOT EXISTS appointment_capabilities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    appointment_id TEXT NOT NULL,
                    capability TEXT NOT NULL,
                    is_required BOOLEAN DEFAULT TRUE,
                    FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE CASCADE,
                    UNIQUE(appointment_id, capability)
                )
            """;

            // Create schedules table
            String createSchedulesTable = """
                CREATE TABLE IF NOT EXISTS schedules (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """;

            // Create schedule appointments table
            String createScheduleAppointmentsTable = """
                CREATE TABLE IF NOT EXISTS schedule_appointments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    schedule_id TEXT NOT NULL,
                    appointment_id TEXT NOT NULL,
                    actual_start_time TEXT,
                    actual_end_time TEXT,
                    assigned_resource_id TEXT,
                    FOREIGN KEY (schedule_id) REFERENCES schedules (id) ON DELETE CASCADE,
                    FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE CASCADE,
                    FOREIGN KEY (assigned_resource_id) REFERENCES resources (id) ON DELETE SET NULL
                )
            """;

            // Execute table creation
            stmt.execute(createAppointmentsTable);
            stmt.execute(createResourcesTable);
            stmt.execute(createResourceCapabilitiesTable);
            stmt.execute(createAppointmentCapabilitiesTable);
            stmt.execute(createSchedulesTable);
            stmt.execute(createScheduleAppointmentsTable);

            // Create indexes for better performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_start_time ON appointments(start_time)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_resources_type ON resources(resource_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_resources_active ON resources(is_active)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_schedule_appointments_schedule ON schedule_appointments(schedule_id)");

            logger.info("Database initialized successfully");
            
        } catch (SQLException e) {
            logger.severe("Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warning("Failed to close database connection: " + e.getMessage());
        }
    }

    public void shutdown() {
        closeConnection();
    }
}
