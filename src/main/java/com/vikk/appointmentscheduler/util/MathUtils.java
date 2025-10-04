package com.vikk.appointmentscheduler.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mathematical utility functions for appointment scheduling optimization.
 * This class provides ≥5 mathematical and logical functions as required by the assignment.
 */
public final class MathUtils {

    private MathUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * 1. MIN/MAX functions for finding extreme values in collections
     */
    public static <T extends Comparable<T>> T min(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return Collections.min(collection);
    }

    public static <T extends Comparable<T>> T max(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return Collections.max(collection);
    }

    public static double minValues(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values array cannot be null or empty");
        }
        double min = values[0];
        for (int i = 1; i < values.length; i++) {
            min = Math.min(min, values[i]);
        }
        return min;
    }

    public static double maxValues(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values array cannot be null or empty");
        }
        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            max = Math.max(max, values[i]);
        }
        return max;
    }

    /**
     * 2. ABS function for absolute values
     */
    public static double abs(double value) {
        return Math.abs(value);
    }

    public static int abs(int value) {
        return Math.abs(value);
    }

    public static long abs(long value) {
        return Math.abs(value);
    }

    /**
     * 3. ROUNDING functions with various precision levels
     */
    public static double round(double value, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Decimal places must be non-negative");
        }
        double multiplier = Math.pow(10, decimalPlaces);
        return Math.round(value * multiplier) / multiplier;
    }

    public static double roundToNearest(double value, double nearest) {
        if (nearest <= 0) {
            throw new IllegalArgumentException("Nearest value must be positive");
        }
        return Math.round(value / nearest) * nearest;
    }

    public static long roundToMinutes(Duration duration) {
        return Math.round(duration.toMinutes());
    }

    /**
     * 4. MODULO function for cyclic calculations
     */
    public static int mod(int dividend, int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        int result = dividend % divisor;
        return result < 0 ? result + divisor : result;
    }

    public static double mod(double dividend, double divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        double result = dividend % divisor;
        return result < 0 ? result + divisor : result;
    }

    /**
     * 5. DATE/TIME calculations for scheduling
     */
    public static Duration calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end times cannot be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        return Duration.between(start, end);
    }

    public static boolean isWithinTimeWindow(LocalDateTime time, LocalDateTime windowStart, 
                                           LocalDateTime windowEnd) {
        if (time == null || windowStart == null || windowEnd == null) {
            return false;
        }
        return !time.isBefore(windowStart) && !time.isAfter(windowEnd);
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        return dateTime.plusMinutes(minutes);
    }

    public static LocalDateTime subtractMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        return dateTime.minusMinutes(minutes);
    }

    /**
     * 6. SET OPERATIONS for capability matching and conflict detection
     */
    public static <T> boolean hasIntersection(Collection<T> set1, Collection<T> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }
        return set1.stream().anyMatch(set2::contains);
    }

    public static <T> boolean isSubset(Collection<T> subset, Collection<T> superset) {
        if (subset == null || superset == null) {
            return false;
        }
        return superset.containsAll(subset);
    }

    public static <T> List<T> intersection(Collection<T> set1, Collection<T> set2) {
        if (set1 == null || set2 == null) {
            return new ArrayList<>();
        }
        return set1.stream()
                .filter(set2::contains)
                .collect(Collectors.toList());
    }

    public static <T> List<T> union(Collection<T> set1, Collection<T> set2) {
        if (set1 == null && set2 == null) {
            return new ArrayList<>();
        }
        if (set1 == null) {
            return new ArrayList<>(set2);
        }
        if (set2 == null) {
            return new ArrayList<>(set1);
        }
        List<T> result = new ArrayList<>(set1);
        result.addAll(set2);
        return result.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 7. BOOLEAN LOGIC functions for constraint evaluation
     */
    public static boolean allTrue(boolean... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (boolean value : values) {
            if (!value) {
                return false;
            }
        }
        return true;
    }

    public static boolean anyTrue(boolean... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (boolean value : values) {
            if (value) {
                return true;
            }
        }
        return false;
    }

    public static boolean xor(boolean a, boolean b) {
        return a != b;
    }

    /**
     * 8. STATISTICAL functions for performance analysis
     */
    public static double mean(Collection<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public static double median(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    public static double standardDeviation(Collection<Double> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }
        double mean = mean(values);
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * 9. OPTIMIZATION helper functions
     */
    public static double calculateEuclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double calculateManhattanDistance(double x1, double y1, double x2, double y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    public static double normalize(double value, double min, double max) {
        if (max <= min) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        return (value - min) / (max - min);
    }

    public static double denormalize(double normalizedValue, double min, double max) {
        if (max <= min) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        return min + normalizedValue * (max - min);
    }

    /**
     * 10. CONSTRAINT SATISFACTION helper functions
     */
    public static boolean isWithinBounds(double value, double lowerBound, double upperBound) {
        return value >= lowerBound && value <= upperBound;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 11. SCHEDULING specific mathematical functions
     */
    public static double calculateOverlapRatio(Duration duration1, LocalDateTime start1,
                                             Duration duration2, LocalDateTime start2) {
        LocalDateTime end1 = start1.plus(duration1);
        LocalDateTime end2 = start2.plus(duration2);
        
        LocalDateTime overlapStart = max(start1, start2);
        LocalDateTime overlapEnd = min(end1, end2);
        
        if (overlapStart.isAfter(overlapEnd) || overlapStart.isEqual(overlapEnd)) {
            return 0.0;
        }
        
        Duration overlapDuration = Duration.between(overlapStart, overlapEnd);
        Duration totalDuration = duration1.plus(duration2);
        
        return (double) overlapDuration.toMinutes() / totalDuration.toMinutes();
    }

    private static LocalDateTime max(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isAfter(dateTime2) ? dateTime1 : dateTime2;
    }

    private static LocalDateTime min(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isBefore(dateTime2) ? dateTime1 : dateTime2;
    }

    public static double calculateResourceEfficiency(int scheduledAppointments, int totalCapacity) {
        if (totalCapacity <= 0) {
            return 0.0;
        }
        return (double) scheduledAppointments / totalCapacity;
    }

    public static double calculateCostEfficiency(double totalScore, double totalCost) {
        if (totalCost <= 0) {
            return totalScore;
        }
        return totalScore / totalCost;
    }
}
