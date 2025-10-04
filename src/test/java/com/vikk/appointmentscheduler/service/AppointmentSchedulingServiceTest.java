package com.vikk.appointmentscheduler.service;

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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AppointmentSchedulingService.
 */
class AppointmentSchedulingServiceTest {

    private AppointmentSchedulingService service;
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

        service = new AppointmentSchedulingService(appointments, resources);
    }

    @Test
    @DisplayName("Test service initialization")
    void testServiceInitialization() {
        assertNotNull(service, "Service should not be null");
        assertEquals(3, service.getAppointments().size(), "Should have 3 appointments");
        assertEquals(3, service.getResources().size(), "Should have 3 resources");
    }

    @Test
    @DisplayName("Test get available algorithms")
    void testGetAvailableAlgorithms() {
        Set<String> algorithms = service.getAvailableAlgorithms();
        assertNotNull(algorithms, "Algorithms set should not be null");
        assertTrue(algorithms.contains("CSP"), "Should contain CSP algorithm");
        assertTrue(algorithms.contains("GA"), "Should contain GA algorithm");
        assertTrue(algorithms.contains("SA"), "Should contain SA algorithm");
        assertEquals(3, algorithms.size(), "Should have 3 algorithms");
    }

    @Test
    @DisplayName("Test get registered algorithms")
    void testGetRegisteredAlgorithms() {
        List<com.vikk.appointmentscheduler.algorithm.SchedulingAlgorithm> algorithms = service.getRegisteredAlgorithms();
        assertNotNull(algorithms, "Algorithms list should not be null");
        assertEquals(3, algorithms.size(), "Should have 3 algorithms");
        
        // Check algorithm names
        Set<String> algorithmNames = algorithms.stream()
                .map(com.vikk.appointmentscheduler.algorithm.SchedulingAlgorithm::getAlgorithmName)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(algorithmNames.contains("Constraint Satisfaction Problem (CSP)"));
        assertTrue(algorithmNames.contains("Genetic Algorithm"));
        assertTrue(algorithmNames.contains("Simulated Annealing Algorithm"));
    }

    @Test
    @DisplayName("Test optimize with CSP algorithm")
    void testOptimizeWithCSP() {
        Schedule schedule = service.optimizeSchedule("CSP");
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getId(), "Schedule ID should not be null");
        assertTrue(schedule.getId().startsWith("CSP_"), "Schedule ID should start with CSP_");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
        assertNotNull(schedule.getResourceAssignments(), "Resource assignments should not be null");
        assertNotNull(schedule.getUnassignedAppointments(), "Unassigned appointments should not be null");
    }

    @Test
    @DisplayName("Test optimize with GA algorithm")
    void testOptimizeWithGA() {
        Schedule schedule = service.optimizeSchedule("GA");
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getId(), "Schedule ID should not be null");
        assertTrue(schedule.getId().startsWith("GA_"), "Schedule ID should start with GA_");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
        assertNotNull(schedule.getResourceAssignments(), "Resource assignments should not be null");
        assertNotNull(schedule.getUnassignedAppointments(), "Unassigned appointments should not be null");
    }

    @Test
    @DisplayName("Test optimize with SA algorithm")
    void testOptimizeWithSA() {
        Schedule schedule = service.optimizeSchedule("SA");
        
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getId(), "Schedule ID should not be null");
        assertTrue(schedule.getId().startsWith("SA_"), "Schedule ID should start with SA_");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
        assertNotNull(schedule.getResourceAssignments(), "Resource assignments should not be null");
        assertNotNull(schedule.getUnassignedAppointments(), "Unassigned appointments should not be null");
    }

    @Test
    @DisplayName("Test optimize with invalid algorithm")
    void testOptimizeWithInvalidAlgorithm() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.optimizeSchedule("INVALID");
        }, "Should throw IllegalArgumentException for invalid algorithm");
    }

    @Test
    @DisplayName("Test optimize with all algorithms")
    void testOptimizeWithAllAlgorithms() {
        Map<String, AppointmentSchedulingService.AlgorithmComparisonResult> results = service.compareAlgorithms();
        
        assertNotNull(results, "Results should not be null");
        assertEquals(3, results.size(), "Should have 3 results");
        assertTrue(results.containsKey("CSP"), "Should contain CSP result");
        assertTrue(results.containsKey("GA"), "Should contain GA result");
        assertTrue(results.containsKey("SA"), "Should contain SA result");
        
        // Verify each result
        for (Map.Entry<String, AppointmentSchedulingService.AlgorithmComparisonResult> entry : results.entrySet()) {
            String algorithm = entry.getKey();
            AppointmentSchedulingService.AlgorithmComparisonResult result = entry.getValue();
            
            assertNotNull(result, "Result for " + algorithm + " should not be null");
            assertNotNull(result.getSchedule(), "Schedule for " + algorithm + " should not be null");
            assertTrue(result.getSchedule().getId().startsWith(algorithm + "_"), 
                     "Schedule ID should start with " + algorithm + "_");
        }
    }

    @Test
    @DisplayName("Test compare algorithms")
    void testCompareAlgorithms() {
        // First run optimization to populate algorithm state
        service.optimizeScheduleWithAllAlgorithms();
        
        Map<String, AppointmentSchedulingService.AlgorithmComparisonResult> comparison = service.compareAlgorithms();
        
        assertNotNull(comparison, "Comparison results should not be null");
        assertEquals(3, comparison.size(), "Should have 3 comparison results");
        assertTrue(comparison.containsKey("CSP"), "Should contain CSP comparison");
        assertTrue(comparison.containsKey("GA"), "Should contain GA comparison");
        assertTrue(comparison.containsKey("SA"), "Should contain SA comparison");
        
        // Verify each comparison result
        for (Map.Entry<String, AppointmentSchedulingService.AlgorithmComparisonResult> entry : comparison.entrySet()) {
            String algorithm = entry.getKey();
            AppointmentSchedulingService.AlgorithmComparisonResult result = entry.getValue();
            
            assertNotNull(result, "Comparison result for " + algorithm + " should not be null");
            assertEquals(algorithm, result.getAlgorithmName(), "Algorithm name should match");
            assertTrue(result.getExecutionTime() >= 0, "Execution time should be non-negative");
            assertTrue(result.getIterations() >= 0, "Iterations should be non-negative");
        }
    }

    @Test
    @DisplayName("Test with empty appointments")
    void testWithEmptyAppointments() {
        AppointmentSchedulingService emptyService = new AppointmentSchedulingService(
            Arrays.asList(), resources);
        
        Schedule schedule = emptyService.optimizeSchedule("CSP");
        assertNotNull(schedule, "Schedule should not be null");
        assertTrue(schedule.getAppointments().isEmpty(), "Appointments should be empty");
    }

    @Test
    @DisplayName("Test with empty resources")
    void testWithEmptyResources() {
        AppointmentSchedulingService emptyService = new AppointmentSchedulingService(
            appointments, Arrays.asList());
        
        Schedule schedule = emptyService.optimizeSchedule("CSP");
        assertNotNull(schedule, "Schedule should not be null");
        // All appointments should be unassigned
        assertEquals(appointments.size(), schedule.getUnassignedAppointments().size(),
                   "All appointments should be unassigned");
    }

    @Test
    @DisplayName("Test with conflicting appointments")
    void testWithConflictingAppointments() {
        // Create two appointments at the same time
        LocalDateTime sameTime = LocalDateTime.now().plusHours(1);
        List<Appointment> conflictingAppointments = Arrays.asList(
            createAppointment("CONFLICT1", "Conflict 1", sameTime, Duration.ofMinutes(30)),
            createAppointment("CONFLICT2", "Conflict 2", sameTime, Duration.ofMinutes(30))
        );
        
        AppointmentSchedulingService conflictService = new AppointmentSchedulingService(
            conflictingAppointments, resources);
        
        Schedule schedule = conflictService.optimizeSchedule("CSP");
        assertNotNull(schedule, "Schedule should not be null");
        // At least one appointment should be unassigned due to conflict
        assertTrue(schedule.getUnassignedAppointments().size() > 0 || 
                  schedule.getConflictCount() > 0,
                  "Should have conflicts or unassigned appointments");
    }

    @Test
    @DisplayName("Test with flexible appointments")
    void testWithFlexibleAppointments() {
        // Create a flexible appointment
        Appointment flexible = createAppointment("FLEX001", "Flexible", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        flexible.setFlexible(true);
        flexible.setFlexibilityWindow(Duration.ofHours(2));
        
        List<Appointment> flexibleAppointments = Arrays.asList(flexible);
        AppointmentSchedulingService flexibleService = new AppointmentSchedulingService(
            flexibleAppointments, resources);
        
        Schedule schedule = flexibleService.optimizeSchedule("CSP");
        assertNotNull(schedule, "Schedule should not be null");
        assertNotNull(schedule.getAppointments(), "Appointments should not be null");
    }

    @Test
    @DisplayName("Test with high priority appointments")
    void testWithHighPriorityAppointments() {
        // Create high priority appointment
        Appointment urgent = createAppointment("URGENT001", "Urgent", 
            LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        urgent.setPriority(Priority.URGENT);
        urgent.setType(AppointmentType.EMERGENCY);
        
        List<Appointment> urgentAppointments = Arrays.asList(urgent);
        AppointmentSchedulingService urgentService = new AppointmentSchedulingService(
            urgentAppointments, resources);
        
        Schedule schedule = urgentService.optimizeSchedule("CSP");
        assertNotNull(schedule, "Schedule should not be null");
        // High priority appointments should be scheduled if possible
        assertTrue(schedule.getUnassignedAppointments().isEmpty() || 
                  schedule.getAppointments().size() > 0,
                  "Should have some scheduled appointments");
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
