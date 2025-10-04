package com.vikk.appointmentscheduler.database;

import com.vikk.appointmentscheduler.model.Resource;
import com.vikk.appointmentscheduler.model.ResourceType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceDAO.
 */
class ResourceDAOTest {

    private ResourceDAO resourceDAO;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        databaseManager = DatabaseManager.getInstance();
        resourceDAO = new ResourceDAO();
        
        // Clear existing data
        try {
            resourceDAO.deleteAll();
        } catch (Exception e) {
            // Ignore if no data exists
        }
    }

    @AfterEach
    void tearDown() {
        try {
            resourceDAO.deleteAll();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    @DisplayName("Test save resource")
    void testSaveResource() throws Exception {
        Resource resource = createTestResource("RES001", "Test Room", ResourceType.ROOM);
        
        resourceDAO.save(resource);
        
        Resource saved = resourceDAO.findById("RES001");
        assertNotNull(saved, "Saved resource should not be null");
        assertEquals("RES001", saved.getId());
        assertEquals("Test Room", saved.getName());
        assertEquals(ResourceType.ROOM, saved.getType());
        assertEquals(50.0, saved.getCostPerHour());
    }

    @Test
    @DisplayName("Test find resource by ID")
    void testFindById() throws Exception {
        Resource resource = createTestResource("RES002", "Find Test", ResourceType.EQUIPMENT);
        resourceDAO.save(resource);
        
        Resource found = resourceDAO.findById("RES002");
        assertNotNull(found, "Found resource should not be null");
        assertEquals("RES002", found.getId());
        assertEquals("Find Test", found.getName());
        assertEquals(ResourceType.EQUIPMENT, found.getType());
    }

    @Test
    @DisplayName("Test find non-existent resource")
    void testFindNonExistentResource() throws Exception {
        Resource found = resourceDAO.findById("NONEXISTENT");
        assertNull(found, "Non-existent resource should be null");
    }

    @Test
    @DisplayName("Test find all resources")
    void testFindAll() throws Exception {
        // Save multiple resources
        resourceDAO.save(createTestResource("RES003", "Resource 1", ResourceType.ROOM));
        resourceDAO.save(createTestResource("RES004", "Resource 2", ResourceType.EQUIPMENT));
        resourceDAO.save(createTestResource("RES005", "Resource 3", ResourceType.STAFF));
        
        List<Resource> allResources = resourceDAO.findAll();
        assertEquals(3, allResources.size(), "Should find 3 resources");
        
        // Check that all resources are present
        Set<String> ids = allResources.stream()
                .map(Resource::getId)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(ids.contains("RES003"));
        assertTrue(ids.contains("RES004"));
        assertTrue(ids.contains("RES005"));
    }

    @Test
    @DisplayName("Test find resources by type")
    void testFindByType() throws Exception {
        // Save resources of different types
        resourceDAO.save(createTestResource("ROOM001", "Room 1", ResourceType.ROOM));
        resourceDAO.save(createTestResource("ROOM002", "Room 2", ResourceType.ROOM));
        resourceDAO.save(createTestResource("EQUIP001", "Equipment 1", ResourceType.EQUIPMENT));
        resourceDAO.save(createTestResource("STAFF001", "Staff 1", ResourceType.STAFF));
        
        List<Resource> rooms = resourceDAO.findByType(ResourceType.ROOM);
        assertEquals(2, rooms.size(), "Should find 2 rooms");
        
        List<Resource> equipment = resourceDAO.findByType(ResourceType.EQUIPMENT);
        assertEquals(1, equipment.size(), "Should find 1 equipment");
        assertEquals("EQUIP001", equipment.get(0).getId());
        
        List<Resource> staff = resourceDAO.findByType(ResourceType.STAFF);
        assertEquals(1, staff.size(), "Should find 1 staff");
        assertEquals("STAFF001", staff.get(0).getId());
    }

    @Test
    @DisplayName("Test update resource")
    void testUpdateResource() throws Exception {
        Resource resource = createTestResource("RES006", "Original Name", ResourceType.ROOM);
        resourceDAO.save(resource);
        
        // Update the resource
        resource.setName("Updated Name");
        resource.setType(ResourceType.EQUIPMENT);
        resource.setCostPerHour(75.0);
        resourceDAO.update(resource);
        
        Resource updated = resourceDAO.findById("RES006");
        assertNotNull(updated, "Updated resource should not be null");
        assertEquals("Updated Name", updated.getName());
        assertEquals(ResourceType.EQUIPMENT, updated.getType());
        assertEquals(75.0, updated.getCostPerHour());
    }

    @Test
    @DisplayName("Test delete resource")
    void testDeleteResource() throws Exception {
        Resource resource = createTestResource("RES007", "To Delete", ResourceType.ROOM);
        resourceDAO.save(resource);
        
        // Verify it exists
        assertNotNull(resourceDAO.findById("RES007"));
        
        // Delete it
        resourceDAO.delete("RES007");
        
        // Verify it's gone
        assertNull(resourceDAO.findById("RES007"));
    }

    @Test
    @DisplayName("Test delete all resources")
    void testDeleteAll() throws Exception {
        // Save some resources
        resourceDAO.save(createTestResource("RES008", "Resource 1", ResourceType.ROOM));
        resourceDAO.save(createTestResource("RES009", "Resource 2", ResourceType.EQUIPMENT));
        
        // Verify they exist
        assertEquals(2, resourceDAO.findAll().size());
        
        // Delete all
        resourceDAO.deleteAll();
        
        // Verify they're all gone
        assertEquals(0, resourceDAO.findAll().size());
    }

    @Test
    @DisplayName("Test resource with capabilities")
    void testResourceWithCapabilities() throws Exception {
        Resource resource = createTestResource("RES010", "With Capabilities", ResourceType.ROOM);
        resource.setCapabilities(Set.of("room", "equipment", "parking"));
        
        resourceDAO.save(resource);
        
        Resource saved = resourceDAO.findById("RES010");
        assertNotNull(saved, "Saved resource should not be null");
        assertEquals(3, saved.getCapabilities().size());
        assertTrue(saved.getCapabilities().contains("room"));
        assertTrue(saved.getCapabilities().contains("equipment"));
        assertTrue(saved.getCapabilities().contains("parking"));
    }

    @Test
    @DisplayName("Test resource with availability")
    void testResourceWithAvailability() throws Exception {
        Resource resource = createTestResource("RES011", "Available Resource", ResourceType.ROOM);
        LocalDateTime now = LocalDateTime.now();
        resource.setAvailableFrom(now.plusHours(1));
        resource.setAvailableTo(now.plusHours(9));
        
        resourceDAO.save(resource);
        
        Resource saved = resourceDAO.findById("RES011");
        assertNotNull(saved, "Saved resource should not be null");
        assertNotNull(saved.getAvailableFrom());
        assertNotNull(saved.getAvailableTo());
        assertTrue(saved.getAvailableFrom().isAfter(now));
        assertTrue(saved.getAvailableTo().isAfter(saved.getAvailableFrom()));
    }

    @Test
    @DisplayName("Test different resource types")
    void testDifferentResourceTypes() throws Exception {
        Resource room = createTestResource("ROOM001", "Conference Room", ResourceType.ROOM);
        room.setCostPerHour(25.0);
        
        Resource equipment = createTestResource("EQUIP001", "X-Ray Machine", ResourceType.EQUIPMENT);
        equipment.setCostPerHour(100.0);
        
        Resource staff = createTestResource("STAFF001", "Dr. Smith", ResourceType.STAFF);
        staff.setCostPerHour(150.0);
        
        resourceDAO.save(room);
        resourceDAO.save(equipment);
        resourceDAO.save(staff);
        
        List<Resource> all = resourceDAO.findAll();
        assertEquals(3, all.size());
        
        Resource savedRoom = resourceDAO.findById("ROOM001");
        assertEquals(ResourceType.ROOM, savedRoom.getType());
        assertEquals(25.0, savedRoom.getCostPerHour());
        
        Resource savedEquipment = resourceDAO.findById("EQUIP001");
        assertEquals(ResourceType.EQUIPMENT, savedEquipment.getType());
        assertEquals(100.0, savedEquipment.getCostPerHour());
        
        Resource savedStaff = resourceDAO.findById("STAFF001");
        assertEquals(ResourceType.STAFF, savedStaff.getType());
        assertEquals(150.0, savedStaff.getCostPerHour());
    }

    @Test
    @DisplayName("Test resource with conflicts")
    void testResourceWithConflicts() throws Exception {
        Resource resource = createTestResource("RES012", "Conflicting Resource", ResourceType.ROOM);
        resource.setConflicts(Set.of("RES013", "RES014"));
        
        resourceDAO.save(resource);
        
        Resource saved = resourceDAO.findById("RES012");
        assertNotNull(saved, "Saved resource should not be null");
        assertEquals(2, saved.getConflicts().size());
        assertTrue(saved.getConflicts().contains("RES013"));
        assertTrue(saved.getConflicts().contains("RES014"));
    }

    @Test
    @DisplayName("Test resource with all fields")
    void testResourceWithAllFields() throws Exception {
        Resource resource = createTestResource("RES013", "Complete Resource", ResourceType.ROOM);
        // Description not available in Resource model
        resource.setCapabilities(Set.of("room", "wifi", "projector"));
        resource.setConflicts(Set.of("RES014"));
        resource.setCostPerHour(75.0);
        
        LocalDateTime now = LocalDateTime.now();
        resource.setAvailableFrom(now.plusHours(1));
        resource.setAvailableTo(now.plusHours(8));
        
        resourceDAO.save(resource);
        
        Resource saved = resourceDAO.findById("RES013");
        assertNotNull(saved, "Saved resource should not be null");
        assertEquals("Complete Resource", saved.getName());
        // Description not available in Resource model
        assertEquals(3, saved.getCapabilities().size());
        assertEquals(1, saved.getConflicts().size());
        assertEquals(75.0, saved.getCostPerHour());
        assertNotNull(saved.getAvailableFrom());
        assertNotNull(saved.getAvailableTo());
    }

    private Resource createTestResource(String id, String name, ResourceType type) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setName(name);
        resource.setType(type);
        resource.setCostPerHour(50.0);
        // Description not available in Resource model
        resource.setCapabilities(Set.of());
        resource.setConflicts(Set.of());
        return resource;
    }
}
