package com.vikk.appointmentscheduler.database;

import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.AppointmentStatus;
import com.vikk.appointmentscheduler.model.AppointmentType;
import com.vikk.appointmentscheduler.model.Priority;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AppointmentDAO.
 */
class AppointmentDAOTest {

    private AppointmentDAO appointmentDAO;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        databaseManager = DatabaseManager.getInstance();
        appointmentDAO = new AppointmentDAO();
        
        // Clear existing data
        try {
            appointmentDAO.deleteAll();
        } catch (Exception e) {
            // Ignore if no data exists
        }
    }

    @AfterEach
    void tearDown() {
        try {
            appointmentDAO.deleteAll();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    @DisplayName("Test save appointment")
    void testSaveAppointment() throws Exception {
        Appointment appointment = createTestAppointment("TEST001", "Test Appointment");
        
        appointmentDAO.save(appointment);
        
        Appointment saved = appointmentDAO.findById("TEST001");
        assertNotNull(saved, "Saved appointment should not be null");
        assertEquals("TEST001", saved.getId());
        assertEquals("Test Appointment", saved.getTitle());
        assertEquals(AppointmentType.CONSULTATION, saved.getType());
        assertEquals(Priority.MEDIUM, saved.getPriority());
        assertEquals(AppointmentStatus.PENDING, saved.getStatus());
    }

    @Test
    @DisplayName("Test find appointment by ID")
    void testFindById() throws Exception {
        Appointment appointment = createTestAppointment("TEST002", "Find Test");
        appointmentDAO.save(appointment);
        
        Appointment found = appointmentDAO.findById("TEST002");
        assertNotNull(found, "Found appointment should not be null");
        assertEquals("TEST002", found.getId());
        assertEquals("Find Test", found.getTitle());
    }

    @Test
    @DisplayName("Test find non-existent appointment")
    void testFindNonExistentAppointment() throws Exception {
        Appointment found = appointmentDAO.findById("NONEXISTENT");
        assertNull(found, "Non-existent appointment should be null");
    }

    @Test
    @DisplayName("Test find all appointments")
    void testFindAll() throws Exception {
        // Save multiple appointments
        appointmentDAO.save(createTestAppointment("TEST003", "Appointment 1"));
        appointmentDAO.save(createTestAppointment("TEST004", "Appointment 2"));
        appointmentDAO.save(createTestAppointment("TEST005", "Appointment 3"));
        
        List<Appointment> allAppointments = appointmentDAO.findAll();
        assertEquals(3, allAppointments.size(), "Should find 3 appointments");
        
        // Check that all appointments are present
        Set<String> ids = allAppointments.stream()
                .map(Appointment::getId)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(ids.contains("TEST003"));
        assertTrue(ids.contains("TEST004"));
        assertTrue(ids.contains("TEST005"));
    }

    @Test
    @DisplayName("Test update appointment")
    void testUpdateAppointment() throws Exception {
        Appointment appointment = createTestAppointment("TEST006", "Original Title");
        appointmentDAO.save(appointment);
        
        // Update the appointment
        appointment.setTitle("Updated Title");
        appointment.setPriority(Priority.HIGH);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentDAO.update(appointment);
        
        Appointment updated = appointmentDAO.findById("TEST006");
        assertNotNull(updated, "Updated appointment should not be null");
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(Priority.HIGH, updated.getPriority());
        assertEquals(AppointmentStatus.SCHEDULED, updated.getStatus());
    }

    @Test
    @DisplayName("Test delete appointment")
    void testDeleteAppointment() throws Exception {
        Appointment appointment = createTestAppointment("TEST007", "To Delete");
        appointmentDAO.save(appointment);
        
        // Verify it exists
        assertNotNull(appointmentDAO.findById("TEST007"));
        
        // Delete it
        appointmentDAO.delete("TEST007");
        
        // Verify it's gone
        assertNull(appointmentDAO.findById("TEST007"));
    }

    @Test
    @DisplayName("Test delete all appointments")
    void testDeleteAll() throws Exception {
        // Save some appointments
        appointmentDAO.save(createTestAppointment("TEST008", "Appointment 1"));
        appointmentDAO.save(createTestAppointment("TEST009", "Appointment 2"));
        
        // Verify they exist
        assertEquals(2, appointmentDAO.findAll().size());
        
        // Delete all
        appointmentDAO.deleteAll();
        
        // Verify they're all gone
        assertEquals(0, appointmentDAO.findAll().size());
    }

    @Test
    @DisplayName("Test appointment with capabilities")
    void testAppointmentWithCapabilities() throws Exception {
        Appointment appointment = createTestAppointment("TEST010", "With Capabilities");
        appointment.setRequiredCapabilities(Set.of("room", "equipment"));
        appointment.setPreferredCapabilities(Set.of("parking", "wifi"));
        
        appointmentDAO.save(appointment);
        
        Appointment saved = appointmentDAO.findById("TEST010");
        assertNotNull(saved, "Saved appointment should not be null");
        assertEquals(2, saved.getRequiredCapabilities().size());
        assertEquals(2, saved.getPreferredCapabilities().size());
        assertTrue(saved.getRequiredCapabilities().contains("room"));
        assertTrue(saved.getRequiredCapabilities().contains("equipment"));
        assertTrue(saved.getPreferredCapabilities().contains("parking"));
        assertTrue(saved.getPreferredCapabilities().contains("wifi"));
    }

    @Test
    @DisplayName("Test appointment with flexible scheduling")
    void testFlexibleAppointment() throws Exception {
        Appointment appointment = createTestAppointment("TEST011", "Flexible Appointment");
        appointment.setFlexible(true);
        appointment.setFlexibilityWindow(Duration.ofHours(2));
        appointment.setImportanceScore(0.8);
        
        appointmentDAO.save(appointment);
        
        Appointment saved = appointmentDAO.findById("TEST011");
        assertNotNull(saved, "Saved appointment should not be null");
        assertTrue(saved.isFlexible());
        assertEquals(Duration.ofHours(2), saved.getFlexibilityWindow());
        assertEquals(0.8, saved.getImportanceScore());
    }

    @Test
    @DisplayName("Test appointment with conflicts")
    void testAppointmentWithConflicts() throws Exception {
        Appointment appointment = createTestAppointment("TEST012", "Conflicting Appointment");
        appointment.setConflicts(Set.of("CONFLICT001", "CONFLICT002"));
        
        appointmentDAO.save(appointment);
        
        Appointment saved = appointmentDAO.findById("TEST012");
        assertNotNull(saved, "Saved appointment should not be null");
        assertEquals(2, saved.getConflicts().size());
        assertTrue(saved.getConflicts().contains("CONFLICT001"));
        assertTrue(saved.getConflicts().contains("CONFLICT002"));
    }

    @Test
    @DisplayName("Test different appointment types")
    void testDifferentAppointmentTypes() throws Exception {
        Appointment consultation = createTestAppointment("CONSULT001", "Consultation");
        consultation.setType(AppointmentType.CONSULTATION);
        
        Appointment emergency = createTestAppointment("EMERG001", "Emergency");
        emergency.setType(AppointmentType.EMERGENCY);
        emergency.setPriority(Priority.URGENT);
        
        Appointment surgery = createTestAppointment("SURG001", "Surgery");
        surgery.setType(AppointmentType.SURGERY);
        surgery.setDuration(Duration.ofHours(2));
        
        appointmentDAO.save(consultation);
        appointmentDAO.save(emergency);
        appointmentDAO.save(surgery);
        
        List<Appointment> all = appointmentDAO.findAll();
        assertEquals(3, all.size());
        
        Appointment savedConsultation = appointmentDAO.findById("CONSULT001");
        assertEquals(AppointmentType.CONSULTATION, savedConsultation.getType());
        
        Appointment savedEmergency = appointmentDAO.findById("EMERG001");
        assertEquals(AppointmentType.EMERGENCY, savedEmergency.getType());
        assertEquals(Priority.URGENT, savedEmergency.getPriority());
        
        Appointment savedSurgery = appointmentDAO.findById("SURG001");
        assertEquals(AppointmentType.SURGERY, savedSurgery.getType());
        assertEquals(Duration.ofHours(2), savedSurgery.getDuration());
    }

    private Appointment createTestAppointment(String id, String title) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setTitle(title);
        appointment.setType(AppointmentType.CONSULTATION);
        appointment.setPriority(Priority.MEDIUM);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setStartTime(LocalDateTime.now().plusHours(1));
        appointment.setDuration(Duration.ofMinutes(30));
        appointment.setDescription("Test appointment description");
        appointment.setImportanceScore(1.0);
        appointment.setFlexible(false);
        appointment.setFlexibilityWindow(Duration.ZERO);
        appointment.setRequiredCapabilities(Set.of());
        appointment.setPreferredCapabilities(Set.of());
        appointment.setConflicts(Set.of());
        return appointment;
    }
}
