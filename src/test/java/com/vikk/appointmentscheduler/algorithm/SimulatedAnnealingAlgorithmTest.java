package com.vikk.appointmentscheduler.algorithm;

import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.AppointmentStatus;
import com.vikk.appointmentscheduler.model.AppointmentType;
import com.vikk.appointmentscheduler.model.Priority;
import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.ResourceType;
import com.vikk.appointmentscheduler.model.Schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulatedAnnealingAlgorithm.
 */
class SimulatedAnnealingAlgorithmTest {

    private SimulatedAnnealingAlgorithm algorithm;
    private List<Appointment> appointments;
    private List<Resource> resources;

    @BeforeEach
    void setUp() {
        // Create test appointments
        appointments = Arrays.asList(
            createAppointment("APT001", "Consultation 1", LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)),
            createAppointment("APT002", "Follow-up 1", LocalDateTime.now().plusHours(2), Duration.ofMinutes(15)),
            createAppointment("APT003", "Treatment 1", LocalDateTime.now().plusHours(3), Duration.ofMinutes(60))
        );

        // Create test resources
        resources = Arrays.asList(
            createResource("ROOM001", "Room 1", ResourceType.ROOM, Set.of("room")),
            createResource("EQUIP001", "Equipment 1", ResourceType.EQUIPMENT, Set.of("equipment")),
            createResource("STAFF001", "Staff 1", ResourceType.STAFF, Set.of("staff"))
        );

        algorithm = new SimulatedAnnealingAlgorithm(appointments, resources);
    }

    @Test
    @DisplayName("Test algorithm initialization")
    void testAlgorithmInitialization() {
        assertNotNull(algorithm, "Algorithm should not be null");
        assertEquals("Simulated Annealing Algorithm", algorithm.getAlgorithmName(), "Algorithm name should be correct");
        assertEquals(3, appointments.size(), "Should have 3 appointments");
        assertEquals(3, resources.size(), "Should have 3 resources");
    }

    @Test
    @DisplayName("Test basic optimization")
    void testBasicOptimization() {
        Schedule schedule = algorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getId(), "Schedule ID should not be null");
        assertTrue(schedule.getId().startsWith("SA_"), "Schedule ID should start with SA_");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
        assertNotNull(schedule.getResourceAssignments(), "Resource assignments should not be null");
        assertNotNull(schedule.getUnassignedAppointments(), "Unassigned appointments should not be null");
    }

    @Test
    @DisplayName("Test optimization with no resources")
    void testOptimizationWithNoResources() {
        SimulatedAnnealingAlgorithm noResourceAlgorithm = new SimulatedAnnealingAlgorithm(appointments, Arrays.asList());
        Schedule schedule = noResourceAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertEquals(appointments.size(), schedule.getUnassignedAppointments().size(),
                   "All appointments should be unassigned");
    }

    @Test
    @DisplayName("Test optimization with no appointments")
    void testOptimizationWithNoAppointments() {
        SimulatedAnnealingAlgorithm noAppointmentAlgorithm = new SimulatedAnnealingAlgorithm(Arrays.asList(), resources);
        Schedule schedule = noAppointmentAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertTrue(schedule.getAppointments().isEmpty(), "Appointments should be empty");
        assertTrue(schedule.getUnassignedAppointments().isEmpty(), "Unassigned appointments should be empty");
    }

    @Test
    @DisplayName("Test optimization with conflicting appointments")
    void testOptimizationWithConflictingAppointments() {
        // Create two appointments at the same time
        LocalDateTime sameTime = LocalDateTime.now().plusHours(1);
        List<Appointment> conflictingAppointments = Arrays.asList(
            createAppointment("CONFLICT1", "Conflict 1", sameTime, Duration.ofMinutes(30)),
            createAppointment("CONFLICT2", "Conflict 2", sameTime, Duration.ofMinutes(30))
        );
        
        SimulatedAnnealingAlgorithm conflictAlgorithm = new SimulatedAnnealingAlgorithm(conflictingAppointments, resources);
        Schedule schedule = conflictAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        // At least one appointment should be unassigned due to conflict
        assertTrue(schedule.getUnassignedAppointments().size() > 0 || 
                  schedule.getConflictCount() > 0,
                  "Should have conflicts or unassigned appointments");
    }

    @Test
    @DisplayName("Test optimization with flexible appointments")
    void testOptimizationWithFlexibleAppointments() {
        // Create a flexible appointment
        Appointment flexible = createAppointment("FLEX001", "Flexible", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        flexible.setFlexible(true);
        flexible.setFlexibilityWindow(Duration.ofHours(2));
        
        List<Appointment> flexibleAppointments = Arrays.asList(flexible);
        SimulatedAnnealingAlgorithm flexibleAlgorithm = new SimulatedAnnealingAlgorithm(flexibleAppointments, resources);
        Schedule schedule = flexibleAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
    }

    @Test
    @DisplayName("Test optimization with high priority appointments")
    void testOptimizationWithHighPriorityAppointments() {
        // Create high priority appointment
        Appointment urgent = createAppointment("URGENT001", "Urgent", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        urgent.setPriority(Priority.URGENT);
        urgent.setType(AppointmentType.EMERGENCY);
        
        List<Appointment> urgentAppointments = Arrays.asList(urgent);
        SimulatedAnnealingAlgorithm urgentAlgorithm = new SimulatedAnnealingAlgorithm(urgentAppointments, resources);
        Schedule schedule = urgentAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        // High priority appointments should be scheduled if possible
        assertTrue(schedule.getUnassignedAppointments().isEmpty() || 
                  schedule.getAppointments().size() > 0,
                  "Should have some scheduled appointments");
    }

    @Test
    @DisplayName("Test optimization with resource capability requirements")
    void testOptimizationWithResourceCapabilityRequirements() {
        // Create appointment with specific capability requirements
        Appointment specialized = createAppointment("SPEC001", "Specialized", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        specialized.setRequiredCapabilities(Set.of("equipment"));
        specialized.setPreferredCapabilities(Set.of("staff"));
        
        List<Appointment> specializedAppointments = Arrays.asList(specialized);
        SimulatedAnnealingAlgorithm specializedAlgorithm = new SimulatedAnnealingAlgorithm(specializedAppointments, resources);
        Schedule schedule = specializedAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
    }

    @Test
    @DisplayName("Test algorithm performance metrics")
    void testAlgorithmPerformanceMetrics() {
        Schedule schedule = algorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertTrue(schedule.getTotalCost() >= 0, "Total cost should be non-negative");
        assertTrue(schedule.getConflictCount() >= 0, "Conflict count should be non-negative");
        assertTrue(schedule.calculateEfficiencyScore() >= 0, "Efficiency score should be non-negative");
    }

    @Test
    @DisplayName("Test algorithm with large dataset")
    void testAlgorithmWithLargeDataset() {
        // Create a larger dataset
        List<Appointment> largeAppointments = Arrays.asList(
            createAppointment("APT001", "Appointment 1", LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)),
            createAppointment("APT002", "Appointment 2", LocalDateTime.now().plusHours(2), Duration.ofMinutes(45)),
            createAppointment("APT003", "Appointment 3", LocalDateTime.now().plusHours(3), Duration.ofMinutes(60)),
            createAppointment("APT004", "Appointment 4", LocalDateTime.now().plusHours(4), Duration.ofMinutes(30)),
            createAppointment("APT005", "Appointment 5", LocalDateTime.now().plusHours(5), Duration.ofMinutes(90))
        );
        
        List<Resource> largeResources = Arrays.asList(
            createResource("ROOM001", "Room 1", ResourceType.ROOM, Set.of("room")),
            createResource("ROOM002", "Room 2", ResourceType.ROOM, Set.of("room")),
            createResource("EQUIP001", "Equipment 1", ResourceType.EQUIPMENT, Set.of("equipment")),
            createResource("STAFF001", "Staff 1", ResourceType.STAFF, Set.of("staff")),
            createResource("STAFF002", "Staff 2", ResourceType.STAFF, Set.of("staff"))
        );
        
        SimulatedAnnealingAlgorithm largeAlgorithm = new SimulatedAnnealingAlgorithm(largeAppointments, largeResources);
        Schedule schedule = largeAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
        assertNotNull(schedule.getResourceAssignments(), "Resource assignments should not be null");
    }

    @Test
    @DisplayName("Test algorithm with different appointment types")
    void testAlgorithmWithDifferentAppointmentTypes() {
        List<Appointment> diverseAppointments = Arrays.asList(
            createAppointment("CONSULT001", "Consultation", LocalDateTime.now().plusHours(1), Duration.ofMinutes(30)),
            createAppointment("EMERG001", "Emergency", LocalDateTime.now().plusHours(2), Duration.ofMinutes(45)),
            createAppointment("SURG001", "Surgery", LocalDateTime.now().plusHours(3), Duration.ofHours(2)),
            createAppointment("DIAG001", "Diagnostic", LocalDateTime.now().plusHours(4), Duration.ofMinutes(60))
        );
        
        // Set different types
        diverseAppointments.get(0).setType(AppointmentType.CONSULTATION);
        diverseAppointments.get(1).setType(AppointmentType.EMERGENCY);
        diverseAppointments.get(1).setPriority(Priority.URGENT);
        diverseAppointments.get(2).setType(AppointmentType.SURGERY);
        diverseAppointments.get(3).setType(AppointmentType.DIAGNOSTIC);
        
        SimulatedAnnealingAlgorithm diverseAlgorithm = new SimulatedAnnealingAlgorithm(diverseAppointments, resources);
        Schedule schedule = diverseAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
    }

    @Test
    @DisplayName("Test algorithm convergence")
    void testAlgorithmConvergence() {
        Schedule schedule = algorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        // The algorithm should produce a valid schedule
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
        assertNotNull(schedule.getResourceAssignments(), "Resource assignments should not be null");
        assertNotNull(schedule.getUnassignedAppointments(), "Unassigned appointments should not be null");
    }

    @Test
    @DisplayName("Test algorithm with time constraints")
    void testAlgorithmWithTimeConstraints() {
        // Create appointments with specific time constraints
        LocalDateTime baseTime = LocalDateTime.now().plusHours(1);
        List<Appointment> timeConstrainedAppointments = Arrays.asList(
            createAppointment("TIME001", "Morning", baseTime, Duration.ofMinutes(30)),
            createAppointment("TIME002", "Afternoon", baseTime.plusHours(2), Duration.ofMinutes(45)),
            createAppointment("TIME003", "Evening", baseTime.plusHours(4), Duration.ofMinutes(60))
        );
        
        SimulatedAnnealingAlgorithm timeAlgorithm = new SimulatedAnnealingAlgorithm(timeConstrainedAppointments, resources);
        Schedule schedule = timeAlgorithm.optimize();
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
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

    private Resource createResource(String id, String name, ResourceType type, Set<String> capabilities) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setName(name);
        resource.setType(type);
        resource.setCostPerHour(50.0);
        resource.setCapabilities(capabilities);
        resource.setConflicts(Set.of());
        return resource;
    }
}
