package com.vikk.appointmentscheduler.model;

/**
 * Types of resources that can be scheduled.
 */
public enum ResourceType {
    ROOM("Room", "Physical space for appointments"),
    EQUIPMENT("Equipment", "Medical or technical equipment"),
    STAFF("Staff", "Human resources - doctors, nurses, technicians"),
    VEHICLE("Vehicle", "Transportation resources"),
    VIRTUAL("Virtual", "Online meeting spaces or virtual resources");

    private final String displayName;
    private final String description;

    ResourceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
