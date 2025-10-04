package com.vikk.appointmentscheduler.algorithm;

import com.vikk.appointmentscheduler.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

/**
 * Tests for ConstraintSatisfactionAlgorithm.
 */
@DisplayName("Constraint Satisfaction Algorithm Tests")
class ConstraintSatisfactionAlgorithmTest {

    private List<Appointment> appointments;
    private List<Resource> resources;
    private ConstraintSatisfactionAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        appointments = createTestAppointments();
        resources = createTestResources();
        algorithm = new ConstraintSatisfactionAlgorithm(appointments, resources);
    }

    @Test
    @DisplayName("Test algorithm optimization with valid data")
    void testOptimizationWithValidData() {
        Schedule schedule = algorithm.optimize();
        
        assertNotNull(schedule);
        assertNotNull(schedule.getId());
        assertTrue(schedule.getId().startsWith("CSP_"));
        assertFalse(schedule.getAppointments().isEmpty());
    }

    @Test
    @DisplayName("Test algorithm with no appointments")
    void testAlgorithmWithNoAppointments() {
        ConstraintSatisfactionAlgorithm emptyAlgorithm = 
            new ConstraintSatisfactionAlgorithm(new ArrayList<>(), resources);
        
        Schedule schedule = emptyAlgorithm.optimize();
        
        assertNotNull(schedule);
        assertTrue(schedule.getAppointments().isEmpty());
    }

    @Test
    @DisplayName("Test algorithm with no resources")
    void testAlgorithmWithNoResources() {
        ConstraintSatisfactionAlgorithm noResourceAlgorithm = 
            new ConstraintSatisfactionAlgorithm(appointments, new ArrayList<>());
        
        Schedule schedule = noResourceAlgorithm.optimize();
        
        assertNotNull(schedule);
        // All appointments should be unassigned
        assertEquals(appointments.size(), schedule.getUnassignedAppointments().size());
    }

    @Test
    @DisplayName("Test algorithm execution time")
    void testExecutionTime() {
        Schedule schedule = algorithm.optimize();
        
        long executionTime = algorithm.getExecutionTime();
        assertTrue(executionTime >= 0);
        assertTrue(executionTime < 10000); // Should complete within 10 seconds
    }

    @Test
    @DisplayName("Test algorithm backtrack count")
    void testBacktrackCount() {
        Schedule schedule = algorithm.optimize();
        
        int backtrackCount = algorithm.getBacktrackCount();
        assertTrue(backtrackCount >= 0);
    }

    @Test
    @DisplayName("Test algorithm name")
    void testAlgorithmName() {
        assertEquals("Constraint Satisfaction Problem (CSP)", algorithm.getAlgorithmName());
    }

    @Test
    @DisplayName("Test schedule metrics calculation")
    void testScheduleMetricsCalculation() {
        Schedule schedule = algorithm.optimize();
        
        assertNotNull(schedule.getMetrics());
        assertTrue(schedule.getTotalCost() >= 0);
        assertTrue(schedule.getTotalScore() >= 0);
        assertTrue(schedule.getConflictCount() >= 0);
    }

    @Test
    @DisplayName("Test with conflicting appointments")
    void testWithConflictingAppointments() {
        // Create two appointments at the same time
        LocalDateTime sameTime = LocalDateTime.now().plusHours(1);
        Appointment apt1 = new Appointment("CONFLICT1", "Conflict 1", 
            sameTime, Duration.ofMinutes(30));
        apt1.setPriority(Priority.MEDIUM);
        apt1.setRequiredCapabilities(Set.of("room"));
        
        Appointment apt2 = new Appointment("CONFLICT2", "Conflict 2", 
            sameTime, Duration.ofMinutes(30));
        apt2.setPriority(Priority.MEDIUM);
        apt2.setRequiredCapabilities(Set.of("room"));
        
        List<Appointment> conflictingAppointments = Arrays.asList(apt1, apt2);
        List<Resource> singleResource = Arrays.asList(createRoomResource("ROOM001"));
        
        ConstraintSatisfactionAlgorithm conflictAlgorithm = 
            new ConstraintSatisfactionAlgorithm(conflictingAppointments, singleResource);
        
        Schedule schedule = conflictAlgorithm.optimize();
        
        assertNotNull(schedule);
        // At least one appointment should be unassigned due to conflict
        assertTrue(schedule.getUnassignedAppointments().size() > 0 || 
                  schedule.getConflictCount() > 0);
    }

    @Test
    @DisplayName("Test with high priority appointments")
    void testWithHighPriorityAppointments() {
        // Create high priority appointment
        Appointment urgent = new Appointment("URGENT", "Urgent Appointment", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        urgent.setPriority(Priority.URGENT);
        urgent.setRequiredCapabilities(Set.of("room"));
        
        List<Appointment> priorityAppointments = Arrays.asList(urgent);
        List<Resource> roomResources = Arrays.asList(createRoomResource("ROOM001"));
        
        ConstraintSatisfactionAlgorithm priorityAlgorithm = 
            new ConstraintSatisfactionAlgorithm(priorityAppointments, roomResources);
        
        Schedule schedule = priorityAlgorithm.optimize();
        
        assertNotNull(schedule);
        // High priority appointment should be assigned
        assertTrue(schedule.getUnassignedAppointments().isEmpty() || 
                  !schedule.getUnassignedAppointments().contains("URGENT"));
    }

    @Test
    @DisplayName("Test with flexible appointments")
    void testWithFlexibleAppointments() {
        // Create flexible appointment
        Appointment flexible = new Appointment("FLEXIBLE", "Flexible Appointment", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        flexible.setPriority(Priority.MEDIUM);
        flexible.setFlexible(true);
        flexible.setFlexibilityWindow(Duration.ofHours(2));
        flexible.setRequiredCapabilities(Set.of("room"));
        
        List<Appointment> flexibleAppointments = Arrays.asList(flexible);
        List<Resource> roomResources = Arrays.asList(createRoomResource("ROOM001"));
        
        ConstraintSatisfactionAlgorithm flexibleAlgorithm = 
            new ConstraintSatisfactionAlgorithm(flexibleAppointments, roomResources);
        
        Schedule schedule = flexibleAlgorithm.optimize();
        
        assertNotNull(schedule);
        // Flexible appointment should be assigned
        assertTrue(schedule.getUnassignedAppointments().isEmpty() || 
                  !schedule.getUnassignedAppointments().contains("FLEXIBLE"));
    }

    @Test
    @DisplayName("Test with resource capability requirements")
    void testWithResourceCapabilityRequirements() {
        // Create appointment requiring specific capabilities
        Appointment specialized = new Appointment("SPECIALIZED", "Specialized Appointment", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        specialized.setPriority(Priority.MEDIUM);
        specialized.setRequiredCapabilities(Set.of("special_equipment", "specialist"));
        
        // Create resource without required capabilities
        Resource basicRoom = createRoomResource("BASIC_ROOM");
        basicRoom.setCapabilities(Set.of("room"));
        
        // Create resource with required capabilities
        Resource specializedRoom = createRoomResource("SPECIALIZED_ROOM");
        specializedRoom.setCapabilities(Set.of("room", "special_equipment", "specialist"));
        
        List<Appointment> specializedAppointments = Arrays.asList(specialized);
        List<Resource> mixedResources = Arrays.asList(basicRoom, specializedRoom);
        
        ConstraintSatisfactionAlgorithm capabilityAlgorithm = 
            new ConstraintSatisfactionAlgorithm(specializedAppointments, mixedResources);
        
        Schedule schedule = capabilityAlgorithm.optimize();
        
        assertNotNull(schedule);
        // Should assign to resource with required capabilities
        String assignedResource = schedule.getResourceForAppointment("SPECIALIZED");
        if (assignedResource != null) {
            assertEquals("SPECIALIZED_ROOM", assignedResource);
        }
    }

    // Helper methods
    private List<Appointment> createTestAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().plusHours(1);
        
        Appointment apt1 = new Appointment("APT001", "Test Appointment 1", 
            baseTime, Duration.ofMinutes(30));
        apt1.setPriority(Priority.MEDIUM);
        apt1.setRequiredCapabilities(Set.of("room"));
        appointments.add(apt1);
        
        Appointment apt2 = new Appointment("APT002", "Test Appointment 2", 
            baseTime.plusHours(1), Duration.ofMinutes(45));
        apt2.setPriority(Priority.HIGH);
        apt2.setRequiredCapabilities(Set.of("room"));
        appointments.add(apt2);
        
        return appointments;
    }

    private List<Resource> createTestResources() {
        List<Resource> resources = new ArrayList<>();
        
        Resource room1 = createRoomResource("ROOM001");
        Resource room2 = createRoomResource("ROOM002");
        
        resources.add(room1);
        resources.add(room2);
        
        return resources;
    }

    private Resource createRoomResource(String id) {
        Resource resource = new Resource(id, "Test Room " + id, ResourceType.ROOM);
        resource.setCapabilities(Set.of("room"));
        resource.setAvailableFrom(LocalDateTime.now());
        resource.setAvailableTo(LocalDateTime.now().plusDays(1));
        resource.setCostPerHour(50.0);
        resource.setCapacity(1);
        return resource;
    }
}

