package com.vikk.appointmentscheduler.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vikk.appointmentscheduler.algorithm.ConstraintSatisfactionAlgorithm;
import com.vikk.appointmentscheduler.algorithm.GeneticAlgorithm;
import com.vikk.appointmentscheduler.algorithm.SchedulingAlgorithm;
import com.vikk.appointmentscheduler.algorithm.SimulatedAnnealingAlgorithm;
import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.Schedule;
import com.vikk.appointmentscheduler.util.MathUtils;

/**
 * Main service for appointment scheduling optimization.
 * Orchestrates different algorithms and provides a unified interface.
 */
public class AppointmentSchedulingService {
    
    private final List<Appointment> appointments;
    private final List<Resource> resources;
    private final Map<String, SchedulingAlgorithm> algorithms;
    
    public AppointmentSchedulingService(List<Appointment> appointments, List<Resource> resources) {
        this.appointments = new ArrayList<>(appointments);
        this.resources = new ArrayList<>(resources);
        this.algorithms = new HashMap<>();
        initializeAlgorithms();
    }
    
    /**
     * Initializes available scheduling algorithms.
     */
    private void initializeAlgorithms() {
        algorithms.put("CSP", new ConstraintSatisfactionAlgorithm(appointments, resources));
        algorithms.put("GA", new GeneticAlgorithm(appointments, resources));
        algorithms.put("SA", new SimulatedAnnealingAlgorithm(appointments, resources));
    }
    
    /**
     * Optimizes schedule using the specified algorithm.
     * 
     * @param algorithmName name of the algorithm to use
     * @return optimized schedule
     */
    public Schedule optimizeSchedule(String algorithmName) {
        SchedulingAlgorithm algorithm = algorithms.get(algorithmName);
        if (algorithm == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
        
        return algorithm.optimize();
    }
    
    /**
     * Optimizes schedule using all available algorithms and returns the best result.
     * 
     * @return best schedule found across all algorithms
     */
    public Schedule optimizeScheduleWithAllAlgorithms() {
        List<Schedule> results = new ArrayList<>();
        
        for (Map.Entry<String, SchedulingAlgorithm> entry : algorithms.entrySet()) {
            try {
                Schedule schedule = entry.getValue().optimize();
                schedule.setId(entry.getKey() + "_" + System.currentTimeMillis());
                results.add(schedule);
            } catch (Exception e) {
                System.err.println("Error running algorithm " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        if (results.isEmpty()) {
            throw new RuntimeException("All algorithms failed to produce a schedule");
        }
        
        // Return the schedule with the highest efficiency score
        return results.stream()
                .max(Comparator.comparing(Schedule::calculateEfficiencyScore))
                .orElse(results.get(0));
    }
    
    /**
     * Compares performance of all algorithms.
     * 
     * @return map of algorithm names to their results
     */
    public Map<String, AlgorithmComparisonResult> compareAlgorithms() {
        Map<String, AlgorithmComparisonResult> results = new HashMap<>();
        
        for (Map.Entry<String, SchedulingAlgorithm> entry : algorithms.entrySet()) {
            try {
                long startTime = System.currentTimeMillis();
                Schedule schedule = entry.getValue().optimize();
                long executionTime = System.currentTimeMillis() - startTime;
                
                AlgorithmComparisonResult result = new AlgorithmComparisonResult(
                    entry.getKey(),
                    schedule,
                    executionTime,
                    entry.getValue().getBacktrackCount(),
                    schedule.calculateEfficiencyScore(),
                    schedule.getTotalCost(),
                    schedule.getConflictCount()
                );
                
                results.put(entry.getKey(), result);
            } catch (Exception e) {
                System.err.println("Error running algorithm " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Validates a schedule for conflicts and constraints.
     * 
     * @param schedule schedule to validate
     * @return validation result
     */
    public ScheduleValidationResult validateSchedule(Schedule schedule) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check for time conflicts
        for (int i = 0; i < schedule.getAppointments().size(); i++) {
            Appointment apt1 = schedule.getAppointments().get(i);
            for (int j = i + 1; j < schedule.getAppointments().size(); j++) {
                Appointment apt2 = schedule.getAppointments().get(j);
                
                if (apt1.conflictsWith(apt2)) {
                    String resource1 = schedule.getResourceForAppointment(apt1.getId());
                    String resource2 = schedule.getResourceForAppointment(apt2.getId());
                    
                    if (resource1 != null && resource1.equals(resource2)) {
                        errors.add("Time conflict between " + apt1.getId() + " and " + apt2.getId() + 
                                 " on resource " + resource1);
                    }
                }
            }
        }
        
        // Check for unassigned appointments
        for (String unassignedId : schedule.getUnassignedAppointments()) {
            warnings.add("Appointment " + unassignedId + " is not assigned to any resource");
        }
        
        // Check resource availability
        for (Appointment appointment : schedule.getAppointments()) {
            String resourceId = schedule.getResourceForAppointment(appointment.getId());
            if (resourceId != null) {
                Resource resource = findResourceById(resourceId);
                if (resource != null) {
                    if (!resource.isAvailableAt(appointment.getStartTime(), appointment.getDuration())) {
                        errors.add("Appointment " + appointment.getId() + " scheduled outside resource " + 
                                 resourceId + " availability");
                    }
                    
                    if (!resource.hasRequiredCapabilities(appointment.getRequiredCapabilities())) {
                        errors.add("Resource " + resourceId + " does not have required capabilities for " + 
                                 appointment.getId());
                    }
                }
            }
        }
        
        return new ScheduleValidationResult(errors, warnings);
    }
    
    /**
     * Generates a report comparing multiple schedules.
     * 
     * @param schedules schedules to compare
     * @return comparison report
     */
    public ScheduleComparisonReport generateComparisonReport(List<Schedule> schedules) {
        if (schedules.isEmpty()) {
            throw new IllegalArgumentException("Cannot compare empty schedule list");
        }
        
        ScheduleComparisonReport report = new ScheduleComparisonReport();
        
        // Calculate statistics
        List<Double> efficiencyScores = schedules.stream()
                .map(Schedule::calculateEfficiencyScore)
                .collect(Collectors.toList());
        
        List<Double> totalCosts = schedules.stream()
                .map(Schedule::getTotalCost)
                .collect(Collectors.toList());
        
        List<Integer> conflictCounts = schedules.stream()
                .map(Schedule::getConflictCount)
                .collect(Collectors.toList());
        
        // Find best and worst
        Schedule bestSchedule = schedules.stream()
                .max(Comparator.comparing(Schedule::calculateEfficiencyScore))
                .orElse(schedules.get(0));
        
        Schedule worstSchedule = schedules.stream()
                .min(Comparator.comparing(Schedule::calculateEfficiencyScore))
                .orElse(schedules.get(0));
        
        // Calculate statistics
        report.setBestSchedule(bestSchedule);
        report.setWorstSchedule(worstSchedule);
        report.setAverageEfficiencyScore(MathUtils.mean(efficiencyScores));
        report.setEfficiencyScoreStdDev(MathUtils.standardDeviation(efficiencyScores));
        report.setAverageCost(MathUtils.mean(totalCosts));
        report.setAverageConflictCount(MathUtils.mean(conflictCounts.stream()
                .map(Integer::doubleValue)
                .collect(Collectors.toList())));
        
        return report;
    }
    
    /**
     * Adds a new appointment to the system.
     * 
     * @param appointment appointment to add
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        // Reinitialize algorithms with new appointment
        initializeAlgorithms();
    }
    
    /**
     * Removes an appointment from the system.
     * 
     * @param appointmentId ID of appointment to remove
     */
    public void removeAppointment(String appointmentId) {
        appointments.removeIf(apt -> apt.getId().equals(appointmentId));
        // Reinitialize algorithms without the appointment
        initializeAlgorithms();
    }
    
    /**
     * Adds a new resource to the system.
     * 
     * @param resource resource to add
     */
    public void addResource(Resource resource) {
        resources.add(resource);
        // Reinitialize algorithms with new resource
        initializeAlgorithms();
    }
    
    /**
     * Removes a resource from the system.
     * 
     * @param resourceId ID of resource to remove
     */
    public void removeResource(String resourceId) {
        resources.removeIf(res -> res.getId().equals(resourceId));
        // Reinitialize algorithms without the resource
        initializeAlgorithms();
    }
    
    /**
     * Gets all available algorithms.
     * 
     * @return set of algorithm names
     */
    public Set<String> getAvailableAlgorithms() {
        return algorithms.keySet();
    }

    public List<SchedulingAlgorithm> getRegisteredAlgorithms() {
        return new ArrayList<>(algorithms.values());
    }
    
    /**
     * Gets all appointments.
     * 
     * @return list of appointments
     */
    public List<Appointment> getAppointments() {
        return new ArrayList<>(appointments);
    }
    
    /**
     * Gets all resources.
     * 
     * @return list of resources
     */
    public List<Resource> getResources() {
        return new ArrayList<>(resources);
    }
    
    // Helper methods
    private Resource findResourceById(String id) {
        return resources.stream()
                .filter(res -> res.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Result of algorithm comparison.
     */
    public static class AlgorithmComparisonResult {
        private final String algorithmName;
        private final Schedule schedule;
        private final long executionTime;
        private final int iterations;
        private final double efficiencyScore;
        private final double totalCost;
        private final int conflictCount;
        
        public AlgorithmComparisonResult(String algorithmName, Schedule schedule, 
                                      long executionTime, int iterations, 
                                      double efficiencyScore, double totalCost, int conflictCount) {
            this.algorithmName = algorithmName;
            this.schedule = schedule;
            this.executionTime = executionTime;
            this.iterations = iterations;
            this.efficiencyScore = efficiencyScore;
            this.totalCost = totalCost;
            this.conflictCount = conflictCount;
        }
        
        // Getters
        public String getAlgorithmName() { return algorithmName; }
        public Schedule getSchedule() { return schedule; }
        public long getExecutionTime() { return executionTime; }
        public int getIterations() { return iterations; }
        public double getEfficiencyScore() { return efficiencyScore; }
        public double getTotalCost() { return totalCost; }
        public int getConflictCount() { return conflictCount; }
    }
    
    /**
     * Result of schedule validation.
     */
    public static class ScheduleValidationResult {
        private final List<String> errors;
        private final List<String> warnings;
        
        public ScheduleValidationResult(List<String> errors, List<String> warnings) {
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public boolean isValid() { return errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
    }
    
    /**
     * Report comparing multiple schedules.
     */
    public static class ScheduleComparisonReport {
        private Schedule bestSchedule;
        private Schedule worstSchedule;
        private double averageEfficiencyScore;
        private double efficiencyScoreStdDev;
        private double averageCost;
        private double averageConflictCount;
        
        // Getters and setters
        public Schedule getBestSchedule() { return bestSchedule; }
        public void setBestSchedule(Schedule bestSchedule) { this.bestSchedule = bestSchedule; }
        
        public Schedule getWorstSchedule() { return worstSchedule; }
        public void setWorstSchedule(Schedule worstSchedule) { this.worstSchedule = worstSchedule; }
        
        public double getAverageEfficiencyScore() { return averageEfficiencyScore; }
        public void setAverageEfficiencyScore(double averageEfficiencyScore) { 
            this.averageEfficiencyScore = averageEfficiencyScore; 
        }
        
        public double getEfficiencyScoreStdDev() { return efficiencyScoreStdDev; }
        public void setEfficiencyScoreStdDev(double efficiencyScoreStdDev) { 
            this.efficiencyScoreStdDev = efficiencyScoreStdDev; 
        }
        
        public double getAverageCost() { return averageCost; }
        public void setAverageCost(double averageCost) { this.averageCost = averageCost; }
        
        public double getAverageConflictCount() { return averageConflictCount; }
        public void setAverageConflictCount(double averageConflictCount) { 
            this.averageConflictCount = averageConflictCount; 
        }
    }
}

