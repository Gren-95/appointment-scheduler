package com.vikk.appointmentscheduler.gui;

import com.vikk.appointmentscheduler.model.Appointment;
import com.vikk.appointmentscheduler.model.AppointmentStatus;
import com.vikk.appointmentscheduler.model.AppointmentType;
import com.vikk.appointmentscheduler.model.Priority;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Dialog for creating and editing appointments.
 */
public class AppointmentDialog {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private Stage dialog;
    private Appointment appointment;
    private boolean isEdit;
    
    // Form controls
    private TextField idField;
    private TextField titleField;
    private TextArea descriptionArea;
    private DatePicker startDatePicker;
    private Spinner<Integer> startHourSpinner;
    private Spinner<Integer> startMinuteSpinner;
    private Spinner<Integer> durationSpinner;
    private ComboBox<AppointmentType> typeComboBox;
    private ComboBox<Priority> priorityComboBox;
    private ComboBox<AppointmentStatus> statusComboBox;
    private TextField resourceIdField;
    private TextField clientIdField;
    private CheckBox flexibleCheckBox;
    private Spinner<Integer> flexibilitySpinner;
    private Spinner<Double> importanceSpinner;
    private TextArea capabilitiesArea;
    
    public AppointmentDialog(Stage parent, Appointment appointment) {
        this.appointment = appointment;
        this.isEdit = appointment != null;
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(isEdit ? "Edit Appointment" : "Add New Appointment");
        dialog.setResizable(true);
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        
        // Create form
        GridPane form = createForm();
        mainLayout.getChildren().add(form);
        
        // Create buttons
        HBox buttonBox = createButtons();
        mainLayout.getChildren().add(buttonBox);
        
        Scene scene = new Scene(mainLayout, 700, 800);
        dialog.setScene(scene);
        
        // Populate form if editing
        if (isEdit) {
            populateForm();
        }
    }
    
    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        
        // Set column constraints for better layout
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(150);
        labelCol.setPrefWidth(150);
        
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setMinWidth(200);
        fieldCol.setPrefWidth(300);
        fieldCol.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        
        form.getColumnConstraints().addAll(labelCol, fieldCol);
        
        int row = 0;
        
        // ID
        form.add(new Label("ID:"), 0, row);
        idField = new TextField();
        idField.setPromptText("e.g., APT001");
        form.add(idField, 1, row++);
        
        // Title
        form.add(new Label("Title:"), 0, row);
        titleField = new TextField();
        titleField.setPromptText("Appointment title");
        form.add(titleField, 1, row++);
        
        // Description
        form.add(new Label("Description:"), 0, row);
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Appointment description");
        descriptionArea.setPrefRowCount(3);
        form.add(descriptionArea, 1, row++);
        
        // Start Date and Time
        form.add(new Label("Start Date:"), 0, row);
        startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDateTime.now().toLocalDate());
        form.add(startDatePicker, 1, row++);
        
        form.add(new Label("Start Time:"), 0, row);
        HBox timeBox = new HBox(5);
        startHourSpinner = new Spinner<>(0, 23, LocalDateTime.now().getHour());
        startMinuteSpinner = new Spinner<>(0, 59, LocalDateTime.now().getMinute());
        timeBox.getChildren().addAll(
            new Label("Hour:"), startHourSpinner,
            new Label("Min:"), startMinuteSpinner
        );
        form.add(timeBox, 1, row++);
        
        // Duration
        form.add(new Label("Duration (min):"), 0, row);
        durationSpinner = new Spinner<>(15, 480, 60);
        durationSpinner.setEditable(true);
        form.add(durationSpinner, 1, row++);
        
        // Type
        form.add(new Label("Type:"), 0, row);
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(AppointmentType.values());
        typeComboBox.setValue(AppointmentType.CONSULTATION);
        form.add(typeComboBox, 1, row++);
        
        // Priority
        form.add(new Label("Priority:"), 0, row);
        priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(Priority.values());
        priorityComboBox.setValue(Priority.MEDIUM);
        form.add(priorityComboBox, 1, row++);
        
        // Status
        form.add(new Label("Status:"), 0, row);
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(AppointmentStatus.values());
        statusComboBox.setValue(AppointmentStatus.PENDING);
        form.add(statusComboBox, 1, row++);
        
        // Resource ID
        form.add(new Label("Resource ID:"), 0, row);
        resourceIdField = new TextField();
        resourceIdField.setPromptText("Optional - will be assigned by scheduler");
        form.add(resourceIdField, 1, row++);
        
        // Client ID
        form.add(new Label("Client ID:"), 0, row);
        clientIdField = new TextField();
        clientIdField.setPromptText("Client identifier");
        form.add(clientIdField, 1, row++);
        
        // Flexible
        form.add(new Label("Flexible:"), 0, row);
        flexibleCheckBox = new CheckBox("Appointment time can be adjusted");
        form.add(flexibleCheckBox, 1, row++);
        
        // Flexibility Window
        form.add(new Label("Flexibility (min):"), 0, row);
        flexibilitySpinner = new Spinner<>(0, 240, 30);
        flexibilitySpinner.setEditable(true);
        form.add(flexibilitySpinner, 1, row++);
        
        // Importance Score
        form.add(new Label("Importance Score:"), 0, row);
        importanceSpinner = new Spinner<>(0.1, 10.0, 1.0, 0.1);
        importanceSpinner.setEditable(true);
        form.add(importanceSpinner, 1, row++);
        
        // Capabilities
        form.add(new Label("Required Capabilities:"), 0, row);
        capabilitiesArea = new TextArea();
        capabilitiesArea.setPromptText("Enter capabilities separated by commas (e.g., CONSULTATION, EMERGENCY)");
        capabilitiesArea.setPrefRowCount(2);
        form.add(capabilitiesArea, 1, row++);
        
        return form;
    }
    
    private HBox createButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button saveButton = new Button(isEdit ? "Update" : "Save");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> saveAppointment());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        
        return buttonBox;
    }
    
    private void populateForm() {
        if (appointment == null) return;
        
        idField.setText(appointment.getId());
        titleField.setText(appointment.getTitle());
        descriptionArea.setText(appointment.getDescription());
        
        if (appointment.getStartTime() != null) {
            startDatePicker.setValue(appointment.getStartTime().toLocalDate());
            startHourSpinner.getValueFactory().setValue(appointment.getStartTime().getHour());
            startMinuteSpinner.getValueFactory().setValue(appointment.getStartTime().getMinute());
        }
        
        durationSpinner.getValueFactory().setValue((int) appointment.getDurationMinutes());
        typeComboBox.setValue(appointment.getType());
        priorityComboBox.setValue(appointment.getPriority());
        statusComboBox.setValue(appointment.getStatus());
        resourceIdField.setText(appointment.getResourceId());
        clientIdField.setText(appointment.getClientId());
        flexibleCheckBox.setSelected(appointment.isFlexible());
        flexibilitySpinner.getValueFactory().setValue((int) appointment.getFlexibilityWindow().toMinutes());
        importanceSpinner.getValueFactory().setValue(appointment.getImportanceScore());
        
        // Capabilities
        Set<String> allCapabilities = new HashSet<>(appointment.getRequiredCapabilities());
        allCapabilities.addAll(appointment.getPreferredCapabilities());
        capabilitiesArea.setText(String.join(", ", allCapabilities));
    }
    
    private void saveAppointment() {
        try {
            // Validate required fields
            if (idField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "ID is required");
                return;
            }
            if (titleField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Title is required");
                return;
            }
            if (startDatePicker.getValue() == null) {
                showAlert("Validation Error", "Start date is required");
                return;
            }
            
            // Create or update appointment
            if (appointment == null) {
                appointment = new Appointment();
            }
            
            appointment.setId(idField.getText().trim());
            appointment.setTitle(titleField.getText().trim());
            appointment.setDescription(descriptionArea.getText().trim());
            
            // Set start time
            LocalDateTime startTime = LocalDateTime.of(
                startDatePicker.getValue(),
                LocalTime.of(startHourSpinner.getValue().intValue(), startMinuteSpinner.getValue().intValue())
            );
            appointment.setStartTime(startTime);
            
            // Set duration and end time
            int durationMinutes = durationSpinner.getValue();
            appointment.setDuration(Duration.ofMinutes(durationMinutes));
            appointment.setEndTime(startTime.plusMinutes(durationMinutes));
            
            appointment.setType(typeComboBox.getValue());
            appointment.setPriority(priorityComboBox.getValue());
            appointment.setStatus(statusComboBox.getValue());
            appointment.setResourceId(resourceIdField.getText().trim());
            appointment.setClientId(clientIdField.getText().trim());
            appointment.setFlexible(flexibleCheckBox.isSelected());
            appointment.setFlexibilityWindow(Duration.ofMinutes(flexibilitySpinner.getValue()));
            appointment.setImportanceScore(importanceSpinner.getValue());
            
            // Parse capabilities
            String capabilitiesText = capabilitiesArea.getText().trim();
            Set<String> capabilities = new HashSet<>();
            if (!capabilitiesText.isEmpty()) {
                String[] parts = capabilitiesText.split(",");
                for (String part : parts) {
                    capabilities.add(part.trim().toUpperCase());
                }
            }
            appointment.setRequiredCapabilities(capabilities);
            appointment.setPreferredCapabilities(new HashSet<>());
            
            dialog.close();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to save appointment: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Appointment showAndWait() {
        dialog.showAndWait();
        return appointment;
    }
}
