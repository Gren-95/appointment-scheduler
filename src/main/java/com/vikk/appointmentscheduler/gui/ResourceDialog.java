package com.vikk.appointmentscheduler.gui;

import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.ResourceType;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
 * Dialog for creating and editing resources.
 */
public class ResourceDialog {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private Stage dialog;
    private Resource resource;
    private boolean isEdit;
    
    // Form controls
    private TextField idField;
    private TextField nameField;
    private ComboBox<ResourceType> typeComboBox;
    private Spinner<Double> costSpinner;
    private Spinner<Integer> capacitySpinner;
    private CheckBox activeCheckBox;
    private DatePicker availableFromDatePicker;
    private Spinner<Integer> availableFromHourSpinner;
    private Spinner<Integer> availableFromMinuteSpinner;
    private DatePicker availableToDatePicker;
    private Spinner<Integer> availableToHourSpinner;
    private Spinner<Integer> availableToMinuteSpinner;
    private Spinner<Integer> setupTimeSpinner;
    private Spinner<Integer> cleanupTimeSpinner;
    private TextArea capabilitiesArea;
    
    public ResourceDialog(Stage parent, Resource resource) {
        this.resource = resource;
        this.isEdit = resource != null;
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(isEdit ? "Edit Resource" : "Add New Resource");
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
        idField.setPromptText("e.g., ROOM001");
        form.add(idField, 1, row++);
        
        // Name
        form.add(new Label("Name:"), 0, row);
        nameField = new TextField();
        nameField.setPromptText("Resource name");
        form.add(nameField, 1, row++);
        
        // Type
        form.add(new Label("Type:"), 0, row);
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(ResourceType.values());
        typeComboBox.setValue(ResourceType.ROOM);
        form.add(typeComboBox, 1, row++);
        
        // Cost per hour
        form.add(new Label("Cost per Hour ($):"), 0, row);
        costSpinner = new Spinner<>(0.0, 1000.0, 50.0, 5.0);
        costSpinner.setEditable(true);
        form.add(costSpinner, 1, row++);
        
        // Capacity
        form.add(new Label("Capacity:"), 0, row);
        capacitySpinner = new Spinner<>(1, 100, 1);
        capacitySpinner.setEditable(true);
        form.add(capacitySpinner, 1, row++);
        
        // Active
        form.add(new Label("Active:"), 0, row);
        activeCheckBox = new CheckBox("Resource is available for scheduling");
        activeCheckBox.setSelected(true);
        form.add(activeCheckBox, 1, row++);
        
        // Available From
        form.add(new Label("Available From:"), 0, row);
        HBox availableFromBox = new HBox(5);
        availableFromDatePicker = new DatePicker();
        availableFromDatePicker.setValue(LocalDateTime.now().toLocalDate());
        availableFromHourSpinner = new Spinner<>(0, 23, 8);
        availableFromMinuteSpinner = new Spinner<>(0, 59, 0);
        availableFromBox.getChildren().addAll(
            availableFromDatePicker,
            new Label("Time:"), availableFromHourSpinner,
            new Label(":"), availableFromMinuteSpinner
        );
        form.add(availableFromBox, 1, row++);
        
        // Available To
        form.add(new Label("Available To:"), 0, row);
        HBox availableToBox = new HBox(5);
        availableToDatePicker = new DatePicker();
        availableToDatePicker.setValue(LocalDateTime.now().plusDays(1).toLocalDate());
        availableToHourSpinner = new Spinner<>(0, 23, 17);
        availableToMinuteSpinner = new Spinner<>(0, 59, 0);
        availableToBox.getChildren().addAll(
            availableToDatePicker,
            new Label("Time:"), availableToHourSpinner,
            new Label(":"), availableToMinuteSpinner
        );
        form.add(availableToBox, 1, row++);
        
        // Setup Time
        form.add(new Label("Setup Time (min):"), 0, row);
        setupTimeSpinner = new Spinner<>(0, 120, 0);
        setupTimeSpinner.setEditable(true);
        form.add(setupTimeSpinner, 1, row++);
        
        // Cleanup Time
        form.add(new Label("Cleanup Time (min):"), 0, row);
        cleanupTimeSpinner = new Spinner<>(0, 120, 0);
        cleanupTimeSpinner.setEditable(true);
        form.add(cleanupTimeSpinner, 1, row++);
        
        // Capabilities
        form.add(new Label("Capabilities:"), 0, row);
        capabilitiesArea = new TextArea();
        capabilitiesArea.setPromptText("Enter capabilities separated by commas (e.g., CONSULTATION, EMERGENCY, SURGERY)");
        capabilitiesArea.setPrefRowCount(3);
        form.add(capabilitiesArea, 1, row++);
        
        return form;
    }
    
    private HBox createButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button saveButton = new Button(isEdit ? "Update" : "Save");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> saveResource());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        
        return buttonBox;
    }
    
    private void populateForm() {
        if (resource == null) return;
        
        idField.setText(resource.getId());
        nameField.setText(resource.getName());
        typeComboBox.setValue(resource.getType());
        costSpinner.getValueFactory().setValue(resource.getCostPerHour());
        capacitySpinner.getValueFactory().setValue(resource.getCapacity());
        activeCheckBox.setSelected(resource.isActive());
        
        if (resource.getAvailableFrom() != null) {
            availableFromDatePicker.setValue(resource.getAvailableFrom().toLocalDate());
            availableFromHourSpinner.getValueFactory().setValue(resource.getAvailableFrom().getHour());
            availableFromMinuteSpinner.getValueFactory().setValue(resource.getAvailableFrom().getMinute());
        }
        
        if (resource.getAvailableTo() != null) {
            availableToDatePicker.setValue(resource.getAvailableTo().toLocalDate());
            availableToHourSpinner.getValueFactory().setValue(resource.getAvailableTo().getHour());
            availableToMinuteSpinner.getValueFactory().setValue(resource.getAvailableTo().getMinute());
        }
        
        setupTimeSpinner.getValueFactory().setValue((int) resource.getSetupTime().toMinutes());
        cleanupTimeSpinner.getValueFactory().setValue((int) resource.getCleanupTime().toMinutes());
        
        // Capabilities
        capabilitiesArea.setText(String.join(", ", resource.getCapabilities()));
    }
    
    private void saveResource() {
        try {
            // Validate required fields
            if (idField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "ID is required");
                return;
            }
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Name is required");
                return;
            }
            if (availableFromDatePicker.getValue() == null) {
                showAlert("Validation Error", "Available from date is required");
                return;
            }
            if (availableToDatePicker.getValue() == null) {
                showAlert("Validation Error", "Available to date is required");
                return;
            }
            
            // Create or update resource
            if (resource == null) {
                resource = new Resource();
            }
            
            resource.setId(idField.getText().trim());
            resource.setName(nameField.getText().trim());
            resource.setType(typeComboBox.getValue());
            resource.setCostPerHour(costSpinner.getValue());
            resource.setCapacity(capacitySpinner.getValue());
            resource.setActive(activeCheckBox.isSelected());
            
            // Set available from time
            LocalDateTime availableFrom = LocalDateTime.of(
                availableFromDatePicker.getValue(),
                LocalTime.of(availableFromHourSpinner.getValue().intValue(), availableFromMinuteSpinner.getValue().intValue())
            );
            resource.setAvailableFrom(availableFrom);
            
            // Set available to time
            LocalDateTime availableTo = LocalDateTime.of(
                availableToDatePicker.getValue(),
                LocalTime.of(availableToHourSpinner.getValue().intValue(), availableToMinuteSpinner.getValue().intValue())
            );
            resource.setAvailableTo(availableTo);
            
            resource.setSetupTime(Duration.ofMinutes(setupTimeSpinner.getValue()));
            resource.setCleanupTime(Duration.ofMinutes(cleanupTimeSpinner.getValue()));
            
            // Parse capabilities
            String capabilitiesText = capabilitiesArea.getText().trim();
            Set<String> capabilities = new HashSet<>();
            if (!capabilitiesText.isEmpty()) {
                String[] parts = capabilitiesText.split(",");
                for (String part : parts) {
                    capabilities.add(part.trim().toUpperCase());
                }
            }
            resource.setCapabilities(capabilities);
            
            dialog.close();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to save resource: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Resource showAndWait() {
        dialog.showAndWait();
        return resource;
    }
}
