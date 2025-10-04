package com.vikk.appointmentscheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.AppointmentType;
import com.vikk.appointmentscheduler.model.Priority;
import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.ResourceType;
import com.vikk.appointmentscheduler.model.Schedule;
import com.vikk.appointmentscheduler.service.AppointmentSchedulingService;
import com.vikk.appointmentscheduler.util.MathUtils;

/**
 * Main application class for the Appointment Scheduling Optimizer.
 * Demonstrates the system capabilities with realistic scenarios.
 */
public class Application {
    
    public static void main(String[] args) {
        System.out.println("=== Appointment Scheduling Optimizer ===");
        System.out.println("Advanced optimization system using multiple algorithms");
        System.out.println();
        
        // Create sample data
        List<Appointment> appointments = createSampleAppointments();
        List<Resource> resources = createSampleResources();
        
        // Initialize the scheduling service
        AppointmentSchedulingService service = new AppointmentSchedulingService(appointments, resources);
        
        // Display initial data
        displayInitialData(appointments, resources);
        
        // Run optimization with all algorithms
        System.out.println("Running optimization with all algorithms...");
        System.out.println();
        
        Map<String, AppointmentSchedulingService.AlgorithmComparisonResult> results = 
            service.compareAlgorithms();
        
        // Display results
        displayAlgorithmResults(results);
        
        // Find and display the best schedule
        AppointmentSchedulingService.AlgorithmComparisonResult bestResult = 
            results.values().stream()
                .max(Comparator.comparing(AppointmentSchedulingService.AlgorithmComparisonResult::getEfficiencyScore))
                .orElse(null);
        
        if (bestResult != null) {
            System.out.println("=== BEST SCHEDULE ===");
            displaySchedule(bestResult.getSchedule());
            
            // Validate the best schedule
            System.out.println("=== VALIDATION ===");
            AppointmentSchedulingService.ScheduleValidationResult validation = 
                service.validateSchedule(bestResult.getSchedule());
            displayValidationResults(validation);
        }
        
        // Performance analysis
        System.out.println("=== PERFORMANCE ANALYSIS ===");
        displayPerformanceAnalysis(results);
        
        System.out.println("\n=== Optimization Complete ===");
    }
    
    /**
     * Creates sample appointments for demonstration.
     */
    private static List<Appointment> createSampleAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().plusHours(1);
        
        // High priority consultation
        Appointment consultation = new Appointment("APT001", "Initial Consultation", 
            baseTime, Duration.ofMinutes(30));
        consultation.setType(AppointmentType.CONSULTATION);
        consultation.setPriority(Priority.HIGH);
        consultation.setRequiredCapabilities(Set.of("examination_room", "doctor"));
        consultation.setClientId("CLIENT001");
        consultation.setImportanceScore(1.5);
        appointments.add(consultation);
        
        // Follow-up appointment
        Appointment followUp = new Appointment("APT002", "Follow-up Visit", 
            baseTime.plusHours(2), Duration.ofMinutes(15));
        followUp.setType(AppointmentType.FOLLOW_UP);
        followUp.setPriority(Priority.MEDIUM);
        followUp.setRequiredCapabilities(Set.of("examination_room"));
        followUp.setClientId("CLIENT002");
        appointments.add(followUp);
        
        // Treatment appointment
        Appointment treatment = new Appointment("APT003", "Physical Therapy", 
            baseTime.plusHours(3), Duration.ofMinutes(60));
        treatment.setType(AppointmentType.TREATMENT);
        treatment.setPriority(Priority.MEDIUM);
        treatment.setRequiredCapabilities(Set.of("treatment_room", "therapist", "equipment"));
        treatment.setClientId("CLIENT003");
        appointments.add(treatment);
        
        // Emergency appointment
        Appointment emergency = new Appointment("APT004", "Emergency Consultation", 
            baseTime.plusMinutes(30), Duration.ofMinutes(45));
        emergency.setType(AppointmentType.EMERGENCY);
        emergency.setPriority(Priority.URGENT);
        emergency.setRequiredCapabilities(Set.of("emergency_room", "doctor"));
        emergency.setClientId("CLIENT004");
        emergency.setImportanceScore(2.0);
        appointments.add(emergency);
        
        // Surgery appointment
        Appointment surgery = new Appointment("APT005", "Minor Surgery", 
            baseTime.plusHours(4), Duration.ofMinutes(120));
        surgery.setType(AppointmentType.SURGERY);
        surgery.setPriority(Priority.HIGH);
        surgery.setRequiredCapabilities(Set.of("operating_room", "surgeon", "anesthesiologist", "surgical_equipment"));
        surgery.setClientId("CLIENT005");
        surgery.setImportanceScore(1.8);
        appointments.add(surgery);
        
        // Flexible appointment
        Appointment flexible = new Appointment("APT006", "Routine Checkup", 
            baseTime.plusHours(5), Duration.ofMinutes(20));
        flexible.setType(AppointmentType.DIAGNOSTIC);
        flexible.setPriority(Priority.LOW);
        flexible.setRequiredCapabilities(Set.of("examination_room"));
        flexible.setClientId("CLIENT006");
        flexible.setFlexible(true);
        flexible.setFlexibilityWindow(Duration.ofHours(2));
        appointments.add(flexible);
        
        return appointments;
    }
    
    /**
     * Creates sample resources for demonstration.
     */
    private static List<Resource> createSampleResources() {
        List<Resource> resources = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Examination room
        Resource examRoom1 = new Resource("ROOM001", "Examination Room 1", ResourceType.ROOM);
        examRoom1.setCapabilities(Set.of("examination_room", "basic_equipment"));
        examRoom1.setAvailableFrom(now);
        examRoom1.setAvailableTo(now.plusDays(1));
        examRoom1.setCostPerHour(50.0);
        examRoom1.setCapacity(1);
        resources.add(examRoom1);
        
        // Treatment room
        Resource treatmentRoom = new Resource("ROOM002", "Treatment Room", ResourceType.ROOM);
        treatmentRoom.setCapabilities(Set.of("treatment_room", "equipment", "therapist"));
        treatmentRoom.setAvailableFrom(now);
        treatmentRoom.setAvailableTo(now.plusDays(1));
        treatmentRoom.setCostPerHour(75.0);
        treatmentRoom.setCapacity(1);
        resources.add(treatmentRoom);
        
        // Emergency room
        Resource emergencyRoom = new Resource("ROOM003", "Emergency Room", ResourceType.ROOM);
        emergencyRoom.setCapabilities(Set.of("emergency_room", "doctor", "emergency_equipment"));
        emergencyRoom.setAvailableFrom(now);
        emergencyRoom.setAvailableTo(now.plusDays(1));
        emergencyRoom.setCostPerHour(100.0);
        emergencyRoom.setCapacity(1);
        resources.add(emergencyRoom);
        
        // Operating room
        Resource operatingRoom = new Resource("ROOM004", "Operating Room", ResourceType.ROOM);
        operatingRoom.setCapabilities(Set.of("operating_room", "surgeon", "anesthesiologist", "surgical_equipment"));
        operatingRoom.setAvailableFrom(now);
        operatingRoom.setAvailableTo(now.plusDays(1));
        operatingRoom.setCostPerHour(200.0);
        operatingRoom.setCapacity(1);
        resources.add(operatingRoom);
        
        // Doctor resource
        Resource doctor = new Resource("STAFF001", "Dr. Smith", ResourceType.STAFF);
        doctor.setCapabilities(Set.of("doctor", "examination_room", "emergency_room"));
        doctor.setAvailableFrom(now);
        doctor.setAvailableTo(now.plusDays(1));
        doctor.setCostPerHour(150.0);
        doctor.setCapacity(1);
        resources.add(doctor);
        
        // Therapist resource
        Resource therapist = new Resource("STAFF002", "Physical Therapist", ResourceType.STAFF);
        therapist.setCapabilities(Set.of("therapist", "treatment_room", "equipment"));
        therapist.setAvailableFrom(now);
        therapist.setAvailableTo(now.plusDays(1));
        therapist.setCostPerHour(80.0);
        therapist.setCapacity(1);
        resources.add(therapist);
        
        // Surgeon resource
        Resource surgeon = new Resource("STAFF003", "Dr. Johnson (Surgeon)", ResourceType.STAFF);
        surgeon.setCapabilities(Set.of("surgeon", "operating_room", "surgical_equipment"));
        surgeon.setAvailableFrom(now);
        surgeon.setAvailableTo(now.plusDays(1));
        surgeon.setCostPerHour(300.0);
        surgeon.setCapacity(1);
        resources.add(surgeon);
        
        return resources;
    }
    
    /**
     * Displays initial data.
     */
    private static void displayInitialData(List<Appointment> appointments, List<Resource> resources) {
        System.out.println("=== INITIAL DATA ===");
        System.out.println("Appointments: " + appointments.size());
        for (Appointment apt : appointments) {
            System.out.println("  " + apt.getId() + ": " + apt.getTitle() + 
                             " (" + apt.getPriority() + ", " + apt.getType() + ")");
        }
        
        System.out.println("\nResources: " + resources.size());
        for (Resource res : resources) {
            System.out.println("  " + res.getId() + ": " + res.getName() + 
                             " (" + res.getType() + ", $" + res.getCostPerHour() + "/hr)");
        }
        System.out.println();
    }
    
    /**
     * Displays algorithm results.
     */
    private static void displayAlgorithmResults(Map<String, AppointmentSchedulingService.AlgorithmComparisonResult> results) {
        System.out.println("=== ALGORITHM COMPARISON ===");
        System.out.printf("%-15s %-12s %-10s %-12s %-10s %-8s%n", 
            "Algorithm", "Time (ms)", "Iterations", "Efficiency", "Cost", "Conflicts");
        System.out.println("-".repeat(80));
        
        for (AppointmentSchedulingService.AlgorithmComparisonResult result : results.values()) {
            System.out.printf("%-15s %-12d %-10d %-12.2f %-10.2f %-8d%n",
                result.getAlgorithmName(),
                (int) result.getExecutionTime(),
                result.getIterations(),
                result.getEfficiencyScore(),
                result.getTotalCost(),
                result.getConflictCount());
        }
        System.out.println();
    }
    
    /**
     * Displays a schedule.
     */
    private static void displaySchedule(Schedule schedule) {
        System.out.println("Schedule ID: " + schedule.getId());
        System.out.println("Total Appointments: " + schedule.getAppointments().size());
        System.out.println("Total Cost: $" + MathUtils.round(schedule.getTotalCost(), 2));
        System.out.println("Efficiency Score: " + MathUtils.round(schedule.calculateEfficiencyScore(), 2));
        System.out.println("Conflicts: " + schedule.getConflictCount());
        System.out.println();
        
        System.out.println("Appointments:");
        for (Appointment apt : schedule.getAppointments()) {
            String resourceId = schedule.getResourceForAppointment(apt.getId());
            System.out.printf("  %s: %s -> %s (%d min)%n",
                apt.getId(),
                apt.getTitle(),
                resourceId != null ? resourceId : "UNASSIGNED",
                (int) apt.getDuration().toMinutes());
        }
        System.out.println();
    }
    
    /**
     * Displays validation results.
     */
    private static void displayValidationResults(AppointmentSchedulingService.ScheduleValidationResult validation) {
        if (validation.isValid()) {
            System.out.println("✓ Schedule is valid");
        } else {
            System.out.println("✗ Schedule has errors:");
            for (String error : validation.getErrors()) {
                System.out.println("  - " + error);
            }
        }
        
        if (validation.hasWarnings()) {
            System.out.println("⚠ Warnings:");
            for (String warning : validation.getWarnings()) {
                System.out.println("  - " + warning);
            }
        }
        System.out.println();
    }
    
    /**
     * Displays performance analysis.
     */
    private static void displayPerformanceAnalysis(Map<String, AppointmentSchedulingService.AlgorithmComparisonResult> results) {
        List<Long> executionTimes = results.values().stream()
            .map(AppointmentSchedulingService.AlgorithmComparisonResult::getExecutionTime)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        List<Double> efficiencyScores = results.values().stream()
            .map(AppointmentSchedulingService.AlgorithmComparisonResult::getEfficiencyScore)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        System.out.println("Performance Statistics:");
        System.out.println("  Average Execution Time: " + MathUtils.round(MathUtils.mean(executionTimes.stream()
            .map(Long::doubleValue).collect(ArrayList::new, ArrayList::add, ArrayList::addAll)), 2) + " ms");
        System.out.println("  Average Efficiency Score: " + MathUtils.round(MathUtils.mean(efficiencyScores), 2));
        System.out.println("  Efficiency Score Std Dev: " + MathUtils.round(MathUtils.standardDeviation(efficiencyScores), 2));
        
        // Find fastest and most efficient
        AppointmentSchedulingService.AlgorithmComparisonResult fastest = results.values().stream()
            .min(Comparator.comparing(AppointmentSchedulingService.AlgorithmComparisonResult::getExecutionTime))
            .orElse(null);
        
        AppointmentSchedulingService.AlgorithmComparisonResult mostEfficient = results.values().stream()
            .max(Comparator.comparing(AppointmentSchedulingService.AlgorithmComparisonResult::getEfficiencyScore))
            .orElse(null);
        
        if (fastest != null) {
            System.out.println("  Fastest Algorithm: " + fastest.getAlgorithmName() + 
                             " (" + fastest.getExecutionTime() + " ms)");
        }
        
        if (mostEfficient != null) {
            System.out.println("  Most Efficient Algorithm: " + mostEfficient.getAlgorithmName() + 
                             " (" + MathUtils.round(mostEfficient.getEfficiencyScore(), 2) + ")");
        }
    }
}
