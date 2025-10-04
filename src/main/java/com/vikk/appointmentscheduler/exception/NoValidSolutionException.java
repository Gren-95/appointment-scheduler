package com.vikk.appointmentscheduler.exception;

/**
 * Exception thrown when no valid schedule can be found.
 */
public class NoValidSolutionException extends SchedulingException {
    
    public NoValidSolutionException(String message) {
        super(message);
    }
    
    public NoValidSolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

