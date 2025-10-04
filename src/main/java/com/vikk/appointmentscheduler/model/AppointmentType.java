package com.vikk.appointmentscheduler.model;

/**
 * Enumeration of different appointment types with their characteristics.
 */
public enum AppointmentType {
    CONSULTATION("Consultation", 30, 1.0),
    FOLLOW_UP("Follow-up", 15, 0.8),
    TREATMENT("Treatment", 60, 1.5),
    EMERGENCY("Emergency", 45, 3.0),
    SURGERY("Surgery", 120, 2.5),
    DIAGNOSTIC("Diagnostic", 45, 1.2),
    THERAPY("Therapy", 50, 1.1),
    VACCINATION("Vaccination", 20, 0.9);

    private final String displayName;
    private final int defaultDurationMinutes;
    private final double complexityFactor;

    AppointmentType(String displayName, int defaultDurationMinutes, double complexityFactor) {
        this.displayName = displayName;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.complexityFactor = complexityFactor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    public double getComplexityFactor() {
        return complexityFactor;
    }
}

