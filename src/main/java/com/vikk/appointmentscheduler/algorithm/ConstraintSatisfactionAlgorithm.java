package com.vikk.appointmentscheduler.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.Schedule;
import com.vikk.appointmentscheduler.util.MathUtils;

/**
 * Constraint Satisfaction Problem (CSP) algorithm for appointment scheduling.
 * This algorithm uses backtracking with constraint propagation to find valid schedules.
 * 
 * Time Complexity: O(b^d) where b is the branching factor and d is the depth
 * Space Complexity: O(d) for the recursion stack
 */
public class ConstraintSatisfactionAlgorithm implements SchedulingAlgorithm {
    
    private static final int MAX_BACKTRACK_ATTEMPTS = 10000;
    private static final double CONSTRAINT_VIOLATION_PENALTY = 1000.0;
    
    private final List<Appointment> appointments;
    private final List<Resource> resources;
    private final Map<String, Set<String>> constraints;
    private int backtrackCount;
    private long startTime;
    
    public ConstraintSatisfactionAlgorithm(List<Appointment> appointments, List<Resource> resources) {
        this.appointments = new ArrayList<>(appointments);
        this.resources = new ArrayList<>(resources);
        this.constraints = new HashMap<>();
        this.backtrackCount = 0;
    }

    @Override
    public Schedule optimize() {
        startTime = System.currentTimeMillis();
        backtrackCount = 0;
        
        // Sort appointments by priority and importance for better constraint propagation
        appointments.sort((a1, a2) -> {
            int priorityCompare = a2.getPriority().getLevel() - a1.getPriority().getLevel();
            if (priorityCompare != 0) return priorityCompare;
            return Double.compare(a2.calculateScore(), a1.calculateScore());
        });
        
        Schedule schedule = new Schedule("CSP_" + System.currentTimeMillis(), 
                                       getEarliestStartTime(), getLatestEndTime());
        
        Map<String, String> assignments = new HashMap<>();
        Set<String> assignedAppointments = new HashSet<>();
        
        boolean success = backtrack(assignments, assignedAppointments, 0);
        
        if (success) {
            // Build the final schedule
            for (Map.Entry<String, String> entry : assignments.entrySet()) {
                String appointmentId = entry.getKey();
                String resourceId = entry.getValue();
                
                Appointment appointment = findAppointmentById(appointmentId);
                if (appointment != null) {
                    schedule.addAppointment(appointment, resourceId);
                }
            }
            
            calculateScheduleMetrics(schedule);
        }
        
        // Mark unassigned appointments
        for (Appointment appointment : appointments) {
            if (!assignments.containsKey(appointment.getId())) {
                schedule.getUnassignedAppointments().add(appointment.getId());
            }
        }
        
        return schedule;
    }

    /**
     * Backtracking algorithm with constraint propagation.
     * Time Complexity: O(b^d) where b is average branching factor, d is depth
     */
    private boolean backtrack(Map<String, String> assignments, 
                            Set<String> assignedAppointments, 
                            int appointmentIndex) {
        
        if (appointmentIndex >= appointments.size()) {
            return true; // All appointments assigned
        }
        
        if (backtrackCount >= MAX_BACKTRACK_ATTEMPTS) {
            return false; // Prevent infinite loops
        }
        
        backtrackCount++;
        
        Appointment currentAppointment = appointments.get(appointmentIndex);
        List<Resource> validResources = getValidResources(currentAppointment, assignments);
        
        // Try each valid resource
        for (Resource resource : validResources) {
            if (isValidAssignment(currentAppointment, resource, assignments)) {
                // Make assignment
                assignments.put(currentAppointment.getId(), resource.getId());
                assignedAppointments.add(currentAppointment.getId());
                
                // Recursive call
                if (backtrack(assignments, assignedAppointments, appointmentIndex + 1)) {
                    return true;
                }
                
                // Backtrack
                assignments.remove(currentAppointment.getId());
                assignedAppointments.remove(currentAppointment.getId());
            }
        }
        
        return false;
    }

    /**
     * Gets valid resources for an appointment based on constraints.
     * Time Complexity: O(r * c) where r is number of resources, c is constraint complexity
     */
    private List<Resource> getValidResources(Appointment appointment, Map<String, String> assignments) {
        return resources.stream()
                .filter(resource -> resource.isActive())
                .filter(resource -> resource.hasRequiredCapabilities(appointment.getRequiredCapabilities()))
                .filter(resource -> resource.isAvailableAt(appointment.getStartTime(), appointment.getDuration()))
                .filter(resource -> !hasResourceConflict(appointment, resource, assignments))
                .sorted((r1, r2) -> Double.compare(calculateResourceCost(r1, appointment), 
                                                  calculateResourceCost(r2, appointment)))
                .collect(Collectors.toList());
    }

    /**
     * Checks if an assignment is valid considering all constraints.
     * Time Complexity: O(n) where n is number of existing assignments
     */
    private boolean isValidAssignment(Appointment appointment, Resource resource, 
                                    Map<String, String> assignments) {
        // Check time conflicts
        if (hasTimeConflict(appointment, resource, assignments)) {
            return false;
        }
        
        // Check resource conflicts
        if (hasResourceConflict(appointment, resource, assignments)) {
            return false;
        }
        
        // Check capability constraints
        if (!resource.hasRequiredCapabilities(appointment.getRequiredCapabilities())) {
            return false;
        }
        
        // Check availability
        if (!resource.isAvailableAt(appointment.getStartTime(), appointment.getDuration())) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks for time conflicts with existing assignments.
     * Time Complexity: O(n) where n is number of existing assignments
     */
    private boolean hasTimeConflict(Appointment appointment, Resource resource, 
                                  Map<String, String> assignments) {
        List<Appointment> resourceAppointments = getAppointmentsForResource(resource.getId(), assignments);
        
        for (Appointment existingAppointment : resourceAppointments) {
            if (appointment.conflictsWith(existingAppointment)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks for resource conflicts (e.g., equipment conflicts).
     * Time Complexity: O(1) - constant time lookup
     */
    private boolean hasResourceConflict(Appointment appointment, Resource resource, 
                                      Map<String, String> assignments) {
        // Check if resource conflicts with any other resource
        for (Resource otherResource : resources) {
            if (!otherResource.getId().equals(resource.getId()) && 
                resource.conflictsWith(otherResource)) {
                
                // Check if the conflicting resource is in use
                List<Appointment> otherAppointments = getAppointmentsForResource(otherResource.getId(), assignments);
                for (Appointment otherAppointment : otherAppointments) {
                    if (appointment.conflictsWith(otherAppointment)) {
                        return true;
                    }
                }
            }
        }
        
        // Check for time conflicts with appointments already assigned to this resource
        List<Appointment> existingAppointments = getAppointmentsForResource(resource.getId(), assignments);
        for (Appointment existingAppointment : existingAppointments) {
            if (appointment.conflictsWith(existingAppointment)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Gets appointments assigned to a specific resource.
     * Time Complexity: O(n) where n is number of appointments
     */
    private List<Appointment> getAppointmentsForResource(String resourceId, Map<String, String> assignments) {
        return assignments.entrySet().stream()
                .filter(entry -> resourceId.equals(entry.getValue()))
                .map(entry -> findAppointmentById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the cost of using a resource for an appointment.
     * Time Complexity: O(1)
     */
    private double calculateResourceCost(Resource resource, Appointment appointment) {
        double baseCost = resource.calculateCost(appointment.getDuration());
        double capabilityBonus = calculateCapabilityMatch(resource, appointment);
        return baseCost - capabilityBonus; // Lower cost is better
    }

    /**
     * Calculates how well a resource matches appointment requirements.
     * Time Complexity: O(c) where c is number of capabilities
     */
    private double calculateCapabilityMatch(Resource resource, Appointment appointment) {
        Set<String> required = appointment.getRequiredCapabilities();
        Set<String> preferred = appointment.getPreferredCapabilities();
        Set<String> resourceCaps = resource.getCapabilities();
        
        double requiredMatch = MathUtils.isSubset(required, resourceCaps) ? 1.0 : 0.0;
        double preferredMatch = MathUtils.hasIntersection(preferred, resourceCaps) ? 0.5 : 0.0;
        
        return requiredMatch + preferredMatch;
    }

    /**
     * Calculates comprehensive schedule metrics.
     * Time Complexity: O(n * m) where n is appointments, m is resources
     */
    private void calculateScheduleMetrics(Schedule schedule) {
        double totalCost = 0.0;
        double totalScore = 0.0;
        int conflictCount = 0;
        
        for (Appointment appointment : schedule.getAppointments()) {
            String resourceId = schedule.getResourceForAppointment(appointment.getId());
            if (resourceId != null) {
                Resource resource = findResourceById(resourceId);
                if (resource != null) {
                    totalCost += resource.calculateCost(appointment.getDuration());
                }
            }
            
            totalScore += appointment.calculateScore();
            
            // Count conflicts
            for (Appointment other : schedule.getAppointments()) {
                if (!appointment.getId().equals(other.getId()) && 
                    appointment.conflictsWith(other)) {
                    conflictCount++;
                }
            }
        }
        
        schedule.setTotalCost(totalCost);
        schedule.setTotalScore(totalScore);
        schedule.setConflictCount(conflictCount / 2); // Each conflict counted twice
        
        // Calculate efficiency score
        double efficiencyScore = calculateEfficiencyScore(schedule);
        schedule.getMetrics().setEfficiencyScore(efficiencyScore);
    }

    /**
     * Calculates the overall efficiency score of the schedule.
     * Time Complexity: O(n) where n is number of appointments
     */
    private double calculateEfficiencyScore(Schedule schedule) {
        double utilizationRate = schedule.calculateUtilizationRate();
        double conflictPenalty = Math.max(0, 1.0 - (schedule.getConflictCount() * 0.1));
        double assignmentRate = schedule.getAppointments().size() > 0 ? 
            (double) (schedule.getAppointments().size() - schedule.getUnassignedAppointments().size()) / 
            schedule.getAppointments().size() : 0.0;
        
        return (utilizationRate * 0.4 + conflictPenalty * 0.4 + assignmentRate * 0.2) * 100;
    }

    // Helper methods
    private Appointment findAppointmentById(String id) {
        return appointments.stream()
                .filter(apt -> apt.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Resource findResourceById(String id) {
        return resources.stream()
                .filter(res -> res.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime getEarliestStartTime() {
        return appointments.stream()
                .map(Appointment::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    private LocalDateTime getLatestEndTime() {
        return appointments.stream()
                .map(Appointment::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().plusDays(1));
    }

    @Override
    public String getAlgorithmName() {
        return "Constraint Satisfaction Problem (CSP)";
    }

    @Override
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public int getBacktrackCount() {
        return backtrackCount;
    }
}

