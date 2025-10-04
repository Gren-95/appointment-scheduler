package com.vikk.appointmentscheduler.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScheduleMetrics.
 */
class ScheduleMetricsTest {

    private ScheduleMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new ScheduleMetrics();
    }

    @Test
    @DisplayName("Test default initialization")
    void testDefaultInitialization() {
        assertEquals(0, metrics.getTotalAppointments());
        assertEquals(0, metrics.getAssignedAppointments());
        assertEquals(0, metrics.getUnassignedAppointments());
        assertEquals(0.0, metrics.getTotalCost());
        assertEquals(0.0, metrics.getAverageCostPerAppointment());
        assertEquals(0, metrics.getConflictCount());
        assertEquals(0.0, metrics.getUtilizationRate());
        assertEquals(0.0, metrics.getEfficiencyScore());
    }

    @Test
    @DisplayName("Test setters and getters")
    void testSettersAndGetters() {
        // Test total appointments
        metrics.setTotalAppointments(10);
        assertEquals(10, metrics.getTotalAppointments());
        
        // Test assigned appointments
        metrics.setAssignedAppointments(8);
        assertEquals(8, metrics.getAssignedAppointments());
        
        // Test unassigned appointments
        metrics.setUnassignedAppointments(2);
        assertEquals(2, metrics.getUnassignedAppointments());
        
        // Test total cost
        metrics.setTotalCost(500.0);
        assertEquals(500.0, metrics.getTotalCost());
        
        // Test average cost per appointment
        metrics.setAverageCostPerAppointment(62.5);
        assertEquals(62.5, metrics.getAverageCostPerAppointment());
        
        // Test conflict count
        metrics.setConflictCount(3);
        assertEquals(3, metrics.getConflictCount());
        
        // Test utilization rate
        metrics.setUtilizationRate(0.8);
        assertEquals(0.8, metrics.getUtilizationRate());
        
        // Test efficiency score
        metrics.setEfficiencyScore(85.0);
        assertEquals(85.0, metrics.getEfficiencyScore());
    }

    @Test
    @DisplayName("Test calculation methods")
    void testCalculationMethods() {
        // Set up metrics
        metrics.setTotalAppointments(10);
        metrics.setAssignedAppointments(8);
        metrics.setUnassignedAppointments(2);
        metrics.setTotalCost(400.0);
        metrics.setConflictCount(1);
        
        // Test assignment rate calculation
        double assignmentRate = metrics.getAssignmentRate();
        assertEquals(80.0, assignmentRate, 0.001, "Assignment rate should be 80.0");
        
        // Test conflict rate calculation
        double conflictRate = metrics.getConflictRate();
        assertEquals(10.0, conflictRate, 0.001, "Conflict rate should be 10.0");
        
        // Test average cost per appointment calculation
        metrics.setAverageCostPerAppointment(40.0);
        assertEquals(40.0, metrics.getAverageCostPerAppointment(), 0.001, "Average cost per appointment should be 40.0");
    }

    @Test
    @DisplayName("Test calculation with zero values")
    void testCalculationWithZeroValues() {
        // Test assignment rate with zero total appointments
        double assignmentRate = metrics.getAssignmentRate();
        assertEquals(0.0, assignmentRate, "Assignment rate should be 0.0 when total appointments is 0");
        
        // Test conflict rate with zero total appointments
        double conflictRate = metrics.getConflictRate();
        assertEquals(0.0, conflictRate, "Conflict rate should be 0.0 when total appointments is 0");
    }

    @Test
    @DisplayName("Test calculation with zero assigned appointments")
    void testCalculationWithZeroAssignedAppointments() {
        metrics.setTotalAppointments(5);
        metrics.setAssignedAppointments(0);
        metrics.setUnassignedAppointments(5);
        
        double assignmentRate = metrics.getAssignmentRate();
        assertEquals(0.0, assignmentRate, "Assignment rate should be 0.0 when no appointments are assigned");
    }

    @Test
    @DisplayName("Test calculation with all appointments assigned")
    void testCalculationWithAllAppointmentsAssigned() {
        metrics.setTotalAppointments(5);
        metrics.setAssignedAppointments(5);
        metrics.setUnassignedAppointments(0);
        
        double assignmentRate = metrics.getAssignmentRate();
        assertEquals(100.0, assignmentRate, "Assignment rate should be 100.0 when all appointments are assigned");
    }

    @Test
    @DisplayName("Test calculation with high conflict count")
    void testCalculationWithHighConflictCount() {
        metrics.setTotalAppointments(10);
        metrics.setConflictCount(5);
        
        double conflictRate = metrics.getConflictRate();
        assertEquals(50.0, conflictRate, "Conflict rate should be 50.0 when half the appointments have conflicts");
    }

    @Test
    @DisplayName("Test calculation with high cost")
    void testCalculationWithHighCost() {
        metrics.setTotalAppointments(4);
        metrics.setTotalCost(1000.0);
        metrics.setAverageCostPerAppointment(250.0);
        
        assertEquals(250.0, metrics.getAverageCostPerAppointment(), "Average cost per appointment should be 250.0");
    }

    @Test
    @DisplayName("Test equals and hashCode")
    void testEqualsAndHashCode() {
        ScheduleMetrics metrics1 = new ScheduleMetrics();
        metrics1.setTotalAppointments(5);
        metrics1.setAssignedAppointments(4);
        metrics1.setTotalCost(200.0);
        
        ScheduleMetrics metrics2 = new ScheduleMetrics();
        metrics2.setTotalAppointments(5);
        metrics2.setAssignedAppointments(4);
        metrics2.setTotalCost(200.0);
        
        ScheduleMetrics metrics3 = new ScheduleMetrics();
        metrics3.setTotalAppointments(6);
        metrics3.setAssignedAppointments(4);
        metrics3.setTotalCost(200.0);
        
        assertEquals(metrics1, metrics2, "Metrics with same values should be equal");
        assertNotEquals(metrics1, metrics3, "Metrics with different values should not be equal");
        assertEquals(metrics1.hashCode(), metrics2.hashCode(), "Equal metrics should have same hashCode");
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        metrics.setTotalAppointments(5);
        metrics.setAssignedAppointments(4);
        metrics.setTotalCost(200.0);
        
        String metricsString = metrics.toString();
        assertNotNull(metricsString, "toString should not be null");
        assertTrue(metricsString.contains("5"), "toString should contain total appointments");
        assertTrue(metricsString.contains("4"), "toString should contain assigned appointments");
        assertTrue(metricsString.contains("200.0"), "toString should contain total cost");
    }

    @Test
    @DisplayName("Test resource utilization methods")
    void testResourceUtilizationMethods() {
        // Set up resource utilization
        metrics.getResourceUtilization().put("RES001", 5);
        metrics.getResourceUtilization().put("RES002", 3);
        metrics.getResourceUtilization().put("RES003", 8);
        
        // Test most utilized resource
        String mostUtilized = metrics.getMostUtilizedResource();
        assertEquals("RES003", mostUtilized, "Most utilized resource should be RES003");
        
        // Test least utilized resource
        String leastUtilized = metrics.getLeastUtilizedResource();
        assertEquals("RES002", leastUtilized, "Least utilized resource should be RES002");
    }

    @Test
    @DisplayName("Test edge case values")
    void testEdgeCaseValues() {
        // Test with maximum values
        metrics.setTotalAppointments(Integer.MAX_VALUE);
        metrics.setAssignedAppointments(Integer.MAX_VALUE);
        metrics.setUnassignedAppointments(0);
        metrics.setTotalCost(Double.MAX_VALUE);
        metrics.setConflictCount(Integer.MAX_VALUE);
        
        assertEquals(Integer.MAX_VALUE, metrics.getTotalAppointments());
        assertEquals(Integer.MAX_VALUE, metrics.getAssignedAppointments());
        assertEquals(0, metrics.getUnassignedAppointments());
        assertEquals(Double.MAX_VALUE, metrics.getTotalCost());
        assertEquals(Integer.MAX_VALUE, metrics.getConflictCount());
    }

    @Test
    @DisplayName("Test negative values")
    void testNegativeValues() {
        // Test with negative values
        metrics.setTotalAppointments(-1);
        metrics.setAssignedAppointments(-2);
        metrics.setUnassignedAppointments(-3);
        metrics.setTotalCost(-100.0);
        metrics.setConflictCount(-1);
        
        assertEquals(-1, metrics.getTotalAppointments());
        assertEquals(-2, metrics.getAssignedAppointments());
        assertEquals(-3, metrics.getUnassignedAppointments());
        assertEquals(-100.0, metrics.getTotalCost());
        assertEquals(-1, metrics.getConflictCount());
    }
}
