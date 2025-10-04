package com.vikk.appointmentscheduler.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a complete schedule with appointments and their resource assignments.
 */
public class Schedule {
    private String id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Appointment> appointments;
    private Map<String, String> resourceAssignments; // appointmentId -> resourceId
    private Map<String, List<Appointment>> resourceSchedules; // resourceId -> appointments
    private double totalCost;
    private double totalScore;
    private int conflictCount;
    private Set<String> unassignedAppointments;
    private ScheduleMetrics metrics;

    public Schedule() {
        this.appointments = new ArrayList<>();
        this.resourceAssignments = new HashMap<>();
        this.resourceSchedules = new HashMap<>();
        this.unassignedAppointments = new HashSet<>();
        this.metrics = new ScheduleMetrics();
    }

    public Schedule(String id, LocalDateTime startDate, LocalDateTime endDate) {
        this();
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public Map<String, String> getResourceAssignments() {
        return resourceAssignments;
    }

    public void setResourceAssignments(Map<String, String> resourceAssignments) {
        this.resourceAssignments = resourceAssignments;
    }

    public Map<String, List<Appointment>> getResourceSchedules() {
        return resourceSchedules;
    }

    public void setResourceSchedules(Map<String, List<Appointment>> resourceSchedules) {
        this.resourceSchedules = resourceSchedules;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public int getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(int conflictCount) {
        this.conflictCount = conflictCount;
    }

    public Set<String> getUnassignedAppointments() {
        return unassignedAppointments;
    }

    public void setUnassignedAppointments(Set<String> unassignedAppointments) {
        this.unassignedAppointments = unassignedAppointments;
    }

    public ScheduleMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(ScheduleMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Adds an appointment to the schedule with resource assignment.
     */
    public void addAppointment(Appointment appointment, String resourceId) {
        if (appointment == null) {
            return;
        }
        
        appointments.add(appointment);
        if (resourceId != null) {
            resourceAssignments.put(appointment.getId(), resourceId);
            resourceSchedules.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(appointment);
        } else {
            unassignedAppointments.add(appointment.getId());
        }
    }

    /**
     * Removes an appointment from the schedule.
     */
    public void removeAppointment(String appointmentId) {
        appointments.removeIf(apt -> apt.getId().equals(appointmentId));
        String resourceId = resourceAssignments.remove(appointmentId);
        if (resourceId != null) {
            resourceSchedules.get(resourceId).removeIf(apt -> apt.getId().equals(appointmentId));
        }
        unassignedAppointments.remove(appointmentId);
    }

    /**
     * Gets all appointments for a specific resource.
     */
    public List<Appointment> getAppointmentsForResource(String resourceId) {
        return resourceSchedules.getOrDefault(resourceId, new ArrayList<>());
    }

    /**
     * Gets the resource assigned to a specific appointment.
     */
    public String getResourceForAppointment(String appointmentId) {
        return resourceAssignments.get(appointmentId);
    }

    /**
     * Checks if the schedule has any conflicts.
     */
    public boolean hasConflicts() {
        return conflictCount > 0;
    }

    /**
     * Calculates the utilization rate of resources.
     */
    public double calculateUtilizationRate() {
        if (resourceSchedules.isEmpty()) {
            return 0.0;
        }
        
        long totalScheduledTime = 0;
        long totalAvailableTime = 0;
        
        for (Map.Entry<String, List<Appointment>> entry : resourceSchedules.entrySet()) {
            List<Appointment> resourceAppointments = entry.getValue();
            for (Appointment appointment : resourceAppointments) {
                totalScheduledTime += appointment.getDuration().toMinutes();
            }
            
            // Calculate available time for this resource
            long availableMinutes = java.time.Duration.between(startDate, endDate).toMinutes();
            totalAvailableTime += availableMinutes;
        }
        
        return totalAvailableTime > 0 ? (double) totalScheduledTime / totalAvailableTime : 0.0;
    }

    /**
     * Calculates the efficiency score of the schedule.
     */
    public double calculateEfficiencyScore() {
        double utilizationScore = calculateUtilizationRate();
        double conflictPenalty = Math.max(0, 1.0 - (conflictCount * 0.1));
        double assignmentScore = unassignedAppointments.isEmpty() ? 1.0 : 
            (double) (appointments.size() - unassignedAppointments.size()) / appointments.size();
        
        return (utilizationScore * 0.4 + conflictPenalty * 0.4 + assignmentScore * 0.2) * 100;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(id, schedule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id='" + id + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", appointmentCount=" + appointments.size() +
                ", totalCost=" + totalCost +
                ", conflictCount=" + conflictCount +
                '}';
    }
}

