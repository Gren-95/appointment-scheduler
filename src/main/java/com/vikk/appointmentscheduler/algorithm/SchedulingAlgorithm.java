package com.vikk.appointmentscheduler.algorithm;

import com.vikk.appointmentscheduler.model.Schedule;

/**
 * Interface for appointment scheduling optimization algorithms.
 * All algorithms must implement this interface to ensure consistent behavior.
 */
public interface SchedulingAlgorithm {
    
    /**
     * Optimizes the appointment schedule using the specific algorithm.
     * 
     * @return optimized Schedule object
     */
    Schedule optimize();
    
    /**
     * Gets the name of the algorithm.
     * 
     * @return algorithm name
     */
    String getAlgorithmName();
    
    /**
     * Gets the execution time of the algorithm in milliseconds.
     * 
     * @return execution time in milliseconds
     */
    long getExecutionTime();
    
    /**
     * Gets the number of iterations/backtracks performed by the algorithm.
     * 
     * @return number of iterations
     */
    int getBacktrackCount();
}

