package com.vikk.appointmentscheduler.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.Schedule;

/**
 * Simulated Annealing algorithm for appointment scheduling optimization.
 * Uses probabilistic acceptance of worse solutions to escape local optima.
 * 
 * Time Complexity: O(iterations * n * m) where n=appointments, m=resources
 * Space Complexity: O(n) for current solution storage
 */
public class SimulatedAnnealingAlgorithm implements SchedulingAlgorithm {
    
    private static final int DEFAULT_ITERATIONS = 10000;
    private static final double INITIAL_TEMPERATURE = 1000.0;
    private static final double COOLING_RATE = 0.95;
    private static final double MIN_TEMPERATURE = 0.1;
    
    private final List<Appointment> appointments;
    private final List<Resource> resources;
    private final int maxIterations;
    private long startTime;
    private int currentIteration;
    private double currentTemperature;
    private double bestEnergy;
    
    public SimulatedAnnealingAlgorithm(List<Appointment> appointments, List<Resource> resources) {
        this(appointments, resources, DEFAULT_ITERATIONS);
    }
    
    public SimulatedAnnealingAlgorithm(List<Appointment> appointments, List<Resource> resources, 
                                     int maxIterations) {
        this.appointments = new ArrayList<>(appointments);
        this.resources = new ArrayList<>(resources);
        this.maxIterations = maxIterations;
        this.currentIteration = 0;
        this.currentTemperature = INITIAL_TEMPERATURE;
        this.bestEnergy = Double.POSITIVE_INFINITY;
    }

    @Override
    public Schedule optimize() {
        startTime = System.currentTimeMillis();
        currentIteration = 0;
        currentTemperature = INITIAL_TEMPERATURE;
        bestEnergy = Double.POSITIVE_INFINITY;
        
        // Initialize with a random solution
        Map<String, String> currentSolution = generateRandomSolution();
        Map<String, String> bestSolution = new HashMap<>(currentSolution);
        double currentEnergy = calculateEnergy(currentSolution);
        bestEnergy = currentEnergy;
        
        // Simulated annealing loop
        while (currentTemperature > MIN_TEMPERATURE && currentIteration < maxIterations) {
            // Generate neighbor solution
            Map<String, String> neighborSolution = generateNeighbor(currentSolution);
            double neighborEnergy = calculateEnergy(neighborSolution);
            
            // Calculate energy difference (lower is better)
            double deltaEnergy = neighborEnergy - currentEnergy;
            
            // Accept or reject the neighbor
            if (deltaEnergy < 0 || shouldAcceptWorseSolution(deltaEnergy, currentTemperature)) {
                currentSolution = neighborSolution;
                currentEnergy = neighborEnergy;
                
                // Update best solution if improved
                if (currentEnergy < bestEnergy) {
                    bestSolution = new HashMap<>(currentSolution);
                    bestEnergy = currentEnergy;
                }
            }
            
            // Cool down
            currentTemperature *= COOLING_RATE;
            currentIteration++;
        }
        
        // Convert best solution to schedule
        return convertToSchedule(bestSolution);
    }

    /**
     * Generates a random initial solution.
     * Time Complexity: O(n * m) where n=appointments, m=resources
     */
    private Map<String, String> generateRandomSolution() {
        Map<String, String> solution = new HashMap<>();
        
        for (Appointment appointment : appointments) {
            List<Resource> validResources = getValidResources(appointment);
            if (!validResources.isEmpty()) {
                Resource randomResource = validResources.get(
                    ThreadLocalRandom.current().nextInt(validResources.size()));
                solution.put(appointment.getId(), randomResource.getId());
            }
        }
        
        return solution;
    }

    /**
     * Generates a neighbor solution by making a small change.
     * Time Complexity: O(n * m) where n=appointments, m=resources
     */
    private Map<String, String> neighborSolution(Map<String, String> currentSolution) {
        Map<String, String> neighbor = new HashMap<>(currentSolution);
        
        // Choose a random appointment to reassign
        if (!appointments.isEmpty()) {
            Appointment randomAppointment = appointments.get(
                ThreadLocalRandom.current().nextInt(appointments.size()));
            
            List<Resource> validResources = getValidResources(randomAppointment);
            if (!validResources.isEmpty()) {
                Resource newResource = validResources.get(
                    ThreadLocalRandom.current().nextInt(validResources.size()));
                neighbor.put(randomAppointment.getId(), newResource.getId());
            }
        }
        
        return neighbor;
    }

    /**
     * Generates a neighbor solution using multiple strategies.
     * Time Complexity: O(n * m) where n=appointments, m=resources
     */
    private Map<String, String> generateNeighbor(Map<String, String> currentSolution) {
        Map<String, String> neighbor = new HashMap<>(currentSolution);
        
        // Choose mutation strategy
        int strategy = ThreadLocalRandom.current().nextInt(4);
        
        switch (strategy) {
            case 0: // Single reassignment
                reassignSingleAppointment(neighbor);
                break;
            case 1: // Swap two appointments
                swapAppointments(neighbor);
                break;
            case 2: // Reassign multiple appointments
                reassignMultipleAppointments(neighbor);
                break;
            case 3: // Time shift
                timeShiftAppointment(neighbor);
                break;
        }
        
        return neighbor;
    }

    /**
     * Reassigns a single appointment to a different resource.
     * Time Complexity: O(m) where m=resources
     */
    private void reassignSingleAppointment(Map<String, String> solution) {
        if (appointments.isEmpty()) return;
        
        Appointment appointment = appointments.get(
            ThreadLocalRandom.current().nextInt(appointments.size()));
        List<Resource> validResources = getValidResources(appointment);
        
        if (validResources.size() > 1) {
            Resource newResource = validResources.get(
                ThreadLocalRandom.current().nextInt(validResources.size()));
            solution.put(appointment.getId(), newResource.getId());
        }
    }

    /**
     * Swaps resources between two appointments.
     * Time Complexity: O(1)
     */
    private void swapAppointments(Map<String, String> solution) {
        if (appointments.size() < 2) return;
        
        int index1 = ThreadLocalRandom.current().nextInt(appointments.size());
        int index2 = ThreadLocalRandom.current().nextInt(appointments.size());
        
        if (index1 != index2) {
            Appointment apt1 = appointments.get(index1);
            Appointment apt2 = appointments.get(index2);
            
            String resource1 = solution.get(apt1.getId());
            String resource2 = solution.get(apt2.getId());
            
            if (resource1 != null && resource2 != null) {
                solution.put(apt1.getId(), resource2);
                solution.put(apt2.getId(), resource1);
            }
        }
    }

    /**
     * Reassigns multiple appointments.
     * Time Complexity: O(k * m) where k=number of reassignments, m=resources
     */
    private void reassignMultipleAppointments(Map<String, String> solution) {
        int numReassignments = Math.min(3, appointments.size());
        
        for (int i = 0; i < numReassignments; i++) {
            Appointment appointment = appointments.get(
                ThreadLocalRandom.current().nextInt(appointments.size()));
            List<Resource> validResources = getValidResources(appointment);
            
            if (!validResources.isEmpty()) {
                Resource newResource = validResources.get(
                    ThreadLocalRandom.current().nextInt(validResources.size()));
                solution.put(appointment.getId(), newResource.getId());
            }
        }
    }

    /**
     * Attempts to shift an appointment's time within its flexibility window.
     * Time Complexity: O(1)
     */
    private void timeShiftAppointment(Map<String, String> solution) {
        // Find a flexible appointment
        List<Appointment> flexibleAppointments = appointments.stream()
                .filter(Appointment::isFlexible)
                .collect(Collectors.toList());
        
        if (!flexibleAppointments.isEmpty()) {
            Appointment appointment = flexibleAppointments.get(
                ThreadLocalRandom.current().nextInt(flexibleAppointments.size()));
            
            // This is a simplified version - in practice, you'd need to update the appointment's time
            // and check for new conflicts
            reassignSingleAppointment(solution);
        }
    }

    /**
     * Calculates the energy (cost) of a solution.
     * Lower energy = better solution.
     * Time Complexity: O(n * m) where n=appointments, m=resources
     */
    private double calculateEnergy(Map<String, String> solution) {
        double energy = 0.0;
        int conflicts = 0;
        int unassigned = 0;
        
        // Calculate assignment costs and conflicts
        for (Appointment appointment : appointments) {
            String resourceId = solution.get(appointment.getId());
            if (resourceId != null) {
                Resource resource = findResourceById(resourceId);
                if (resource != null) {
                    energy += resource.calculateCost(appointment.getDuration());
                    
                    // Count conflicts
                    conflicts += countConflicts(appointment, resourceId, solution);
                } else {
                    unassigned++;
                }
            } else {
                unassigned++;
            }
        }
        
        // Add penalty for conflicts and unassigned appointments
        energy += conflicts * 100.0; // High penalty for conflicts
        energy += unassigned * 200.0; // Very high penalty for unassigned
        
        return energy;
    }

    /**
     * Counts conflicts for a specific appointment.
     * Time Complexity: O(n) where n=appointments
     */
    private int countConflicts(Appointment appointment, String resourceId, 
                              Map<String, String> solution) {
        int conflicts = 0;
        
        for (Appointment other : appointments) {
            if (!appointment.getId().equals(other.getId())) {
                String otherResourceId = solution.get(other.getId());
                if (resourceId.equals(otherResourceId) && appointment.conflictsWith(other)) {
                    conflicts++;
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Determines whether to accept a worse solution based on temperature and energy difference.
     * Time Complexity: O(1)
     */
    private boolean shouldAcceptWorseSolution(double deltaEnergy, double temperature) {
        if (temperature <= 0) return false;
        
        double probability = Math.exp(-deltaEnergy / temperature);
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    /**
     * Gets valid resources for an appointment.
     * Time Complexity: O(m) where m=resources
     */
    private List<Resource> getValidResources(Appointment appointment) {
        return resources.stream()
                .filter(Resource::isActive)
                .filter(resource -> resource.hasRequiredCapabilities(appointment.getRequiredCapabilities()))
                .filter(resource -> resource.isAvailableAt(appointment.getStartTime(), appointment.getDuration()))
                .collect(Collectors.toList());
    }

    /**
     * Converts a solution to a Schedule object.
     * Time Complexity: O(n) where n=appointments
     */
    private Schedule convertToSchedule(Map<String, String> solution) {
        Schedule schedule = new Schedule("SA_" + System.currentTimeMillis(),
                                       getEarliestStartTime(), getLatestEndTime());
        
        for (Appointment appointment : appointments) {
            String resourceId = solution.get(appointment.getId());
            schedule.addAppointment(appointment, resourceId);
        }
        
        calculateScheduleMetrics(schedule);
        return schedule;
    }

    /**
     * Calculates comprehensive schedule metrics.
     * Time Complexity: O(n * m) where n=appointments, m=resources
     */
    private void calculateScheduleMetrics(Schedule schedule) {
        double totalCost = 0.0;
        double totalScore = 0.0;
        int conflictCount = 0;
        
        for (Appointment appointment : schedule.getAppointments()) {
            String resourceId = schedule.getResourceForAppointment(appointment.getId());
            if (resourceId != null) {
                Resource resource = findResourceById(resourceId);
                if (resource != null) {
                    totalCost += resource.calculateCost(appointment.getDuration());
                }
            }
            
            totalScore += appointment.calculateScore();
            
            // Count conflicts
            for (Appointment other : schedule.getAppointments()) {
                if (!appointment.getId().equals(other.getId()) && 
                    appointment.conflictsWith(other)) {
                    conflictCount++;
                }
            }
        }
        
        schedule.setTotalCost(totalCost);
        schedule.setTotalScore(totalScore);
        schedule.setConflictCount(conflictCount / 2); // Each conflict counted twice
        
        // Calculate efficiency score
        double efficiencyScore = calculateEfficiencyScore(schedule);
        schedule.getMetrics().setEfficiencyScore(efficiencyScore);
    }

    /**
     * Calculates the overall efficiency score of the schedule.
     * Time Complexity: O(n) where n=appointments
     */
    private double calculateEfficiencyScore(Schedule schedule) {
        double utilizationRate = schedule.calculateUtilizationRate();
        double conflictPenalty = Math.max(0, 1.0 - (schedule.getConflictCount() * 0.1));
        double assignmentRate = schedule.getAppointments().size() > 0 ? 
            (double) (schedule.getAppointments().size() - schedule.getUnassignedAppointments().size()) / 
            schedule.getAppointments().size() : 0.0;
        
        return (utilizationRate * 0.4 + conflictPenalty * 0.4 + assignmentRate * 0.2) * 100;
    }

    // Helper methods
    private Resource findResourceById(String id) {
        return resources.stream()
                .filter(res -> res.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime getEarliestStartTime() {
        return appointments.stream()
                .map(Appointment::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    private LocalDateTime getLatestEndTime() {
        return appointments.stream()
                .map(Appointment::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().plusDays(1));
    }

    @Override
    public String getAlgorithmName() {
        return "Simulated Annealing";
    }

    @Override
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public int getBacktrackCount() {
        return currentIteration;
    }

    /**
     * Gets the current temperature of the algorithm.
     */
    public double getCurrentTemperature() {
        return currentTemperature;
    }

    /**
     * Gets the best energy found so far.
     */
    public double getBestEnergy() {
        return bestEnergy;
    }
}

