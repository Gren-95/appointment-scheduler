package com.vikk.appointmentscheduler.model;

/**
 * Status of an appointment in the scheduling system.
 */
public enum AppointmentStatus {
    PENDING("Pending"),
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    UNSCHEDULED("Unscheduled");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
