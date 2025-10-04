package com.vikk.appointmentscheduler.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
 * Genetic Algorithm for appointment scheduling optimization.
 * Uses evolutionary approach with selection, crossover, and mutation operations.
 * 
 * Time Complexity: O(g * p * n * m) where g=generations, p=population, n=appointments, m=resources
 * Space Complexity: O(p * n) for population storage
 */
public class GeneticAlgorithm implements SchedulingAlgorithm {
    
    private static final int DEFAULT_POPULATION_SIZE = 100;
    private static final int DEFAULT_GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.8;
    private static final double ELITE_PERCENTAGE = 0.1;
    
    private final List<Appointment> appointments;
    private final List<Resource> resources;
    private final int populationSize;
    private final int maxGenerations;
    private long startTime;
    private int currentGeneration;
    private double bestFitness;
    
    public GeneticAlgorithm(List<Appointment> appointments, List<Resource> resources) {
        this(appointments, resources, DEFAULT_POPULATION_SIZE, DEFAULT_GENERATIONS);
    }
    
    public GeneticAlgorithm(List<Appointment> appointments, List<Resource> resources, 
                           int populationSize, int maxGenerations) {
        this.appointments = new ArrayList<>(appointments);
        this.resources = new ArrayList<>(resources);
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.currentGeneration = 0;
        this.bestFitness = Double.NEGATIVE_INFINITY;
    }

    @Override
    public Schedule optimize() {
        startTime = System.currentTimeMillis();
        currentGeneration = 0;
        bestFitness = Double.NEGATIVE_INFINITY;
        
        // Initialize population
        List<ScheduleChromosome> population = initializePopulation();
        
        // Evolution loop
        for (currentGeneration = 0; currentGeneration < maxGenerations; currentGeneration++) {
            // Evaluate fitness
            evaluatePopulation(population);
            
            // Check for convergence
            if (hasConverged(population)) {
                break;
            }
            
            // Create new generation
            List<ScheduleChromosome> newPopulation = new ArrayList<>();
            
            // Elitism - keep best individuals
            int eliteCount = (int) (populationSize * ELITE_PERCENTAGE);
            population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
            for (int i = 0; i < eliteCount; i++) {
                newPopulation.add(population.get(i).copy());
            }
            
            // Generate offspring
            while (newPopulation.size() < populationSize) {
                ScheduleChromosome parent1 = tournamentSelection(population);
                ScheduleChromosome parent2 = tournamentSelection(population);
                
                if (ThreadLocalRandom.current().nextDouble() < CROSSOVER_RATE) {
                    ScheduleChromosome[] offspring = crossover(parent1, parent2);
                    newPopulation.add(offspring[0]);
                    if (newPopulation.size() < populationSize) {
                        newPopulation.add(offspring[1]);
                    }
                } else {
                    newPopulation.add(parent1.copy());
                    if (newPopulation.size() < populationSize) {
                        newPopulation.add(parent2.copy());
                    }
                }
            }
            
            // Mutation
            for (int i = eliteCount; i < newPopulation.size(); i++) {
                if (ThreadLocalRandom.current().nextDouble() < MUTATION_RATE) {
                    mutate(newPopulation.get(i));
                }
            }
            
            population = newPopulation;
        }
        
        // Return best solution
        population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        ScheduleChromosome best = population.get(0);
        return convertToSchedule(best);
    }

    /**
     * Initializes the population with random schedules.
     * Time Complexity: O(p * n * m) where p=population, n=appointments, m=resources
     */
    private List<ScheduleChromosome> initializePopulation() {
        List<ScheduleChromosome> population = new ArrayList<>();
        
        for (int i = 0; i < populationSize; i++) {
            ScheduleChromosome chromosome = new ScheduleChromosome();
            
            // Random assignment of resources to appointments
            for (Appointment appointment : appointments) {
                List<Resource> validResources = getValidResources(appointment);
                if (!validResources.isEmpty()) {
                    Resource randomResource = validResources.get(
                        ThreadLocalRandom.current().nextInt(validResources.size()));
                    chromosome.addAssignment(appointment.getId(), randomResource.getId());
                }
            }
            
            population.add(chromosome);
        }
        
        return population;
    }

    /**
     * Evaluates fitness for all individuals in the population.
     * Time Complexity: O(p * n * m) where p=population, n=appointments, m=resources
     */
    private void evaluatePopulation(List<ScheduleChromosome> population) {
        for (ScheduleChromosome chromosome : population) {
            double fitness = calculateFitness(chromosome);
            chromosome.setFitness(fitness);
            
            if (fitness > bestFitness) {
                bestFitness = fitness;
            }
        }
    }

    /**
     * Calculates fitness score for a chromosome.
     * Higher fitness = better solution.
     * Time Complexity: O(n * m) where n=appointments, m=resources
     */
    private double calculateFitness(ScheduleChromosome chromosome) {
        double score = 0.0;
        double cost = 0.0;
        int conflicts = 0;
        int assignments = 0;
        
        // Calculate individual appointment scores
        for (Appointment appointment : appointments) {
            String resourceId = chromosome.getAssignment(appointment.getId());
            if (resourceId != null) {
                Resource resource = findResourceById(resourceId);
                if (resource != null) {
                    score += appointment.calculateScore();
                    cost += resource.calculateCost(appointment.getDuration());
                    assignments++;
                    
                    // Check for conflicts
                    conflicts += countConflicts(appointment, resourceId, chromosome);
                }
            }
        }
        
        // Calculate fitness components
        double assignmentRate = appointments.size() > 0 ? 
            (double) assignments / appointments.size() : 0.0;
        double conflictPenalty = Math.max(0, 1.0 - (conflicts * 0.1));
        double costEfficiency = cost > 0 ? score / cost : score;
        
        // Weighted fitness function
        return (assignmentRate * 0.3 + conflictPenalty * 0.4 + costEfficiency * 0.3) * 100;
    }

    /**
     * Counts conflicts for a specific appointment.
     * Time Complexity: O(n) where n=appointments
     */
    private int countConflicts(Appointment appointment, String resourceId, 
                              ScheduleChromosome chromosome) {
        int conflicts = 0;
        
        for (Appointment other : appointments) {
            if (!appointment.getId().equals(other.getId())) {
                String otherResourceId = chromosome.getAssignment(other.getId());
                if (resourceId.equals(otherResourceId) && appointment.conflictsWith(other)) {
                    conflicts++;
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Tournament selection for parent selection.
     * Time Complexity: O(1) - constant tournament size
     */
    private ScheduleChromosome tournamentSelection(List<ScheduleChromosome> population) {
        int tournamentSize = Math.min(5, population.size());
        List<ScheduleChromosome> tournament = new ArrayList<>();
        
        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }
        
        return tournament.stream()
                .max(Comparator.comparing(ScheduleChromosome::getFitness))
                .orElse(population.get(0));
    }

    /**
     * Crossover operation between two parent chromosomes.
     * Time Complexity: O(n) where n=appointments
     */
    private ScheduleChromosome[] crossover(ScheduleChromosome parent1, ScheduleChromosome parent2) {
        ScheduleChromosome offspring1 = new ScheduleChromosome();
        ScheduleChromosome offspring2 = new ScheduleChromosome();
        
        // Single-point crossover
        int crossoverPoint = ThreadLocalRandom.current().nextInt(appointments.size());
        
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            String appointmentId = appointment.getId();
            
            if (i < crossoverPoint) {
                offspring1.addAssignment(appointmentId, parent1.getAssignment(appointmentId));
                offspring2.addAssignment(appointmentId, parent2.getAssignment(appointmentId));
            } else {
                offspring1.addAssignment(appointmentId, parent2.getAssignment(appointmentId));
                offspring2.addAssignment(appointmentId, parent1.getAssignment(appointmentId));
            }
        }
        
        return new ScheduleChromosome[]{offspring1, offspring2};
    }

    /**
     * Mutation operation on a chromosome.
     * Time Complexity: O(n * m) where n=appointments, m=resources
     */
    private void mutate(ScheduleChromosome chromosome) {
        // Randomly reassign some appointments
        for (Appointment appointment : appointments) {
            if (ThreadLocalRandom.current().nextDouble() < 0.1) { // 10% mutation chance per appointment
                List<Resource> validResources = getValidResources(appointment);
                if (!validResources.isEmpty()) {
                    Resource newResource = validResources.get(
                        ThreadLocalRandom.current().nextInt(validResources.size()));
                    chromosome.addAssignment(appointment.getId(), newResource.getId());
                }
            }
        }
    }

    /**
     * Checks if the population has converged.
     * Time Complexity: O(p) where p=population
     */
    private boolean hasConverged(List<ScheduleChromosome> population) {
        if (population.size() < 2) return true;
        
        double bestFitness = population.stream()
                .mapToDouble(ScheduleChromosome::getFitness)
                .max()
                .orElse(0.0);
        
        double averageFitness = population.stream()
                .mapToDouble(ScheduleChromosome::getFitness)
                .average()
                .orElse(0.0);
        
        // Convergence if best and average are close
        return Math.abs(bestFitness - averageFitness) < 0.01;
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
     * Converts a chromosome to a Schedule object.
     * Time Complexity: O(n) where n=appointments
     */
    private Schedule convertToSchedule(ScheduleChromosome chromosome) {
        Schedule schedule = new Schedule("GA_" + System.currentTimeMillis(),
                                       getEarliestStartTime(), getLatestEndTime());
        
        for (Appointment appointment : appointments) {
            String resourceId = chromosome.getAssignment(appointment.getId());
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
        return "Genetic Algorithm";
    }

    @Override
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public int getBacktrackCount() {
        return currentGeneration;
    }

    /**
     * Chromosome representation for the genetic algorithm.
     */
    private static class ScheduleChromosome {
        private final Map<String, String> assignments; // appointmentId -> resourceId
        private double fitness;

        public ScheduleChromosome() {
            this.assignments = new HashMap<>();
            this.fitness = 0.0;
        }

        public void addAssignment(String appointmentId, String resourceId) {
            assignments.put(appointmentId, resourceId);
        }

        public String getAssignment(String appointmentId) {
            return assignments.get(appointmentId);
        }

        public double getFitness() {
            return fitness;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        public ScheduleChromosome copy() {
            ScheduleChromosome copy = new ScheduleChromosome();
            copy.assignments.putAll(this.assignments);
            copy.fitness = this.fitness;
            return copy;
        }
    }
}

