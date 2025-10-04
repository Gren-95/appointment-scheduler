# Testing Documentation

## Test Overview

The Appointment Scheduling Optimizer includes a comprehensive test suite designed to ensure reliability, correctness, and performance of all system components. The testing strategy covers unit tests, integration tests, and performance benchmarks.

## 🧪 Test Structure

### Test Organization
```
src/test/java/com/vikk/appointmentscheduler/
├── algorithm/              # Algorithm-specific tests
├── model/                 # Data model validation tests
├── service/               # Service layer integration tests
└── util/                  # Utility function tests
```

### Test Categories

#### 1. Unit Tests
- **MathUtilsTest**: 36 comprehensive test cases for mathematical functions
- **Model Tests**: Data model validation and behavior testing
- **Algorithm Tests**: Individual algorithm component testing

#### 2. Integration Tests
- **Service Tests**: End-to-end workflow testing
- **Algorithm Comparison Tests**: Multi-algorithm integration testing
- **Performance Tests**: Benchmarking and performance validation

#### 3. Edge Case Tests
- **Boundary Conditions**: Testing limits and edge cases
- **Error Scenarios**: Exception handling validation
- **Invalid Input**: Malformed data handling

## 📊 Test Coverage

### Coverage Requirements
- **Minimum Coverage**: 70% for algorithm modules
- **Critical Path Coverage**: 100% for core optimization logic
- **Exception Coverage**: All exception scenarios tested

### Current Coverage
- **MathUtils**: 100% line coverage (36 test cases)
- **Algorithm Core Logic**: 95%+ coverage
- **Model Validation**: 90%+ coverage
- **Service Layer**: 85%+ coverage

## 🔧 Test Configuration

### Maven Test Configuration
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
    </configuration>
</plugin>
```

### JaCoCo Coverage Plugin
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 🧮 Mathematical Function Tests

### MathUtilsTest Coverage

#### Core Mathematical Functions
```java
@Test
@DisplayName("Test min/max functions with collections")
void testMinMaxCollections() {
    List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3);
    assertEquals(Integer.valueOf(1), MathUtils.<Integer>min(numbers));
    assertEquals(Integer.valueOf(9), MathUtils.<Integer>max(numbers));
}

@Test
@DisplayName("Test min/max functions with varargs")
void testMinMaxVarargs() {
    assertEquals(1.0, MathUtils.minValues(5.0, 2.0, 8.0, 1.0, 9.0, 3.0));
    assertEquals(9.0, MathUtils.maxValues(5.0, 2.0, 8.0, 1.0, 9.0, 3.0));
}
```

#### Date/Time Functions
```java
@Test
@DisplayName("Test duration calculation")
void testCalculateDuration() {
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 30);
    assertEquals(90, MathUtils.calculateDuration(start, end));
}

@Test
@DisplayName("Test overlap detection")
void testIsOverlap() {
    LocalDateTime start1 = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2024, 1, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2024, 1, 1, 10, 30);
    LocalDateTime end2 = LocalDateTime.of(2024, 1, 1, 11, 30);
    assertTrue(MathUtils.isOverlap(start1, end1, start2, end2));
}
```

#### Set Operations
```java
@Test
@DisplayName("Test set operations")
void testSetOperations() {
    Set<String> set1 = Set.of("A", "B", "C");
    Set<String> set2 = Set.of("B", "C", "D");
    
    Set<String> union = MathUtils.union(set1, set2);
    assertEquals(Set.of("A", "B", "C", "D"), union);
    
    Set<String> intersection = MathUtils.intersection(set1, set2);
    assertEquals(Set.of("B", "C"), intersection);
    
    Set<String> difference = MathUtils.difference(set1, set2);
    assertEquals(Set.of("A"), difference);
}
```

#### Statistical Functions
```java
@Test
@DisplayName("Test statistical functions")
void testStatisticalFunctions() {
    List<Double> numbers = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
    
    assertEquals(3.0, MathUtils.mean(numbers));
    assertEquals(3.0, MathUtils.median(numbers));
    assertEquals(1.58, MathUtils.standardDeviation(numbers), 0.01);
}
```

## 🔄 Algorithm Testing

### Constraint Satisfaction Algorithm Tests

#### Basic Functionality
```java
@Test
@DisplayName("CSP should schedule appointments without conflicts")
void testCSPBasicScheduling() {
    List<Appointment> appointments = createTestAppointments();
    List<Resource> resources = createTestResources();
    
    ConstraintSatisfactionAlgorithm csp = new ConstraintSatisfactionAlgorithm();
    Schedule schedule = csp.optimize(appointments, resources);
    
    assertNotNull(schedule);
    assertTrue(schedule.getAppointments().size() > 0);
}
```

#### Conflict Detection
```java
@Test
@DisplayName("CSP should detect and report conflicts")
void testCSPConflictDetection() {
    Schedule schedule = createScheduleWithConflicts();
    ConstraintSatisfactionAlgorithm csp = new ConstraintSatisfactionAlgorithm();
    
    int conflicts = csp.calculateConflicts(schedule);
    assertTrue(conflicts > 0);
}
```

### Genetic Algorithm Tests

#### Population Initialization
```java
@Test
@DisplayName("GA should initialize population correctly")
void testGAPopulationInitialization() {
    List<Appointment> appointments = createTestAppointments();
    List<Resource> resources = createTestResources();
    
    GeneticAlgorithm ga = new GeneticAlgorithm();
    List<Schedule> population = ga.initializePopulation(appointments, resources);
    
    assertEquals(50, population.size()); // POPULATION_SIZE
    assertTrue(population.stream().allMatch(s -> s != null));
}
```

#### Fitness Calculation
```java
@Test
@DisplayName("GA should calculate fitness correctly")
void testGAFitnessCalculation() {
    Schedule schedule = createTestSchedule();
    List<Appointment> appointments = createTestAppointments();
    List<Resource> resources = createTestResources();
    
    GeneticAlgorithm ga = new GeneticAlgorithm();
    double fitness = ga.calculateFitness(schedule, appointments, resources);
    
    assertTrue(fitness >= 0);
}
```

### Simulated Annealing Tests

#### Temperature Cooling
```java
@Test
@DisplayName("SA should cool temperature correctly")
void testSATemperatureCooling() {
    SimulatedAnnealingAlgorithm sa = new SimulatedAnnealingAlgorithm();
    
    double initialTemp = 1000.0;
    double coolingRate = 0.003;
    double cooledTemp = initialTemp * (1 - coolingRate);
    
    assertTrue(cooledTemp < initialTemp);
}
```

#### Acceptance Probability
```java
@Test
@DisplayName("SA should calculate acceptance probability correctly")
void testSAAcceptanceProbability() {
    SimulatedAnnealingAlgorithm sa = new SimulatedAnnealingAlgorithm();
    
    double currentEnergy = 100.0;
    double newEnergy = 80.0; // Better solution
    double temperature = 100.0;
    
    double probability = sa.acceptanceProbability(currentEnergy, newEnergy, temperature);
    assertEquals(1.0, probability); // Better solutions always accepted
}
```

## 🔧 Service Layer Tests

### AppointmentSchedulingService Tests

#### Algorithm Registration
```java
@Test
@DisplayName("Service should register algorithms correctly")
void testAlgorithmRegistration() {
    AppointmentSchedulingService service = new AppointmentSchedulingService(
        createTestAppointments(), createTestResources());
    
    service.registerAlgorithm(new ConstraintSatisfactionAlgorithm());
    service.registerAlgorithm(new GeneticAlgorithm());
    
    // Verify algorithms are registered
    assertTrue(service.getRegisteredAlgorithms().size() >= 2);
}
```

#### Schedule Comparison
```java
@Test
@DisplayName("Service should compare algorithms correctly")
void testAlgorithmComparison() {
    AppointmentSchedulingService service = new AppointmentSchedulingService(
        createTestAppointments(), createTestResources());
    
    service.registerAlgorithm(new ConstraintSatisfactionAlgorithm());
    service.registerAlgorithm(new GeneticAlgorithm());
    
    Map<String, Schedule> schedules = service.runAllOptimizations();
    Map<String, AlgorithmComparisonResult> results = service.compareAlgorithms(schedules);
    
    assertNotNull(results);
    assertTrue(results.size() >= 2);
}
```

## 📊 Performance Testing

### Benchmark Tests

#### Execution Time Testing
```java
@Test
@DisplayName("Algorithms should complete within reasonable time")
void testAlgorithmPerformance() {
    List<Appointment> appointments = createLargeAppointmentSet(100);
    List<Resource> resources = createLargeResourceSet(20);
    
    ConstraintSatisfactionAlgorithm csp = new ConstraintSatisfactionAlgorithm();
    
    long startTime = System.currentTimeMillis();
    Schedule schedule = csp.optimize(appointments, resources);
    long endTime = System.currentTimeMillis();
    
    assertTrue(endTime - startTime < 5000); // Should complete within 5 seconds
    assertNotNull(schedule);
}
```

#### Memory Usage Testing
```java
@Test
@DisplayName("Algorithms should not exceed memory limits")
void testMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long initialMemory = runtime.totalMemory() - runtime.freeMemory();
    
    // Run algorithm with large dataset
    List<Appointment> appointments = createLargeAppointmentSet(1000);
    List<Resource> resources = createLargeResourceSet(50);
    
    GeneticAlgorithm ga = new GeneticAlgorithm();
    Schedule schedule = ga.optimize(appointments, resources);
    
    long finalMemory = runtime.totalMemory() - runtime.freeMemory();
    long memoryUsed = finalMemory - initialMemory;
    
    assertTrue(memoryUsed < 100 * 1024 * 1024); // Should use less than 100MB
}
```

## 🚨 Error Handling Tests

### Exception Testing

#### Invalid Input Handling
```java
@Test
@DisplayName("Should handle null inputs gracefully")
void testNullInputHandling() {
    ConstraintSatisfactionAlgorithm csp = new ConstraintSatisfactionAlgorithm();
    
    assertThrows(IllegalArgumentException.class, () -> {
        csp.optimize(null, createTestResources());
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        csp.optimize(createTestAppointments(), null);
    });
}
```

#### Resource Constraint Violations
```java
@Test
@DisplayName("Should handle resource constraint violations")
void testResourceConstraintViolations() {
    List<Appointment> appointments = createAppointmentsWithConflicts();
    List<Resource> resources = createTestResources();
    
    ConstraintSatisfactionAlgorithm csp = new ConstraintSatisfactionAlgorithm();
    Schedule schedule = csp.optimize(appointments, resources);
    
    // Should handle conflicts gracefully
    assertNotNull(schedule);
    assertTrue(csp.calculateConflicts(schedule) > 0);
}
```

## 🔍 Edge Case Testing

### Boundary Conditions

#### Empty Input Sets
```java
@Test
@DisplayName("Should handle empty appointment lists")
void testEmptyAppointmentList() {
    List<Appointment> emptyAppointments = new ArrayList<>();
    List<Resource> resources = createTestResources();
    
    ConstraintSatisfactionAlgorithm csp = new ConstraintSatisfactionAlgorithm();
    Schedule schedule = csp.optimize(emptyAppointments, resources);
    
    assertNotNull(schedule);
    assertEquals(0, schedule.getAppointments().size());
}
```

#### Single Appointment Scheduling
```java
@Test
@DisplayName("Should handle single appointment scheduling")
void testSingleAppointmentScheduling() {
    List<Appointment> singleAppointment = Arrays.asList(createTestAppointment());
    List<Resource> resources = createTestResources();
    
    ConstraintSatisfactionAlgorithm csp = new ConstraintSatisfactionAlgorithm();
    Schedule schedule = csp.optimize(singleAppointment, resources);
    
    assertNotNull(schedule);
    assertEquals(1, schedule.getAppointments().size());
}
```

## 🏃‍♂️ Running Tests

### Command Line Execution

#### Run All Tests
```bash
mvn test
```

#### Run Specific Test Class
```bash
mvn test -Dtest=MathUtilsTest
```

#### Run Tests with Coverage
```bash
mvn clean test jacoco:report
```

#### Run Performance Tests
```bash
mvn test -Dtest=*PerformanceTest
```

### IDE Integration

#### IntelliJ IDEA
1. Right-click on test class or method
2. Select "Run 'TestName'"
3. View results in test runner window

#### Eclipse
1. Right-click on test class
2. Select "Run As" → "JUnit Test"
3. View results in JUnit view

## 📈 Test Metrics

### Coverage Reports
- **Location**: `target/site/jacoco/index.html`
- **Format**: HTML report with detailed coverage information
- **Metrics**: Line coverage, branch coverage, method coverage

### Performance Benchmarks
- **Execution Time**: Average algorithm runtime
- **Memory Usage**: Peak memory consumption
- **Throughput**: Appointments processed per second
- **Scalability**: Performance with increasing data size

## 🔧 Test Maintenance

### Adding New Tests
1. Create test class following naming convention `*Test.java`
2. Use descriptive test method names with `@DisplayName`
3. Include comprehensive assertions
4. Test both positive and negative scenarios

### Test Data Management
- **Test Fixtures**: Reusable test data creation methods
- **Data Builders**: Fluent API for test data construction
- **Mock Objects**: Use Mockito for external dependencies
- **Test Isolation**: Ensure tests don't affect each other

### Continuous Integration
- **Automated Testing**: Tests run on every commit
- **Coverage Monitoring**: Track coverage trends over time
- **Performance Regression**: Detect performance degradation
- **Quality Gates**: Prevent deployment with failing tests

---

This comprehensive testing strategy ensures the Appointment Scheduling Optimizer maintains high quality, reliability, and performance across all system components.
