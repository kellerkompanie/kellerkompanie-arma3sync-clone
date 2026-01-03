package fr.soe.a3s.service;

import fr.soe.a3s.dao.repository.RepositoryDAO;
import fr.soe.a3s.domain.repository.Events;
import fr.soe.a3s.domain.repository.Event;
import fr.soe.a3s.domain.repository.Repository;
import fr.soe.a3s.domain.Ftp;
import fr.soe.a3s.dto.EventDTO;
import fr.soe.a3s.constant.ProtocolType;
import fr.soe.a3s.exception.WritingException;
import fr.soe.a3s.exception.repository.RepositoryException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for modset (Event) functionality in RepositoryService.
 */
class RepositoryServiceModsetTest {

    private RepositoryService repositoryService;
    private RepositoryDAO repositoryDAO;
    private Repository testRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repositoryService = new RepositoryService();
        repositoryDAO = new RepositoryDAO();

        // Create a test repository with a temp path
        Ftp protocol = new Ftp("localhost", "21", "anonymous", "", ProtocolType.FTP);
        testRepository = new Repository("TestRepo", protocol);
        testRepository.setPath(tempDir.toString());

        // Add repository to the DAO map
        repositoryDAO.add(testRepository);
    }

    @AfterEach
    void tearDown() {
        // Clear the repository map
        repositoryDAO.getMap().clear();
    }

    @Test
    @DisplayName("getEvents returns empty list when no events exist")
    void getEvents_NoEvents_ReturnsEmptyList() {
        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    @DisplayName("addEvent creates a new event in repository")
    void addEvent_NewEvent_EventIsAdded() {
        EventDTO newEvent = new EventDTO();
        newEvent.setName("TestModset");
        newEvent.setDescription("Test description");
        newEvent.setRepositoryName("TestRepo");

        repositoryService.addEvent("TestRepo", newEvent);

        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(1, events.size());
        assertEquals("TestModset", events.get(0).getName());
    }

    @Test
    @DisplayName("addEvent initializes Events container when null")
    void addEvent_NullEventsContainer_InitializesContainer() {
        assertNull(testRepository.getEvents());

        EventDTO newEvent = new EventDTO();
        newEvent.setName("TestModset");
        repositoryService.addEvent("TestRepo", newEvent);

        assertNotNull(testRepository.getEvents());
        assertEquals(1, testRepository.getEvents().getList().size());
    }

    @Test
    @DisplayName("getEvents returns correct event details")
    void getEvents_WithEvents_ReturnsCorrectDetails() {
        EventDTO newEvent = new EventDTO();
        newEvent.setName("MyModset");
        newEvent.setDescription("My description");
        newEvent.getAddonNames().put("@ace", false);  // required
        newEvent.getAddonNames().put("@tfar", true);  // optional

        repositoryService.addEvent("TestRepo", newEvent);

        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(1, events.size());

        EventDTO retrieved = events.get(0);
        assertEquals("MyModset", retrieved.getName());
        assertEquals("My description", retrieved.getDescription());
        assertEquals(2, retrieved.getAddonNames().size());
        assertEquals(false, retrieved.getAddonNames().get("@ace"));
        assertEquals(true, retrieved.getAddonNames().get("@tfar"));
    }

    @Test
    @DisplayName("removeEvent removes existing event")
    void removeEvent_ExistingEvent_EventIsRemoved() throws RepositoryException {
        // Add event first
        EventDTO newEvent = new EventDTO();
        newEvent.setName("ToBeDeleted");
        repositoryService.addEvent("TestRepo", newEvent);

        assertEquals(1, repositoryService.getEvents("TestRepo").size());

        // Remove it
        repositoryService.removeEvent("TestRepo", "ToBeDeleted");

        assertEquals(0, repositoryService.getEvents("TestRepo").size());
    }

    @Test
    @DisplayName("renameEvent updates event name and description")
    void renameEvent_ExistingEvent_NameAndDescriptionUpdated() throws RepositoryException {
        // Add event first
        EventDTO newEvent = new EventDTO();
        newEvent.setName("OldName");
        newEvent.setDescription("Old description");
        repositoryService.addEvent("TestRepo", newEvent);

        // Rename it
        repositoryService.renameEvent("TestRepo", "OldName", "NewName", "New description");

        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(1, events.size());
        assertEquals("NewName", events.get(0).getName());
        assertEquals("New description", events.get(0).getDescription());
    }

    @Test
    @DisplayName("saveEvent updates addon list")
    void saveEvent_UpdateAddons_AddonsAreUpdated() {
        // Add event first
        EventDTO newEvent = new EventDTO();
        newEvent.setName("TestModset");
        repositoryService.addEvent("TestRepo", newEvent);

        // Modify and save
        EventDTO updatedEvent = repositoryService.getEvents("TestRepo").get(0);
        updatedEvent.getAddonNames().put("@ace", false);
        updatedEvent.getAddonNames().put("@cba_a3", false);
        repositoryService.saveEvent("TestRepo", updatedEvent);

        // Verify
        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(2, events.get(0).getAddonNames().size());
        assertTrue(events.get(0).getAddonNames().containsKey("@ace"));
        assertTrue(events.get(0).getAddonNames().containsKey("@cba_a3"));
    }

    @Test
    @DisplayName("duplicateEvent creates copy with suffix")
    void duplicateEvent_ExistingEvent_CopyCreated() throws Exception {
        // Add event first
        EventDTO newEvent = new EventDTO();
        newEvent.setName("Original");
        newEvent.setDescription("Original description");
        newEvent.getAddonNames().put("@ace", false);
        repositoryService.addEvent("TestRepo", newEvent);

        // Duplicate it
        repositoryService.duplicateEvent("TestRepo", "Original");

        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(2, events.size());

        // Find the duplicate
        EventDTO duplicate = events.stream()
            .filter(e -> e.getName().contains("duplicate"))
            .findFirst()
            .orElse(null);

        assertNotNull(duplicate);
        assertEquals("Original - duplicate", duplicate.getName());
        assertEquals("Original description", duplicate.getDescription());
        assertEquals(1, duplicate.getAddonNames().size());
    }

    @Test
    @DisplayName("addEvent with non-existent repository does nothing")
    void addEvent_NonExistentRepository_NoException() {
        EventDTO newEvent = new EventDTO();
        newEvent.setName("TestModset");

        // Should not throw exception
        assertDoesNotThrow(() -> repositoryService.addEvent("NonExistent", newEvent));
    }

    @Test
    @DisplayName("Multiple events can be added to same repository")
    void addEvent_MultipleEvents_AllAdded() {
        for (int i = 1; i <= 5; i++) {
            EventDTO event = new EventDTO();
            event.setName("Modset" + i);
            repositoryService.addEvent("TestRepo", event);
        }

        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(5, events.size());
    }

    @Test
    @DisplayName("writeEvents creates events file in repository path")
    void writeEvents_WithEvents_FileCreated() throws WritingException {
        // Add event
        EventDTO newEvent = new EventDTO();
        newEvent.setName("TestModset");
        repositoryService.addEvent("TestRepo", newEvent);

        // Write events
        repositoryService.writeEvents("TestRepo");

        // Verify file exists
        File a3sFolder = new File(tempDir.toFile(), ".a3s");
        File eventsFile = new File(a3sFolder, "events");
        assertTrue(eventsFile.exists(), "Events file should exist after writeEvents");
    }

    @Test
    @DisplayName("loadEventsFromRepositoryPath loads previously saved events")
    void loadEventsFromRepositoryPath_AfterWrite_EventsLoaded() throws WritingException {
        // Add and write event
        EventDTO newEvent = new EventDTO();
        newEvent.setName("PersistedModset");
        newEvent.setDescription("Persisted description");
        repositoryService.addEvent("TestRepo", newEvent);
        repositoryService.writeEvents("TestRepo");

        // Clear events from memory
        testRepository.setEvents(null);
        assertEquals(0, repositoryService.getEvents("TestRepo").size());

        // Load from file
        repositoryService.loadEventsFromRepositoryPath("TestRepo");

        // Verify events are loaded
        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(1, events.size());
        assertEquals("PersistedModset", events.get(0).getName());
        assertEquals("Persisted description", events.get(0).getDescription());
    }

    @Test
    @DisplayName("loadEventsFromRepositoryPath with no path does nothing")
    void loadEventsFromRepositoryPath_NoPath_NoException() {
        testRepository.setPath(null);

        // Should not throw exception
        assertDoesNotThrow(() -> repositoryService.loadEventsFromRepositoryPath("TestRepo"));
    }

    @Test
    @DisplayName("Event addon optional flag is preserved")
    void addEvent_WithOptionalAddons_FlagPreserved() throws WritingException {
        EventDTO newEvent = new EventDTO();
        newEvent.setName("TestModset");
        newEvent.getAddonNames().put("@required_addon", false);
        newEvent.getAddonNames().put("@optional_addon", true);
        repositoryService.addEvent("TestRepo", newEvent);
        repositoryService.writeEvents("TestRepo");

        // Clear and reload
        testRepository.setEvents(null);
        repositoryService.loadEventsFromRepositoryPath("TestRepo");

        List<EventDTO> events = repositoryService.getEvents("TestRepo");
        assertEquals(false, events.get(0).getAddonNames().get("@required_addon"));
        assertEquals(true, events.get(0).getAddonNames().get("@optional_addon"));
    }
}
