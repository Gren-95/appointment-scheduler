package com.vikk.appointmentscheduler.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Comprehensive tests for MathUtils class.
 * Tests all mathematical and logical functions as required by the assignment.
 */
@DisplayName("MathUtils Tests")
class MathUtilsTest {

    @Test
    @DisplayName("Test min/max functions with collections")
    void testMinMaxCollections() {
        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3);
        
        assertEquals(Integer.valueOf(1), MathUtils.<Integer>min(numbers));
        assertEquals(Integer.valueOf(9), MathUtils.<Integer>max(numbers));
        
        // Test with empty collection
        assertNull(MathUtils.<Integer>min(new ArrayList<>()));
        assertNull(MathUtils.<Integer>max(new ArrayList<>()));
    }

    @Test
    @DisplayName("Test min/max functions with varargs")
    void testMinMaxVarargs() {
        assertEquals(1.0, MathUtils.minValues(5.0, 2.0, 8.0, 1.0, 9.0, 3.0));
        assertEquals(9.0, MathUtils.maxValues(5.0, 2.0, 8.0, 1.0, 9.0, 3.0));
        
        // Test with single value
        assertEquals(5.0, MathUtils.minValues(5.0));
        assertEquals(5.0, MathUtils.maxValues(5.0));
    }

    @Test
    @DisplayName("Test abs function")
    void testAbs() {
        assertEquals(5.0, MathUtils.abs(5.0));
        assertEquals(5.0, MathUtils.abs(-5.0));
        assertEquals(0.0, MathUtils.abs(0.0));
        
        assertEquals(5, MathUtils.abs(5));
        assertEquals(5, MathUtils.abs(-5));
        assertEquals(0, MathUtils.abs(0));
    }

    @Test
    @DisplayName("Test rounding functions")
    void testRounding() {
        assertEquals(3.14, MathUtils.round(3.14159, 2));
        assertEquals(3.1, MathUtils.round(3.14159, 1));
        assertEquals(3.0, MathUtils.round(3.14159, 0));
        
        assertEquals(10.0, MathUtils.roundToNearest(12.0, 5.0));
        assertEquals(15.0, MathUtils.roundToNearest(13.0, 5.0));
        assertEquals(0.0, MathUtils.roundToNearest(2.0, 5.0));
        
        assertEquals(60, MathUtils.roundToMinutes(Duration.ofMinutes(60).plusSeconds(30)));
        assertEquals(61, MathUtils.roundToMinutes(Duration.ofMinutes(61).plusSeconds(30)));
    }

    @Test
    @DisplayName("Test modulo function")
    void testModulo() {
        assertEquals(2, MathUtils.mod(7, 5));
        assertEquals(3, MathUtils.mod(-7, 5));
        assertEquals(0, MathUtils.mod(10, 5));
        
        assertEquals(2.5, MathUtils.mod(7.5, 5.0));
        assertEquals(2.5, MathUtils.mod(-7.5, 5.0));
        
        // Test division by zero
        assertThrows(IllegalArgumentException.class, () -> MathUtils.mod(5, 0));
        assertThrows(IllegalArgumentException.class, () -> MathUtils.mod(5.0, 0.0));
    }

    @Test
    @DisplayName("Test date/time calculations")
    void testDateTimeCalculations() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 30);
        
        Duration duration = MathUtils.calculateDuration(start, end);
        assertEquals(90, duration.toMinutes());
        
        // Test time window
        LocalDateTime windowStart = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime windowEnd = LocalDateTime.of(2024, 1, 1, 12, 0);
        assertTrue(MathUtils.isWithinTimeWindow(start, windowStart, windowEnd));
        assertFalse(MathUtils.isWithinTimeWindow(start.minusHours(2), windowStart, windowEnd));
        
        // Test add/subtract minutes
        LocalDateTime result = MathUtils.addMinutes(start, 30);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 30), result);
        
        result = MathUtils.subtractMinutes(start, 30);
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 30), result);
    }

    @Test
    @DisplayName("Test set operations")
    void testSetOperations() {
        Set<String> set1 = Set.of("a", "b", "c");
        Set<String> set2 = Set.of("b", "c", "d");
        Set<String> set3 = Set.of("x", "y", "z");
        
        // Test intersection
        assertTrue(MathUtils.hasIntersection(set1, set2));
        assertFalse(MathUtils.hasIntersection(set1, set3));
        
        // Test subset
        assertTrue(MathUtils.isSubset(Set.of("b", "c"), set1));
        assertFalse(MathUtils.isSubset(Set.of("b", "d"), set1));
        
        // Test intersection result
        List<String> intersection = MathUtils.intersection(set1, set2);
        assertEquals(2, intersection.size());
        assertTrue(intersection.contains("b"));
        assertTrue(intersection.contains("c"));
        
        // Test union
        List<String> union = MathUtils.union(set1, set2);
        assertEquals(4, union.size());
        assertTrue(union.containsAll(Set.of("a", "b", "c", "d")));
    }

    @Test
    @DisplayName("Test boolean logic functions")
    void testBooleanLogic() {
        assertTrue(MathUtils.allTrue(true, true, true));
        assertFalse(MathUtils.allTrue(true, false, true));
        assertTrue(MathUtils.allTrue()); // Empty array
        
        assertTrue(MathUtils.anyTrue(true, false, false));
        assertFalse(MathUtils.anyTrue(false, false, false));
        assertFalse(MathUtils.anyTrue()); // Empty array
        
        assertTrue(MathUtils.xor(true, false));
        assertTrue(MathUtils.xor(false, true));
        assertFalse(MathUtils.xor(true, true));
        assertFalse(MathUtils.xor(false, false));
    }

    @Test
    @DisplayName("Test statistical functions")
    void testStatisticalFunctions() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        
        assertEquals(3.0, MathUtils.mean(values));
        assertEquals(3.0, MathUtils.median(values));
        
        // Test with even number of values
        List<Double> evenValues = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        assertEquals(2.5, MathUtils.median(evenValues));
        
        // Test standard deviation
        double stdDev = MathUtils.standardDeviation(values);
        assertTrue(stdDev > 1.4 && stdDev < 1.6); // Approximate value
    }

    @Test
    @DisplayName("Test optimization helper functions")
    void testOptimizationHelpers() {
        // Test Euclidean distance
        double distance = MathUtils.calculateEuclideanDistance(0, 0, 3, 4);
        assertEquals(5.0, distance);
        
        // Test Manhattan distance
        double manhattanDistance = MathUtils.calculateManhattanDistance(0, 0, 3, 4);
        assertEquals(7.0, manhattanDistance);
        
        // Test normalization
        double normalized = MathUtils.normalize(5.0, 0.0, 10.0);
        assertEquals(0.5, normalized);
        
        // Test denormalization
        double denormalized = MathUtils.denormalize(0.5, 0.0, 10.0);
        assertEquals(5.0, denormalized);
    }

    @Test
    @DisplayName("Test constraint satisfaction helpers")
    void testConstraintSatisfactionHelpers() {
        assertTrue(MathUtils.isWithinBounds(5.0, 0.0, 10.0));
        assertFalse(MathUtils.isWithinBounds(15.0, 0.0, 10.0));
        assertFalse(MathUtils.isWithinBounds(-5.0, 0.0, 10.0));
        
        assertEquals(5.0, MathUtils.clamp(5.0, 0.0, 10.0));
        assertEquals(0.0, MathUtils.clamp(-5.0, 0.0, 10.0));
        assertEquals(10.0, MathUtils.clamp(15.0, 0.0, 10.0));
        
        assertEquals(5, MathUtils.clamp(5, 0, 10));
        assertEquals(0, MathUtils.clamp(-5, 0, 10));
        assertEquals(10, MathUtils.clamp(15, 0, 10));
    }

    @Test
    @DisplayName("Test scheduling specific functions")
    void testSchedulingSpecificFunctions() {
        LocalDateTime start1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime start2 = LocalDateTime.of(2024, 1, 1, 10, 30);
        Duration duration1 = Duration.ofMinutes(60);
        Duration duration2 = Duration.ofMinutes(30);
        
        // Test overlap ratio
        double overlapRatio = MathUtils.calculateOverlapRatio(duration1, start1, duration2, start2);
        assertTrue(overlapRatio > 0.0 && overlapRatio < 1.0);
        
        // Test resource efficiency
        double efficiency = MathUtils.calculateResourceEfficiency(8, 10);
        assertEquals(0.8, efficiency);
        
        // Test cost efficiency
        double costEfficiency = MathUtils.calculateCostEfficiency(100.0, 50.0);
        assertEquals(2.0, costEfficiency);
    }

    @Test
    @DisplayName("Test edge cases and error handling")
    void testEdgeCasesAndErrorHandling() {
        // Test null collections
        assertNull(MathUtils.<Integer>min(null));
        assertNull(MathUtils.<Integer>max(null));
        assertFalse(MathUtils.hasIntersection(null, Set.of("a")));
        assertFalse(MathUtils.isSubset(null, Set.of("a")));
        
        // Test empty collections
        assertEquals(0.0, MathUtils.mean(new ArrayList<Double>()));
        assertEquals(0.0, MathUtils.median(new ArrayList<Double>()));
        assertEquals(0.0, MathUtils.standardDeviation(new ArrayList<Double>()));
        
        // Test invalid parameters
        assertThrows(IllegalArgumentException.class, () -> MathUtils.minValues());
        assertThrows(IllegalArgumentException.class, () -> MathUtils.maxValues());
        assertThrows(IllegalArgumentException.class, () -> MathUtils.round(5.0, -1));
        assertThrows(IllegalArgumentException.class, () -> MathUtils.roundToNearest(5.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> MathUtils.normalize(5.0, 10.0, 5.0));
        assertThrows(IllegalArgumentException.class, () -> MathUtils.calculateDuration(null, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> MathUtils.calculateDuration(LocalDateTime.now(), null));
    }
}
