package com.vikk.appointmentscheduler.gui;

import com.vikk.appointmentscheduler.algorithm.ConstraintSatisfactionAlgorithm;
import com.vikk.appointmentscheduler.algorithm.GeneticAlgorithm;
import com.vikk.appointmentscheduler.algorithm.SimulatedAnnealingAlgorithm;
import com.vikk.appointmentscheduler.algorithm.SchedulingAlgorithm;
import com.vikk.appointmentscheduler.database.AppointmentDAO;
import com.vikk.appointmentscheduler.database.DatabaseManager;
import com.vikk.appointmentscheduler.database.ResourceDAO;
import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.AppointmentStatus;
import com.vikk.appointmentscheduler.model.AppointmentType;
import com.vikk.appointmentscheduler.model.Priority;
import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.ResourceType;
import com.vikk.appointmentscheduler.model.Schedule;
import com.vikk.appointmentscheduler.model.ScheduleMetrics;
import com.vikk.appointmentscheduler.service.AppointmentSchedulingService;
import com.vikk.appointmentscheduler.util.MathUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * JavaFX GUI for the Appointment Scheduling Optimizer.
 * Provides a user-friendly interface for managing appointments, resources,
 * and running optimization algorithms.
 */
public class SchedulingGUI extends Application {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Data
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private ObservableList<Resource> resources = FXCollections.observableArrayList();
    private AppointmentSchedulingService schedulingService;
    private Map<String, Schedule> optimizationResults = new HashMap<>();
    
    // Database
    private AppointmentDAO appointmentDAO;
    private ResourceDAO resourceDAO;
    
    // UI Components
    private TableView<Appointment> appointmentTable;
    private TableView<Resource> resourceTable;
    private TableView<ScheduleResult> resultTable;
    private TextArea logArea;
    private ProgressBar progressBar;
    private Label statusLabel;
    private TextArea scheduleArea;
    
    // Algorithm selection
    private CheckBox cspCheckBox;
    private CheckBox gaCheckBox;
    private CheckBox saCheckBox;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Appointment Scheduling Optimizer");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        // Create main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Title and controls
        VBox topPanel = createTopPanel();
        root.setTop(topPanel);

        // Center: Main content with tabs
        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        // Bottom: Status and progress
        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);

        // Initialize database after UI is created
        initializeDatabase();
        
        // Load data from database
        loadDataFromDatabase();
        
        schedulingService = new AppointmentSchedulingService(
            appointments.stream().collect(Collectors.toList()),
            resources.stream().collect(Collectors.toList())
        );

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));

        // Title
        Label titleLabel = new Label("Appointment Scheduling Optimizer");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        // Algorithm selection
        HBox algorithmPanel = new HBox(20);
        algorithmPanel.setAlignment(Pos.CENTER_LEFT);
        
        Label algorithmLabel = new Label("Select Algorithms:");
        algorithmLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        cspCheckBox = new CheckBox("Constraint Satisfaction (CSP)");
        gaCheckBox = new CheckBox("Genetic Algorithm (GA)");
        saCheckBox = new CheckBox("Simulated Annealing (SA)");
        
        // Select all by default
        cspCheckBox.setSelected(true);
        gaCheckBox.setSelected(true);
        saCheckBox.setSelected(true);
        
        algorithmPanel.getChildren().addAll(algorithmLabel, cspCheckBox, gaCheckBox, saCheckBox);

        // Control buttons
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        
        Button runOptimizationButton = new Button("Run Optimization");
        runOptimizationButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        runOptimizationButton.setOnAction(e -> runOptimization());
        
        Button addAppointmentButton = new Button("Add Appointment");
        addAppointmentButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        addAppointmentButton.setOnAction(e -> showAddAppointmentDialog());
        
        Button addResourceButton = new Button("Add Resource");
        addResourceButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        addResourceButton.setOnAction(e -> showAddResourceDialog());
        
        Button clearDataButton = new Button("Clear All Data");
        clearDataButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        clearDataButton.setOnAction(e -> clearAllData());
        
        buttonPanel.getChildren().addAll(runOptimizationButton, addAppointmentButton, addResourceButton, clearDataButton);

        topPanel.getChildren().addAll(titleLabel, algorithmPanel, buttonPanel);
        return topPanel;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();

        // Appointments tab
        Tab appointmentsTab = new Tab("Appointments");
        appointmentsTab.setContent(createAppointmentsTab());
        appointmentsTab.setClosable(false);

        // Resources tab
        Tab resourcesTab = new Tab("Resources");
        resourcesTab.setContent(createResourcesTab());
        resourcesTab.setClosable(false);

        // Results tab
        Tab resultsTab = new Tab("Optimization Results");
        resultsTab.setContent(createResultsTab());
        resultsTab.setClosable(false);

        // Schedule tab
        Tab scheduleTab = new Tab("Best Schedule");
        scheduleTab.setContent(createScheduleTab());
        scheduleTab.setClosable(false);

        tabPane.getTabs().addAll(appointmentsTab, resourcesTab, resultsTab, scheduleTab);
        return tabPane;
    }

    private VBox createAppointmentsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Table
        appointmentTable = new TableView<>();
        appointmentTable.setItems(appointments);

        // Columns
        TableColumn<Appointment, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Appointment, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Appointment, AppointmentType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Appointment, com.vikk.appointmentscheduler.model.Priority> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Appointment, String> startTimeCol = new TableColumn<>("Requested Start");
        startTimeCol.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime();
            return new javafx.beans.property.SimpleStringProperty(
                startTime != null ? startTime.format(TIME_FORMATTER) : "N/A"
            );
        });

        TableColumn<Appointment, Long> durationCol = new TableColumn<>("Duration (min)");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        TableColumn<Appointment, com.vikk.appointmentscheduler.model.AppointmentStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        appointmentTable.getColumns().addAll(idCol, descriptionCol, typeCol, priorityCol, startTimeCol, durationCol, statusCol);

        // Context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> editSelectedAppointment());
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteSelectedAppointment());
        contextMenu.getItems().addAll(editItem, deleteItem);
        appointmentTable.setContextMenu(contextMenu);

        vbox.getChildren().add(appointmentTable);
        return vbox;
    }

    private VBox createResourcesTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Table
        resourceTable = new TableView<>();
        resourceTable.setItems(resources);

        // Columns
        TableColumn<Resource, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Resource, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Resource, ResourceType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Resource, String> capabilitiesCol = new TableColumn<>("Capabilities");
        capabilitiesCol.setCellValueFactory(cellData -> {
            Set<String> capabilities = cellData.getValue().getCapabilities();
            return new javafx.beans.property.SimpleStringProperty(
                capabilities != null ? capabilities.toString() : "All"
            );
        });

        TableColumn<Resource, Double> costCol = new TableColumn<>("Cost/Hour");
        costCol.setCellValueFactory(cellData -> {
            double cost = cellData.getValue().getCostPerHour();
            return new javafx.beans.property.SimpleDoubleProperty(cost).asObject();
        });

        TableColumn<Resource, String> availabilityCol = new TableColumn<>("Availability");
        availabilityCol.setCellValueFactory(cellData -> {
            Set<LocalDateTime> slots = cellData.getValue().getAvailableSlots();
            return new javafx.beans.property.SimpleStringProperty(
                slots != null ? slots.size() + " slots" : "0 slots"
            );
        });

        resourceTable.getColumns().addAll(idCol, nameCol, typeCol, capabilitiesCol, costCol, availabilityCol);

        // Context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> editSelectedResource());
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteSelectedResource());
        contextMenu.getItems().addAll(editItem, deleteItem);
        resourceTable.setContextMenu(contextMenu);

        vbox.getChildren().add(resourceTable);
        return vbox;
    }

    private VBox createResultsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Results table
        resultTable = new TableView<>();
        resultTable.setItems(FXCollections.observableArrayList());

        // Columns
        TableColumn<ScheduleResult, String> algorithmCol = new TableColumn<>("Algorithm");
        algorithmCol.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));

        TableColumn<ScheduleResult, Long> timeCol = new TableColumn<>("Time (ms)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("executionTime"));

        TableColumn<ScheduleResult, Integer> iterationsCol = new TableColumn<>("Iterations");
        iterationsCol.setCellValueFactory(new PropertyValueFactory<>("iterations"));

        TableColumn<ScheduleResult, Double> efficiencyCol = new TableColumn<>("Efficiency (%)");
        efficiencyCol.setCellValueFactory(new PropertyValueFactory<>("efficiencyScore"));

        TableColumn<ScheduleResult, Double> costCol = new TableColumn<>("Cost ($)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        TableColumn<ScheduleResult, Integer> conflictsCol = new TableColumn<>("Conflicts");
        conflictsCol.setCellValueFactory(new PropertyValueFactory<>("conflictCount"));

        resultTable.getColumns().addAll(algorithmCol, timeCol, iterationsCol, efficiencyCol, costCol, conflictsCol);

        vbox.getChildren().add(resultTable);
        return vbox;
    }

    private VBox createScheduleTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Schedule display
        scheduleArea = new TextArea();
        scheduleArea.setEditable(false);
        scheduleArea.setFont(Font.font("Courier New", 12));
        scheduleArea.setPrefRowCount(20);
        scheduleArea.setText("Run optimization to see the best schedule details.");

        // Update schedule display when results change
        resultTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateScheduleDisplay(scheduleArea, newVal);
            }
        });

        vbox.getChildren().add(scheduleArea);
        return vbox;
    }

    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setAlignment(Pos.CENTER_LEFT);

        // Progress bar
        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);

        // Status label
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("Arial", 12));

        // Log area
        logArea = new TextArea();
        logArea.setPrefRowCount(3);
        logArea.setEditable(false);
        logArea.setFont(Font.font("Courier New", 10));

        VBox logBox = new VBox(5);
        logBox.getChildren().addAll(new Label("Log:"), logArea);

        bottomPanel.getChildren().addAll(progressBar, statusLabel, logBox);
        return bottomPanel;
    }

    private void initializeDatabase() {
        try {
            appointmentDAO = new AppointmentDAO();
            resourceDAO = new ResourceDAO();
            logArea.appendText("Database initialized successfully\n");
        } catch (Exception e) {
            logArea.appendText("Error initializing database: " + e.getMessage() + "\n");
            showAlert("Database Error", "Failed to initialize database: " + e.getMessage());
        }
    }

    private void loadDataFromDatabase() {
        try {
            // Load appointments
            List<Appointment> appointmentList = appointmentDAO.findAll();
            appointments.clear();
            appointments.addAll(appointmentList);
            
            // Load resources
            List<Resource> resourceList = resourceDAO.findAll();
            resources.clear();
            resources.addAll(resourceList);
            
            logArea.appendText("Loaded " + appointments.size() + " appointments and " + resources.size() + " resources from database\n");
            
            // If no data exists, create some sample data
            if (appointments.isEmpty() && resources.isEmpty()) {
                createSampleData();
            }
            
        } catch (Exception e) {
            logArea.appendText("Error loading data from database: " + e.getMessage() + "\n");
            showAlert("Database Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void createSampleData() {
        try {
            // Create sample appointments
            LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            
            Appointment apt1 = createAppointment("APT001", AppointmentType.CONSULTATION, "Initial Consultation",
                    now.plusHours(1), Duration.ofMinutes(60), com.vikk.appointmentscheduler.model.Priority.HIGH);
            Appointment apt2 = createAppointment("APT002", AppointmentType.FOLLOW_UP, "Follow-up Visit",
                    now.plusHours(2), Duration.ofMinutes(30), com.vikk.appointmentscheduler.model.Priority.MEDIUM);
            Appointment apt3 = createAppointment("APT003", AppointmentType.TREATMENT, "Physical Therapy",
                    now.plusHours(1).plusMinutes(30), Duration.ofMinutes(60), com.vikk.appointmentscheduler.model.Priority.MEDIUM);
            Appointment apt4 = createAppointment("APT004", AppointmentType.EMERGENCY, "Emergency Consultation",
                    now.plusMinutes(10), Duration.ofMinutes(30), com.vikk.appointmentscheduler.model.Priority.URGENT);
            Appointment apt5 = createAppointment("APT005", AppointmentType.SURGERY, "Minor Surgery",
                    now.plusHours(3), Duration.ofMinutes(60), com.vikk.appointmentscheduler.model.Priority.HIGH);
            Appointment apt6 = createAppointment("APT006", AppointmentType.CONSULTATION, "Routine Checkup",
                    now.plusHours(2).plusMinutes(45), Duration.ofMinutes(30), com.vikk.appointmentscheduler.model.Priority.LOW);

            // Save appointments to database
            appointmentDAO.save(apt1);
            appointmentDAO.save(apt2);
            appointmentDAO.save(apt3);
            appointmentDAO.save(apt4);
            appointmentDAO.save(apt5);
            appointmentDAO.save(apt6);

            // Create sample resources
            Resource room1 = createResource("ROOM001", "Examination Room 1", ResourceType.ROOM, 
                    Set.of("CONSULTATION", "FOLLOW_UP"), 50.0, now, now.plusDays(1));
            Resource room2 = createResource("ROOM002", "Treatment Room", ResourceType.ROOM, 
                    Set.of("TREATMENT", "THERAPY"), 75.0, now, now.plusDays(1));
            Resource room3 = createResource("ROOM003", "Emergency Room", ResourceType.ROOM, 
                    Set.of("EMERGENCY"), 100.0, now, now.plusDays(1));
            Resource room4 = createResource("ROOM004", "Operating Room", ResourceType.ROOM, 
                    Set.of("SURGERY"), 200.0, now, now.plusDays(1));
            Resource staff1 = createResource("STAFF001", "Dr. Smith", ResourceType.STAFF, 
                    Set.of("CONSULTATION", "FOLLOW_UP"), 150.0, now, now.plusDays(1));
            Resource staff2 = createResource("STAFF002", "Physical Therapist", ResourceType.STAFF, 
                    Set.of("TREATMENT", "THERAPY"), 80.0, now, now.plusDays(1));
            Resource staff3 = createResource("STAFF003", "Dr. Johnson (Surgeon)", ResourceType.STAFF, 
                    Set.of("SURGERY"), 300.0, now, now.plusDays(1));

            // Save resources to database
            resourceDAO.save(room1);
            resourceDAO.save(room2);
            resourceDAO.save(room3);
            resourceDAO.save(room4);
            resourceDAO.save(staff1);
            resourceDAO.save(staff2);
            resourceDAO.save(staff3);

            // Reload data from database
            loadDataFromDatabase();
            
            logArea.appendText("Created sample data\n");
            
        } catch (Exception e) {
            logArea.appendText("Error creating sample data: " + e.getMessage() + "\n");
        }
    }

    private Resource createResource(String id, String name, ResourceType type, Set<String> capabilities, 
                                  double costPerHour, LocalDateTime availableFrom, LocalDateTime availableTo) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setName(name);
        resource.setType(type);
        resource.setCapabilities(capabilities);
        resource.setCostPerHour(costPerHour);
        resource.setAvailableFrom(availableFrom);
        resource.setAvailableTo(availableTo);
        resource.setActive(true);
        return resource;
    }

    private void runOptimization() {
        if (appointments.isEmpty() || resources.isEmpty()) {
            showAlert("Error", "Please add at least one appointment and one resource before running optimization.");
            return;
        }

        // Update service with current data
        schedulingService = new AppointmentSchedulingService(
            appointments.stream().collect(Collectors.toList()),
            resources.stream().collect(Collectors.toList())
        );

        // The service already has all algorithms registered
        // We'll filter results based on selection

        if (schedulingService.getRegisteredAlgorithms().isEmpty()) {
            showAlert("Error", "Please select at least one algorithm.");
            return;
        }

        // Run optimization asynchronously
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate progress
        statusLabel.setText("Running optimization...");
        logArea.appendText("Starting optimization with " + schedulingService.getRegisteredAlgorithms().size() + " algorithms...\n");

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Schedule> results = new HashMap<>();
                
                // Run selected algorithms
                if (cspCheckBox.isSelected()) {
                    Schedule cspResult = schedulingService.optimizeSchedule("CSP");
                    results.put("CSP", cspResult);
                }
                if (gaCheckBox.isSelected()) {
                    Schedule gaResult = schedulingService.optimizeSchedule("GA");
                    results.put("GA", gaResult);
                }
                if (saCheckBox.isSelected()) {
                    Schedule saResult = schedulingService.optimizeSchedule("SA");
                    results.put("SA", saResult);
                }
                
                // Store results for display
                optimizationResults.clear();
                optimizationResults.putAll(results);
                
                final Map<String, AppointmentSchedulingService.AlgorithmComparisonResult> comparisonResults = schedulingService.compareAlgorithms();

                Platform.runLater(() -> {
                    updateResultsTable(comparisonResults);
                    updateBestScheduleDisplay();
                    progressBar.setVisible(false);
                    statusLabel.setText("Optimization completed");
                    logArea.appendText("Optimization completed successfully!\n");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Optimization failed");
                    logArea.appendText("Error: " + e.getMessage() + "\n");
                    showAlert("Error", "Optimization failed: " + e.getMessage());
                });
            }
        });
    }

    private void updateResultsTable(Map<String, AppointmentSchedulingService.AlgorithmComparisonResult> results) {
        ObservableList<ScheduleResult> resultData = FXCollections.observableArrayList();
        
        for (AppointmentSchedulingService.AlgorithmComparisonResult result : results.values()) {
            resultData.add(new ScheduleResult(
                result.getAlgorithmName(),
                result.getExecutionTime(),
                result.getIterations(),
                result.getEfficiencyScore(),
                result.getTotalCost(),
                result.getConflictCount()
            ));
        }
        
        resultTable.setItems(resultData);
    }

    private void updateBestScheduleDisplay() {
        if (optimizationResults.isEmpty()) {
            scheduleArea.setText("No optimization results available. Run optimization first.");
            return;
        }
        
        // Find the best schedule (lowest cost, highest efficiency)
        Schedule bestSchedule = null;
        String bestAlgorithm = "";
        double bestScore = Double.MAX_VALUE;
        
        for (Map.Entry<String, Schedule> entry : optimizationResults.entrySet()) {
            String algorithm = entry.getKey();
            Schedule schedule = entry.getValue();
            
            // Calculate a simple score (lower is better)
            double efficiencyScore = schedule.calculateEfficiencyScore();
            double score = schedule.getTotalCost() - (efficiencyScore * 10);
            if (score < bestScore) {
                bestScore = score;
                bestSchedule = schedule;
                bestAlgorithm = algorithm;
            }
        }
        
        if (bestSchedule != null) {
            displayScheduleDetails(bestSchedule, bestAlgorithm);
        }
    }
    
    private void displayScheduleDetails(Schedule schedule, String algorithm) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEST SCHEDULE - ").append(algorithm).append(" Algorithm\n");
        sb.append("==========================================\n\n");
        
        sb.append("Schedule ID: ").append(schedule.getId()).append("\n");
        sb.append("Start Date: ").append(schedule.getStartDate() != null ? schedule.getStartDate().format(TIME_FORMATTER) : "Not set").append("\n");
        sb.append("End Date: ").append(schedule.getEndDate() != null ? schedule.getEndDate().format(TIME_FORMATTER) : "Not set").append("\n");
        sb.append("Total Cost: $").append(String.format("%.2f", schedule.getTotalCost())).append("\n");
        sb.append("Efficiency Score: ").append(String.format("%.2f", schedule.calculateEfficiencyScore())).append("%\n");
        sb.append("Conflict Count: ").append(schedule.getConflictCount()).append("\n\n");
        
        sb.append("SCHEDULED APPOINTMENTS:\n");
        sb.append("======================\n");
        
        if (schedule.getAppointments().isEmpty()) {
            sb.append("No appointments scheduled.\n");
        } else {
            for (Appointment appointment : schedule.getAppointments()) {
                sb.append("• ").append(appointment.getId()).append(" - ").append(appointment.getTitle()).append("\n");
                sb.append("  Time: ").append(appointment.getStartTime().format(TIME_FORMATTER));
                if (appointment.getEndTime() != null) {
                    sb.append(" - ").append(appointment.getEndTime().format(TIME_FORMATTER));
                }
                sb.append("\n");
                String resourceId = schedule.getResourceForAppointment(appointment.getId());
                sb.append("  Resource: ").append(resourceId != null ? resourceId : "Not assigned").append("\n");
                sb.append("  Type: ").append(appointment.getType()).append(" | Priority: ").append(appointment.getPriority()).append("\n\n");
            }
        }
        
        sb.append("UNSCHEDULED APPOINTMENTS:\n");
        sb.append("========================\n");
        
        if (schedule.getUnassignedAppointments().isEmpty()) {
            sb.append("All appointments scheduled successfully!\n");
        } else {
            sb.append("Unassigned appointment IDs: ").append(String.join(", ", schedule.getUnassignedAppointments())).append("\n");
        }
        
        scheduleArea.setText(sb.toString());
    }

    private void updateScheduleDisplay(TextArea scheduleArea, ScheduleResult result) {
        // This would display the actual schedule details
        // For now, show a summary
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule for ").append(result.getAlgorithmName()).append("\n");
        sb.append("=====================================\n");
        sb.append("Execution Time: ").append(result.getExecutionTime()).append(" ms\n");
        sb.append("Efficiency: ").append(String.format("%.2f", result.getEfficiencyScore())).append("%\n");
        sb.append("Total Cost: $").append(String.format("%.2f", result.getTotalCost())).append("\n");
        sb.append("Conflicts: ").append(result.getConflictCount()).append("\n");
        
        scheduleArea.setText(sb.toString());
    }

    private void showAddAppointmentDialog() {
        try {
            AppointmentDialog dialog = new AppointmentDialog((Stage) appointmentTable.getScene().getWindow(), null);
            Appointment newAppointment = dialog.showAndWait();
            
            if (newAppointment != null) {
                appointmentDAO.save(newAppointment);
                loadDataFromDatabase();
                logArea.appendText("Added appointment: " + newAppointment.getId() + "\n");
            }
        } catch (Exception e) {
            logArea.appendText("Error adding appointment: " + e.getMessage() + "\n");
            showAlert("Error", "Failed to add appointment: " + e.getMessage());
        }
    }

    private void showAddResourceDialog() {
        try {
            ResourceDialog dialog = new ResourceDialog((Stage) resourceTable.getScene().getWindow(), null);
            Resource newResource = dialog.showAndWait();
            
            if (newResource != null) {
                resourceDAO.save(newResource);
                loadDataFromDatabase();
                logArea.appendText("Added resource: " + newResource.getId() + "\n");
            }
        } catch (Exception e) {
            logArea.appendText("Error adding resource: " + e.getMessage() + "\n");
            showAlert("Error", "Failed to add resource: " + e.getMessage());
        }
    }

    private void editSelectedAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                AppointmentDialog dialog = new AppointmentDialog((Stage) appointmentTable.getScene().getWindow(), selected);
                Appointment updatedAppointment = dialog.showAndWait();
                
                if (updatedAppointment != null) {
                    appointmentDAO.update(updatedAppointment);
                    loadDataFromDatabase();
                    logArea.appendText("Updated appointment: " + updatedAppointment.getId() + "\n");
                }
            } catch (Exception e) {
                logArea.appendText("Error updating appointment: " + e.getMessage() + "\n");
                showAlert("Error", "Failed to update appointment: " + e.getMessage());
            }
        } else {
            showAlert("Selection Required", "Please select an appointment to edit.");
        }
    }

    private void deleteSelectedAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Delete");
                confirmAlert.setHeaderText("Delete Appointment");
                confirmAlert.setContentText("Are you sure you want to delete appointment: " + selected.getId() + "?");
                
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    appointmentDAO.delete(selected.getId());
                    loadDataFromDatabase();
                    logArea.appendText("Deleted appointment: " + selected.getId() + "\n");
                }
            } catch (Exception e) {
                logArea.appendText("Error deleting appointment: " + e.getMessage() + "\n");
                showAlert("Error", "Failed to delete appointment: " + e.getMessage());
            }
        } else {
            showAlert("Selection Required", "Please select an appointment to delete.");
        }
    }

    private void editSelectedResource() {
        Resource selected = resourceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                ResourceDialog dialog = new ResourceDialog((Stage) resourceTable.getScene().getWindow(), selected);
                Resource updatedResource = dialog.showAndWait();
                
                if (updatedResource != null) {
                    resourceDAO.update(updatedResource);
                    loadDataFromDatabase();
                    logArea.appendText("Updated resource: " + updatedResource.getId() + "\n");
                }
            } catch (Exception e) {
                logArea.appendText("Error updating resource: " + e.getMessage() + "\n");
                showAlert("Error", "Failed to update resource: " + e.getMessage());
            }
        } else {
            showAlert("Selection Required", "Please select a resource to edit.");
        }
    }

    private void deleteSelectedResource() {
        Resource selected = resourceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Delete");
                confirmAlert.setHeaderText("Delete Resource");
                confirmAlert.setContentText("Are you sure you want to delete resource: " + selected.getId() + "?");
                
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    resourceDAO.delete(selected.getId());
                    loadDataFromDatabase();
                    logArea.appendText("Deleted resource: " + selected.getId() + "\n");
                }
            } catch (Exception e) {
                logArea.appendText("Error deleting resource: " + e.getMessage() + "\n");
                showAlert("Error", "Failed to delete resource: " + e.getMessage());
            }
        } else {
            showAlert("Selection Required", "Please select a resource to delete.");
        }
    }

    private void clearAllData() {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Clear All Data");
            confirmAlert.setHeaderText("Clear All Data");
            confirmAlert.setContentText("Are you sure you want to delete ALL appointments and resources? This action cannot be undone.");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Clear database
                appointmentDAO.deleteAll();
                resourceDAO.deleteAll();
                
                // Clear UI
                appointments.clear();
                resources.clear();
                resultTable.getItems().clear();
                logArea.clear();
                statusLabel.setText("All data cleared");
                
                logArea.appendText("All data cleared from database\n");
            }
        } catch (Exception e) {
            logArea.appendText("Error clearing data: " + e.getMessage() + "\n");
            showAlert("Error", "Failed to clear data: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Appointment createAppointment(String id, AppointmentType type, String description, 
                                        LocalDateTime startTime, Duration duration, com.vikk.appointmentscheduler.model.Priority priority) {
        Appointment appointment = new Appointment(id, description, startTime, duration);
        appointment.setType(type);
        appointment.setPriority(priority);
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointment;
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Helper class for result table
    public static class ScheduleResult {
        private final String algorithmName;
        private final long executionTime;
        private final int iterations;
        private final double efficiencyScore;
        private final double totalCost;
        private final int conflictCount;

        public ScheduleResult(String algorithmName, long executionTime, int iterations, 
                            double efficiencyScore, double totalCost, int conflictCount) {
            this.algorithmName = algorithmName;
            this.executionTime = executionTime;
            this.iterations = iterations;
            this.efficiencyScore = efficiencyScore;
            this.totalCost = totalCost;
            this.conflictCount = conflictCount;
        }

        // Getters
        public String getAlgorithmName() { return algorithmName; }
        public long getExecutionTime() { return executionTime; }
        public int getIterations() { return iterations; }
        public double getEfficiencyScore() { return efficiencyScore; }
        public double getTotalCost() { return totalCost; }
        public int getConflictCount() { return conflictCount; }
    }
}
