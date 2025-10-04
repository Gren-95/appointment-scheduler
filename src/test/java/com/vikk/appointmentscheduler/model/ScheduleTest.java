package com.vikk.appointmentscheduler.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Schedule.
 */
class ScheduleTest {

    private Schedule schedule;
    private Appointment appointment1;
    private Appointment appointment2;
    private Resource resource1;
    private Resource resource2;

    @BeforeEach
    void setUp() {
        schedule = new Schedule("TEST_SCHEDULE", 
            LocalDateTime.now(), 
            LocalDateTime.now().plusHours(8));
        
        // Create test appointments
        appointment1 = createAppointment("APT001", "Appointment 1", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        appointment2 = createAppointment("APT002", "Appointment 2", 
            LocalDateTime.now().plusHours(2), Duration.ofMinutes(45));
        
        // Create test resources
        resource1 = createResource("RES001", "Resource 1", ResourceType.ROOM);
        resource2 = createResource("RES002", "Resource 2", ResourceType.EQUIPMENT);
    }

    @Test
    @DisplayName("Test schedule initialization")
    void testScheduleInitialization() {
        assertNotNull(schedule, "Schedule should not be null");
        assertEquals("TEST_SCHEDULE", schedule.getId());
        assertNotNull(schedule.getStartDate());
        assertNotNull(schedule.getEndDate());
        assertNotNull(schedule.getAppointments());
        assertNotNull(schedule.getResourceAssignments());
        assertNotNull(schedule.getResourceSchedules());
        assertNotNull(schedule.getUnassignedAppointments());
        assertEquals(0.0, schedule.getTotalCost());
        assertEquals(0.0, schedule.getTotalScore());
        assertEquals(0, schedule.getConflictCount());
    }

    @Test
    @DisplayName("Test add appointment")
    void testAddAppointment() {
        schedule.addAppointment(appointment1, resource1.getId());
        
        assertEquals(1, schedule.getAppointments().size());
        assertTrue(schedule.getAppointments().contains(appointment1));
        assertEquals(resource1.getId(), schedule.getResourceForAppointment(appointment1.getId()));
        assertTrue(schedule.getResourceSchedules().containsKey(resource1.getId()));
        assertEquals(1, schedule.getResourceSchedules().get(resource1.getId()).size());
    }

    @Test
    @DisplayName("Test add multiple appointments")
    void testAddMultipleAppointments() {
        schedule.addAppointment(appointment1, resource1.getId());
        schedule.addAppointment(appointment2, resource2.getId());
        
        assertEquals(2, schedule.getAppointments().size());
        assertTrue(schedule.getAppointments().contains(appointment1));
        assertTrue(schedule.getAppointments().contains(appointment2));
        assertEquals(resource1.getId(), schedule.getResourceForAppointment(appointment1.getId()));
        assertEquals(resource2.getId(), schedule.getResourceForAppointment(appointment2.getId()));
    }

    @Test
    @DisplayName("Test remove appointment")
    void testRemoveAppointment() {
        schedule.addAppointment(appointment1, resource1.getId());
        schedule.addAppointment(appointment2, resource2.getId());
        
        schedule.removeAppointment(appointment1.getId());
        
        assertEquals(1, schedule.getAppointments().size());
        assertFalse(schedule.getAppointments().contains(appointment1));
        assertTrue(schedule.getAppointments().contains(appointment2));
        assertNull(schedule.getResourceForAppointment(appointment1.getId()));
        assertEquals(resource2.getId(), schedule.getResourceForAppointment(appointment2.getId()));
    }

    @Test
    @DisplayName("Test get appointments for resource")
    void testGetAppointmentsForResource() {
        schedule.addAppointment(appointment1, resource1.getId());
        schedule.addAppointment(appointment2, resource1.getId());
        
        List<Appointment> resourceAppointments = schedule.getAppointmentsForResource(resource1.getId());
        assertEquals(2, resourceAppointments.size());
        assertTrue(resourceAppointments.contains(appointment1));
        assertTrue(resourceAppointments.contains(appointment2));
    }

    @Test
    @DisplayName("Test get appointments for non-existent resource")
    void testGetAppointmentsForNonExistentResource() {
        List<Appointment> resourceAppointments = schedule.getAppointmentsForResource("NONEXISTENT");
        assertTrue(resourceAppointments.isEmpty());
    }

    @Test
    @DisplayName("Test has conflicts")
    void testHasConflicts() {
        assertFalse(schedule.hasConflicts(), "New schedule should have no conflicts");
        
        schedule.setConflictCount(1);
        assertTrue(schedule.hasConflicts(), "Schedule with conflicts should return true");
        
        schedule.setConflictCount(0);
        assertFalse(schedule.hasConflicts(), "Schedule with no conflicts should return false");
    }

    @Test
    @DisplayName("Test calculate utilization rate")
    void testCalculateUtilizationRate() {
        // Empty schedule should have 0 utilization
        assertEquals(0.0, schedule.calculateUtilizationRate());
        
        // Add appointments
        schedule.addAppointment(appointment1, resource1.getId());
        schedule.addAppointment(appointment2, resource2.getId());
        
        // Utilization should be calculated based on scheduled time vs total time
        double utilization = schedule.calculateUtilizationRate();
        assertTrue(utilization >= 0.0 && utilization <= 1.0, "Utilization should be between 0 and 1");
    }

    @Test
    @DisplayName("Test calculate efficiency score")
    void testCalculateEfficiencyScore() {
        // Empty schedule should have 0 efficiency
        assertEquals(0.0, schedule.calculateEfficiencyScore());
        
        // Add appointments
        schedule.addAppointment(appointment1, resource1.getId());
        schedule.addAppointment(appointment2, resource2.getId());
        
        // Efficiency should be calculated
        double efficiency = schedule.calculateEfficiencyScore();
        assertTrue(efficiency >= 0.0, "Efficiency should be non-negative");
    }

    @Test
    @DisplayName("Test setters and getters")
    void testSettersAndGetters() {
        // Test ID
        schedule.setId("NEW_ID");
        assertEquals("NEW_ID", schedule.getId());
        
        // Test start date
        LocalDateTime newStart = LocalDateTime.now().plusDays(1);
        schedule.setStartDate(newStart);
        assertEquals(newStart, schedule.getStartDate());
        
        // Test end date
        LocalDateTime newEnd = LocalDateTime.now().plusDays(2);
        schedule.setEndDate(newEnd);
        assertEquals(newEnd, schedule.getEndDate());
        
        // Test total cost
        schedule.setTotalCost(100.0);
        assertEquals(100.0, schedule.getTotalCost());
        
        // Test total score
        schedule.setTotalScore(85.5);
        assertEquals(85.5, schedule.getTotalScore());
        
        // Test conflict count
        schedule.setConflictCount(3);
        assertEquals(3, schedule.getConflictCount());
        
        // Test unassigned appointments
        Set<String> unassigned = Set.of("APT003", "APT004");
        schedule.setUnassignedAppointments(unassigned);
        assertEquals(unassigned, schedule.getUnassignedAppointments());
    }

    @Test
    @DisplayName("Test resource assignments")
    void testResourceAssignments() {
        Map<String, String> assignments = new HashMap<>();
        assignments.put(appointment1.getId(), resource1.getId());
        assignments.put(appointment2.getId(), resource2.getId());
        
        schedule.setResourceAssignments(assignments);
        assertEquals(assignments, schedule.getResourceAssignments());
        assertEquals(resource1.getId(), schedule.getResourceForAppointment(appointment1.getId()));
        assertEquals(resource2.getId(), schedule.getResourceForAppointment(appointment2.getId()));
    }

    @Test
    @DisplayName("Test resource schedules")
    void testResourceSchedules() {
        Map<String, List<Appointment>> resourceSchedules = new HashMap<>();
        resourceSchedules.put(resource1.getId(), Arrays.asList(appointment1));
        resourceSchedules.put(resource2.getId(), Arrays.asList(appointment2));
        
        schedule.setResourceSchedules(resourceSchedules);
        assertEquals(resourceSchedules, schedule.getResourceSchedules());
    }

    @Test
    @DisplayName("Test metrics")
    void testMetrics() {
        ScheduleMetrics metrics = new ScheduleMetrics();
        metrics.setTotalAppointments(5);
        metrics.setAssignedAppointments(4);
        metrics.setUnassignedAppointments(1);
        metrics.setTotalCost(250.0);
        metrics.setAverageCostPerAppointment(62.5);
        metrics.setConflictCount(2);
        metrics.setUtilizationRate(0.8);
        metrics.setEfficiencyScore(85.0);
        
        schedule.setMetrics(metrics);
        assertEquals(metrics, schedule.getMetrics());
    }

    @Test
    @DisplayName("Test equals and hashCode")
    void testEqualsAndHashCode() {
        Schedule schedule1 = new Schedule("TEST1", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        Schedule schedule2 = new Schedule("TEST1", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        Schedule schedule3 = new Schedule("TEST2", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        
        assertEquals(schedule1, schedule2, "Schedules with same ID should be equal");
        assertNotEquals(schedule1, schedule3, "Schedules with different IDs should not be equal");
        assertEquals(schedule1.hashCode(), schedule2.hashCode(), "Equal schedules should have same hashCode");
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        String scheduleString = schedule.toString();
        assertNotNull(scheduleString, "toString should not be null");
        assertTrue(scheduleString.contains("TEST_SCHEDULE"), "toString should contain schedule ID");
    }

    @Test
    @DisplayName("Test with null values")
    void testWithNullValues() {
        Schedule nullSchedule = new Schedule();
        assertNull(nullSchedule.getId());
        assertNull(nullSchedule.getStartDate());
        assertNull(nullSchedule.getEndDate());
        assertNotNull(nullSchedule.getAppointments());
        assertNotNull(nullSchedule.getResourceAssignments());
        assertNotNull(nullSchedule.getResourceSchedules());
        assertNotNull(nullSchedule.getUnassignedAppointments());
    }

    @Test
    @DisplayName("Test appointment conflicts")
    void testAppointmentConflicts() {
        // Create conflicting appointments
        LocalDateTime sameTime = LocalDateTime.now().plusHours(1);
        Appointment conflict1 = createAppointment("CONFLICT1", "Conflict 1", sameTime, Duration.ofMinutes(30));
        Appointment conflict2 = createAppointment("CONFLICT2", "Conflict 2", sameTime, Duration.ofMinutes(30));
        
        schedule.addAppointment(conflict1, resource1.getId());
        schedule.addAppointment(conflict2, resource1.getId());
        
        // Should have conflicts
        assertTrue(schedule.getConflictCount() > 0 || schedule.hasConflicts());
    }

    private Appointment createAppointment(String id, String title, LocalDateTime startTime, Duration duration) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setTitle(title);
        appointment.setType(AppointmentType.CONSULTATION);
        appointment.setPriority(Priority.MEDIUM);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setStartTime(startTime);
        appointment.setDuration(duration);
        appointment.setDescription("Test appointment");
        appointment.setImportanceScore(1.0);
        appointment.setFlexible(false);
        appointment.setFlexibilityWindow(Duration.ZERO);
        appointment.setRequiredCapabilities(Set.of());
        appointment.setPreferredCapabilities(Set.of());
        appointment.setConflicts(Set.of());
        return appointment;
    }

    private Resource createResource(String id, String name, ResourceType type) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setName(name);
        resource.setType(type);
        resource.setCostPerHour(50.0);
        resource.setCapabilities(Set.of("CONSULTATION"));
        resource.setConflicts(Set.of());
        return resource;
    }
}
