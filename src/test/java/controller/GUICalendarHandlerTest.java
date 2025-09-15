package controller;

import model.CalendarManager;
import model.ICalendarManager;
import model.IEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import view.IView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


/**
 * Unit tests for the GUICalendarHandler class.
 */
public class GUICalendarHandlerTest {
  private GUICalendarHandler handler;
  private ICalendarManager cm;
  private MockView mockView;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  private static class MockView implements IView {
    private int updateDisplayCount = 0;
    private boolean interactiveModeStarted = false;

    @Override
    public void updateDisplay() {
      updateDisplayCount++;
    }

    @Override
    public void startInteractiveMode() {
      interactiveModeStarted = true;
    }

    @Override
    public void displayMessage(String message) {
      // No-op for this test
    }

    public int getUpdateDisplayCount() {
      return updateDisplayCount;
    }

    public boolean isInteractiveModeStarted() {
      return interactiveModeStarted;
    }
  }

  @Before
  public void setUp() {
    cm = new CalendarManager();
    mockView = new MockView();
    handler = new GUICalendarHandler(cm, mockView);
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  public void testProcessInputGuiMode() {
    handler.processInput(new StringReader(""), "gui");
    assertTrue(mockView.isInteractiveModeStarted());
    assertEquals(0, mockView.getUpdateDisplayCount());
  }

  @Test
  public void testProcessInputNonGuiMode() {
    String input = "create calendar --name Test --timezone UTC\nexit\n";
    handler.processInput(new StringReader(input), "headless");
    String output = outContent.toString();
    assertTrue(output.contains("Processing headless input."));
    assertTrue(output.contains("Calendar 'Test' created"));
    assertFalse(mockView.isInteractiveModeStarted());
  }

  @Test
  public void testCreateCalendar() throws Exception {
    handler.createCalendar("Work", "UTC");
    assertEquals("Work", cm.getCalendar("Work").getName());
    assertEquals(1, mockView.getUpdateDisplayCount());
  }

  @Test(expected = Exception.class)
  public void testCreateCalendarInvalidTimezone() throws Exception {
    handler.createCalendar("BadTZ", "Invalid/Timezone");
  }

  @Test
  public void testCreateSingleEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createSingleEvent("Lunch", "2025-04-10T12:00", "2025-04-10T13:00");
    List<IEvent> events = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents();
    assertEquals(1, events.size());
    assertEquals("Lunch", events.get(0).getEventName());
    assertEquals(2, mockView.getUpdateDisplayCount());
  }

  @Test(expected = Exception.class)
  public void testCreateSingleEventNoCalendar() throws Exception {
    handler.createSingleEvent("NoCal", "2025-04-10T12:00", "2025-04-10T13:00");
  }

  @Test
  public void testCreateRecurringEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createRecurringEvent("Weekly", "2025-04-07T09:00",
            "2025-04-07T10:00", "M for 3 times");
    List<IEvent> events = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents();
    assertEquals(3, events.size());
    assertEquals("Weekly", events.get(0).getEventName());
    assertEquals(2, mockView.getUpdateDisplayCount());
  }

  @Test(expected = Exception.class)
  public void testCreateRecurringEventInvalidRule() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createRecurringEvent("Bad", "2025-04-07T09:00",
            "2025-04-07T10:00", "X for 3 times");
  }

  @Test
  public void testEditSingleEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createSingleEvent("Meeting", "2025-04-09T09:00",
            "2025-04-09T10:00");
    handler.editSingleEvent("name", "Meeting", "2025-04-09T09:00",
            "2025-04-09T10:00", "NewMeeting");
    List<IEvent> events = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents();
    assertEquals("NewMeeting", events.get(0).getEventName());
    assertEquals(3, mockView.getUpdateDisplayCount());
  }

  @Test(expected = Exception.class)
  public void testEditSingleEventNotFound() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.editSingleEvent("name", "Nope", "2025-04-09T09:00",
            "2025-04-09T10:00", "NewName");
  }

  @Test
  public void testEditMultipleEvents() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createRecurringEvent("Weekly", "2025-04-07T09:00",
            "2025-04-07T10:00", "M for 3 times");
    handler.editMultipleEvents("description", "Weekly",
            "2025-04-07T09:00", "Team Sync");
    List<IEvent> events = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents();
    assertEquals("Team Sync", events.get(0).getDescription());
    assertEquals(3, mockView.getUpdateDisplayCount());
  }

  @Test(expected = Exception.class)
  public void testEditMultipleEventsNoMatches() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.editMultipleEvents("description", "Nope",
            "2025-04-07T09:00", "Nothing");
  }

  @Test
  public void testExportCalendar() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createSingleEvent("Event", "2025-04-09T09:00", "2025-04-09T10:00");
    File tempFile = File.createTempFile("test", ".csv");
    String filePath = tempFile.getAbsolutePath();
    handler.exportCalendar(filePath);
    assertEquals(2, mockView.getUpdateDisplayCount());
    String fileContent = new String(Files.readAllBytes(tempFile.toPath()));
    assertTrue("File should contain event name", fileContent.contains("Event"));
    assertTrue("File should contain start date", fileContent.contains("04/09/2025"));
    assertTrue("File should contain start time", fileContent.contains("09:00 AM"));
  }

  @Test
  public void testImportCalendarFullDayEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    String csvContent = "Name,Start Date,Start Time,End Date,End Time,Full "
            + "Day,Description,Location,Private\n"
            + "FullDayMeeting,04/09/2025,,04/09/2025,,true,All day event,Office,true";
    File tempFile = File.createTempFile("test", ".csv");
    try (PrintWriter writer = new PrintWriter(tempFile)) {
      writer.write(csvContent);
    }
    handler.importCalendar(tempFile.getAbsolutePath());
    List<IEvent> events = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents();
    assertEquals(1, events.size());
    IEvent event = events.get(0);
    assertEquals("FullDayMeeting", event.getEventName());
    assertEquals("All day event", event.getDescription());
    assertEquals("Office", event.getLocation());
    assertFalse("Event should be private", event.isPublic());
    assertTrue("Event should be full day", event.isFullDay());
    assertEquals(2, mockView.getUpdateDisplayCount());
  }

  @Test
  public void testImportCalendarNonFullDayEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    String csvContent = "Name,Start Date,Start Time,End Date,End Time,Full "
            + "Day,Description,Location,Private\n"
            + "Meeting,04/09/2025,09:00 AM,04/09/2025,10:00 AM,false,Team meeting,Office,false";
    File tempFile = File.createTempFile("test", ".csv");
    try (PrintWriter writer = new PrintWriter(tempFile)) {
      writer.write(csvContent);
    }
    handler.importCalendar(tempFile.getAbsolutePath());
    List<IEvent> events = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents();
    assertEquals(1, events.size());
    IEvent event = events.get(0);
    assertEquals("Meeting", event.getEventName());
    assertEquals("Team meeting", event.getDescription());
    assertEquals("Office", event.getLocation());
    assertTrue("Event should be public", event.isPublic());
    assertFalse("Event should not be full day", event.isFullDay());
    assertEquals(2, mockView.getUpdateDisplayCount());
  }

  @Test
  public void testImportCalendarShortLine() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    String csvContent = "Name,Start Date\nShort,04/09/2025";
    File tempFile = File.createTempFile("test", ".csv");
    try (PrintWriter writer = new PrintWriter(tempFile)) {
      writer.write(csvContent);
    }
    handler.importCalendar(tempFile.getAbsolutePath());
    assertEquals(0, cm.getCurrentCalendar()
            .getEventScheduler().retrieveAllEvents().size());
  }
}