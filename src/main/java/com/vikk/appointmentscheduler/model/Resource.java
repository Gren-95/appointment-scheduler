package com.vikk.appointmentscheduler.model;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a resource (room, equipment, staff) that can be used for appointments.
 */
public class Resource {
    private String id;
    private String name;
    private ResourceType type;
    private Set<String> capabilities;
    private Set<String> requiredCapabilities;
    private LocalDateTime availableFrom;
    private LocalDateTime availableTo;
    private Duration setupTime;
    private Duration cleanupTime;
    private double costPerHour;
    private int capacity;
    private boolean isActive;
    private Set<String> conflicts;

    public Resource() {
        this.capabilities = new HashSet<>();
        this.requiredCapabilities = new HashSet<>();
        this.conflicts = new HashSet<>();
        this.setupTime = Duration.ZERO;
        this.cleanupTime = Duration.ZERO;
        this.costPerHour = 0.0;
        this.capacity = 1;
        this.isActive = true;
    }

    public Resource(String id, String name, ResourceType type) {
        this();
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Resource(String id, String name, ResourceType type, Set<AppointmentType> capabilities, 
                   double costPerHour, Set<LocalDateTime> availableSlots) {
        this();
        this.id = id;
        this.name = name;
        this.type = type;
        this.costPerHour = costPerHour;
        // Convert AppointmentType to String for capabilities
        this.capabilities = capabilities.stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet());
        // Convert available slots to time range
        if (availableSlots != null && !availableSlots.isEmpty()) {
            this.availableFrom = availableSlots.stream().min(LocalDateTime::compareTo).orElse(null);
            this.availableTo = availableSlots.stream().max(LocalDateTime::compareTo).orElse(null);
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<String> capabilities) {
        this.capabilities = capabilities;
    }

    // Convenience method for GUI
    public Set<AppointmentType> getCapabilitiesAsAppointmentTypes() {
        return capabilities.stream()
                .map(AppointmentType::valueOf)
                .collect(java.util.stream.Collectors.toSet());
    }

    public void setCapabilitiesFromAppointmentTypes(Set<AppointmentType> appointmentTypes) {
        this.capabilities = appointmentTypes.stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet());
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void setRequiredCapabilities(Set<String> requiredCapabilities) {
        this.requiredCapabilities = requiredCapabilities;
    }

    public LocalDateTime getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalDateTime availableFrom) {
        this.availableFrom = availableFrom;
    }

    public LocalDateTime getAvailableTo() {
        return availableTo;
    }

    public void setAvailableTo(LocalDateTime availableTo) {
        this.availableTo = availableTo;
    }

    public Duration getSetupTime() {
        return setupTime;
    }

    public void setSetupTime(Duration setupTime) {
        this.setupTime = setupTime;
    }

    public Duration getCleanupTime() {
        return cleanupTime;
    }

    public void setCleanupTime(Duration cleanupTime) {
        this.cleanupTime = cleanupTime;
    }

    public double getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(double costPerHour) {
        this.costPerHour = costPerHour;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Set<String> getConflicts() {
        return conflicts;
    }

    public void setConflicts(Set<String> conflicts) {
        this.conflicts = conflicts;
    }

    // Convenience method for GUI
    public Set<LocalDateTime> getAvailableSlots() {
        Set<LocalDateTime> slots = new HashSet<>();
        if (availableFrom != null && availableTo != null) {
            LocalDateTime current = availableFrom;
            while (!current.isAfter(availableTo)) {
                slots.add(current);
                current = current.plusMinutes(30); // 30-minute slots
            }
        }
        return slots;
    }

    /**
     * Checks if this resource is available at the given time for the specified duration.
     */
    public boolean isAvailableAt(LocalDateTime startTime, Duration duration) {
        if (!isActive) {
            return false;
        }

        LocalDateTime endTime = startTime.plus(duration);
        
        // Check if within availability window
        if (availableFrom != null && startTime.isBefore(availableFrom)) {
            return false;
        }
        if (availableTo != null && endTime.isAfter(availableTo)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if this resource has all required capabilities for an appointment.
     */
    public boolean hasRequiredCapabilities(Set<String> requiredCaps) {
        if (requiredCaps == null || requiredCaps.isEmpty()) {
            return true;
        }
        return capabilities.containsAll(requiredCaps);
    }

    /**
     * Calculates the total cost for using this resource for the given duration.
     */
    public double calculateCost(Duration duration) {
        double hours = duration.toMinutes() / 60.0;
        return costPerHour * hours;
    }

    /**
     * Checks if this resource conflicts with another resource.
     */
    public boolean conflictsWith(Resource other) {
        if (other == null || this.id.equals(other.id)) {
            return false;
        }
        return this.conflicts.contains(other.id) || other.conflicts.contains(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isActive=" + isActive +
                '}';
    }
}

