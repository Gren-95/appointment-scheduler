package com.vikk.appointmentscheduler.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Set;
import java.util.HashSet;

/**
 * Tests for Appointment model class.
 */
@DisplayName("Appointment Tests")
class AppointmentTest {

    private Appointment appointment;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        appointment = new Appointment("APT001", "Test Appointment", baseTime, Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("Test appointment creation and basic properties")
    void testAppointmentCreation() {
        assertEquals("APT001", appointment.getId());
        assertEquals("Test Appointment", appointment.getTitle());
        assertEquals(baseTime, appointment.getStartTime());
        assertEquals(Duration.ofMinutes(30), appointment.getDuration());
        assertEquals(baseTime.plusMinutes(30), appointment.getEndTime());
    }

    @Test
    @DisplayName("Test appointment conflicts detection")
    void testConflictsWith() {
        // Non-overlapping appointments
        Appointment other1 = new Appointment("APT002", "Other 1", 
            baseTime.plusHours(1), Duration.ofMinutes(30));
        assertFalse(appointment.conflictsWith(other1));
        
        // Overlapping appointments
        Appointment other2 = new Appointment("APT003", "Other 2", 
            baseTime.plusMinutes(15), Duration.ofMinutes(30));
        assertTrue(appointment.conflictsWith(other2));
        
        // Same appointment
        assertFalse(appointment.conflictsWith(appointment));
        
        // Null appointment
        assertFalse(appointment.conflictsWith(null));
    }

    @Test
    @DisplayName("Test flexible scheduling")
    void testCanBeScheduledAt() {
        // Non-flexible appointment
        assertTrue(appointment.canBeScheduledAt(baseTime));
        assertFalse(appointment.canBeScheduledAt(baseTime.plusMinutes(30)));
        
        // Flexible appointment
        appointment.setFlexible(true);
        appointment.setFlexibilityWindow(Duration.ofMinutes(60));
        
        assertTrue(appointment.canBeScheduledAt(baseTime));
        assertTrue(appointment.canBeScheduledAt(baseTime.plusMinutes(30)));
        assertTrue(appointment.canBeScheduledAt(baseTime.minusMinutes(30)));
        assertTrue(appointment.canBeScheduledAt(baseTime.plusHours(1)));
        assertTrue(appointment.canBeScheduledAt(baseTime.minusHours(1)));
    }

    @Test
    @DisplayName("Test score calculation")
    void testCalculateScore() {
        appointment.setPriority(Priority.LOW);
        appointment.setImportanceScore(1.0);
        assertEquals(1.0, appointment.calculateScore());
        
        appointment.setPriority(Priority.MEDIUM);
        assertEquals(1.5, appointment.calculateScore());
        
        appointment.setPriority(Priority.HIGH);
        assertEquals(2.0, appointment.calculateScore());
        
        appointment.setPriority(Priority.URGENT);
        assertEquals(3.0, appointment.calculateScore());
        
        appointment.setImportanceScore(2.0);
        assertEquals(6.0, appointment.calculateScore());
    }

    @Test
    @DisplayName("Test time updates")
    void testTimeUpdates() {
        LocalDateTime newStartTime = baseTime.plusHours(1);
        appointment.setStartTime(newStartTime);
        
        assertEquals(newStartTime, appointment.getStartTime());
        assertEquals(newStartTime.plusMinutes(30), appointment.getEndTime());
        
        // Test setting end time
        LocalDateTime newEndTime = newStartTime.plusMinutes(45);
        appointment.setEndTime(newEndTime);
        
        assertEquals(newEndTime, appointment.getEndTime());
        assertEquals(Duration.ofMinutes(45), appointment.getDuration());
    }

    @Test
    @DisplayName("Test capabilities")
    void testCapabilities() {
        Set<String> required = Set.of("room", "doctor");
        Set<String> preferred = Set.of("window", "equipment");
        
        appointment.setRequiredCapabilities(required);
        appointment.setPreferredCapabilities(preferred);
        
        assertEquals(required, appointment.getRequiredCapabilities());
        assertEquals(preferred, appointment.getPreferredCapabilities());
    }

    @Test
    @DisplayName("Test conflicts set")
    void testConflicts() {
        Set<String> conflicts = Set.of("APT002", "APT003");
        appointment.setConflicts(conflicts);
        
        assertEquals(conflicts, appointment.getConflicts());
    }

    @Test
    @DisplayName("Test appointment type and priority")
    void testTypeAndPriority() {
        appointment.setType(AppointmentType.CONSULTATION);
        appointment.setPriority(Priority.HIGH);
        
        assertEquals(AppointmentType.CONSULTATION, appointment.getType());
        assertEquals(Priority.HIGH, appointment.getPriority());
    }

    @Test
    @DisplayName("Test equals and hashCode")
    void testEqualsAndHashCode() {
        Appointment sameId = new Appointment("APT001", "Different Title", 
            baseTime.plusHours(1), Duration.ofMinutes(60));
        
        assertEquals(appointment, sameId);
        assertEquals(appointment.hashCode(), sameId.hashCode());
        
        Appointment differentId = new Appointment("APT002", "Same Title", 
            baseTime, Duration.ofMinutes(30));
        
        assertNotEquals(appointment, differentId);
        assertNotEquals(appointment.hashCode(), differentId.hashCode());
        
        // Test with null
        assertNotEquals(appointment, null);
        
        // Test with different class
        assertNotEquals(appointment, "not an appointment");
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        String toString = appointment.toString();
        
        assertTrue(toString.contains("APT001"));
        assertTrue(toString.contains("Test Appointment"));
        assertTrue(toString.contains("10:00"));
    }

    @Test
    @DisplayName("Test default values")
    void testDefaultValues() {
        Appointment newAppointment = new Appointment();
        
        assertNull(newAppointment.getId());
        assertNull(newAppointment.getTitle());
        assertNull(newAppointment.getStartTime());
        assertNull(newAppointment.getEndTime());
        assertNull(newAppointment.getDuration());
        assertNull(newAppointment.getResourceId());
        assertNull(newAppointment.getClientId());
        assertNull(newAppointment.getType());
        assertNull(newAppointment.getPriority());
        assertNotNull(newAppointment.getRequiredCapabilities());
        assertNotNull(newAppointment.getPreferredCapabilities());
        assertNotNull(newAppointment.getConflicts());
        assertFalse(newAppointment.isFlexible());
        assertEquals(Duration.ZERO, newAppointment.getFlexibilityWindow());
        assertEquals(1.0, newAppointment.getImportanceScore());
    }

    @Test
    @DisplayName("Test edge cases")
    void testEdgeCases() {
        // Test with zero duration
        Appointment zeroDuration = new Appointment("APT004", "Zero Duration", 
            baseTime, Duration.ZERO);
        assertEquals(baseTime, zeroDuration.getEndTime());
        
        // Test with very long duration
        Duration longDuration = Duration.ofDays(1);
        Appointment longAppointment = new Appointment("APT005", "Long Appointment", 
            baseTime, longDuration);
        assertEquals(baseTime.plus(longDuration), longAppointment.getEndTime());
    }
}

