package com.vikk.appointmentscheduler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents an appointment with all necessary scheduling information.
 * This is a core entity in the appointment scheduling optimization system.
 */
public class Appointment {
    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private String resourceId;
    private String clientId;
    private AppointmentType type;
    private Priority priority;
    private Set<String> requiredCapabilities;
    private Set<String> preferredCapabilities;
    private boolean isFlexible;
    private Duration flexibilityWindow;
    private Set<String> conflicts;
    private double importanceScore;
    private AppointmentStatus status;

    public Appointment() {
        this.requiredCapabilities = new HashSet<>();
        this.preferredCapabilities = new HashSet<>();
        this.conflicts = new HashSet<>();
        this.isFlexible = false;
        this.flexibilityWindow = Duration.ZERO;
        this.importanceScore = 1.0;
    }

    public Appointment(String id, String title, LocalDateTime startTime, Duration duration) {
        this();
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = startTime.plus(duration);
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        if (duration != null) {
            this.endTime = startTime.plus(duration);
        }
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        if (startTime != null) {
            this.duration = Duration.between(startTime, endTime);
        }
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        if (startTime != null) {
            this.endTime = startTime.plus(duration);
        }
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public AppointmentType getType() {
        return type;
    }

    public void setType(AppointmentType type) {
        this.type = type;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void setRequiredCapabilities(Set<String> requiredCapabilities) {
        this.requiredCapabilities = requiredCapabilities;
    }

    public Set<String> getPreferredCapabilities() {
        return preferredCapabilities;
    }

    public void setPreferredCapabilities(Set<String> preferredCapabilities) {
        this.preferredCapabilities = preferredCapabilities;
    }

    public boolean isFlexible() {
        return isFlexible;
    }

    public void setFlexible(boolean flexible) {
        isFlexible = flexible;
    }

    public Duration getFlexibilityWindow() {
        return flexibilityWindow;
    }

    public void setFlexibilityWindow(Duration flexibilityWindow) {
        this.flexibilityWindow = flexibilityWindow;
    }

    public Set<String> getConflicts() {
        return conflicts;
    }

    public void setConflicts(Set<String> conflicts) {
        this.conflicts = conflicts;
    }

    public double getImportanceScore() {
        return importanceScore;
    }

    public void setImportanceScore(double importanceScore) {
        this.importanceScore = importanceScore;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public long getDurationMinutes() {
        return duration != null ? duration.toMinutes() : 0;
    }

    /**
     * Checks if this appointment conflicts with another appointment.
     * Two appointments conflict if they have overlapping time slots.
     */
    public boolean conflictsWith(Appointment other) {
        if (other == null || this.id.equals(other.id)) {
            return false;
        }
        
        return this.startTime.isBefore(other.endTime) && 
               this.endTime.isAfter(other.startTime);
    }

    /**
     * Checks if this appointment can be scheduled at the given time.
     */
    public boolean canBeScheduledAt(LocalDateTime newStartTime) {
        if (!isFlexible) {
            return this.startTime.equals(newStartTime);
        }
        
        LocalDateTime earliestStart = this.startTime.minus(flexibilityWindow);
        LocalDateTime latestStart = this.startTime.plus(flexibilityWindow);
        
        return !newStartTime.isBefore(earliestStart) && 
               !newStartTime.isAfter(latestStart);
    }

    /**
     * Calculates the total score for this appointment based on priority and importance.
     */
    public double calculateScore() {
        double priorityMultiplier = switch (priority) {
            case LOW -> 1.0;
            case MEDIUM -> 1.5;
            case HIGH -> 2.0;
            case URGENT -> 3.0;
        };
        
        return importanceScore * priorityMultiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", resourceId='" + resourceId + '\'' +
                ", priority=" + priority +
                '}';
    }
}

