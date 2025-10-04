package com.vikk.appointmentscheduler.exception;

/**
 * Base exception for appointment scheduling operations.
 */
public class SchedulingException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public SchedulingException(String message) {
        super(message);
    }
    
    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}

