package com.vikk.appointmentscheduler.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Metrics and statistics for a schedule.
 */
public class ScheduleMetrics {
    private double totalCost;
    private double averageCostPerAppointment;
    private double totalScore;
    private double averageScorePerAppointment;
    private int totalAppointments;
    private int assignedAppointments;
    private int unassignedAppointments;
    private int conflictCount;
    private double utilizationRate;
    private double efficiencyScore;
    private Duration totalScheduledTime;
    private Duration totalAvailableTime;
    private Map<String, Integer> resourceUtilization;
    private Map<Priority, Integer> priorityDistribution;
    private Map<AppointmentType, Integer> typeDistribution;

    public ScheduleMetrics() {
        this.resourceUtilization = new HashMap<>();
        this.priorityDistribution = new HashMap<>();
        this.typeDistribution = new HashMap<>();
        this.totalScheduledTime = Duration.ZERO;
        this.totalAvailableTime = Duration.ZERO;
    }

    // Getters and setters
    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getAverageCostPerAppointment() {
        return averageCostPerAppointment;
    }

    public void setAverageCostPerAppointment(double averageCostPerAppointment) {
        this.averageCostPerAppointment = averageCostPerAppointment;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getAverageScorePerAppointment() {
        return averageScorePerAppointment;
    }

    public void setAverageScorePerAppointment(double averageScorePerAppointment) {
        this.averageScorePerAppointment = averageScorePerAppointment;
    }

    public int getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(int totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public int getAssignedAppointments() {
        return assignedAppointments;
    }

    public void setAssignedAppointments(int assignedAppointments) {
        this.assignedAppointments = assignedAppointments;
    }

    public int getUnassignedAppointments() {
        return unassignedAppointments;
    }

    public void setUnassignedAppointments(int unassignedAppointments) {
        this.unassignedAppointments = unassignedAppointments;
    }

    public int getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(int conflictCount) {
        this.conflictCount = conflictCount;
    }

    public double getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(double utilizationRate) {
        this.utilizationRate = utilizationRate;
    }

    public double getEfficiencyScore() {
        return efficiencyScore;
    }

    public void setEfficiencyScore(double efficiencyScore) {
        this.efficiencyScore = efficiencyScore;
    }

    public Duration getTotalScheduledTime() {
        return totalScheduledTime;
    }

    public void setTotalScheduledTime(Duration totalScheduledTime) {
        this.totalScheduledTime = totalScheduledTime;
    }

    public Duration getTotalAvailableTime() {
        return totalAvailableTime;
    }

    public void setTotalAvailableTime(Duration totalAvailableTime) {
        this.totalAvailableTime = totalAvailableTime;
    }

    public Map<String, Integer> getResourceUtilization() {
        return resourceUtilization;
    }

    public void setResourceUtilization(Map<String, Integer> resourceUtilization) {
        this.resourceUtilization = resourceUtilization;
    }

    public Map<Priority, Integer> getPriorityDistribution() {
        return priorityDistribution;
    }

    public void setPriorityDistribution(Map<Priority, Integer> priorityDistribution) {
        this.priorityDistribution = priorityDistribution;
    }

    public Map<AppointmentType, Integer> getTypeDistribution() {
        return typeDistribution;
    }

    public void setTypeDistribution(Map<AppointmentType, Integer> typeDistribution) {
        this.typeDistribution = typeDistribution;
    }

    /**
     * Calculates the assignment rate (percentage of appointments that are assigned).
     */
    public double getAssignmentRate() {
        return totalAppointments > 0 ? (double) assignedAppointments / totalAppointments * 100 : 0.0;
    }

    /**
     * Calculates the conflict rate (percentage of appointments with conflicts).
     */
    public double getConflictRate() {
        return totalAppointments > 0 ? (double) conflictCount / totalAppointments * 100 : 0.0;
    }

    /**
     * Gets the most utilized resource.
     */
    public String getMostUtilizedResource() {
        return resourceUtilization.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Gets the least utilized resource.
     */
    public String getLeastUtilizedResource() {
        return resourceUtilization.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}

