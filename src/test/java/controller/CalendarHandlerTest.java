package controller;

import model.CalendarManager;
import model.ICalendarManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.security.Permission;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the CalendarHandler class.
 */
public class CalendarHandlerTest {
  private CalendarHandler handler;
  private ICalendarManager cm;
  private java.io.ByteArrayOutputStream outContent;
  private java.io.PrintStream originalOut;
  private SecurityManager originalSecurityManager;

  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // Allow all permissions except exit
    }

    @Override
    public void checkExit(int status) {
      throw new SecurityException("System.exit(" + status + ") attempted");
    }
  }

  @Before
  public void setUp() {
    cm = new CalendarManager();
    handler = new CalendarHandler(cm);
    outContent = new java.io.ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new java.io.PrintStream(outContent));
    originalSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setSecurityManager(originalSecurityManager);
  }

  @Test
  public void testProcessInputInteractiveEmpty() {
    String input = "\n\nexit\n";
    handler.processInput(new StringReader(input), "interactive");
    String output = outContent.toString();
    assertTrue(output.contains("Processing interactive input. Type 'exit' to stop."));
    assertTrue(output.contains("> "));
    assertTrue(output.contains("Error: Empty Line, ignored"));
    assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testProcessInputHeadlessMultipleCommands() {
    String input = "create calendar --name Test --timezone America/New_York\n"
            +
            "invalid command\n" +
            "exit\n";
    try {
      handler.processInput(new StringReader(input), "headless");
      fail("Expected SecurityException due to headless mode exiting on error");
    } catch (SecurityException e) {
      String output = outContent.toString();
      assertTrue(output.contains("Processing headless input."));
      assertTrue(output.contains("Calendar 'Test' created"));
      assertTrue(output.contains("Error: Unknown command 'invalid command'"));
      assertEquals("System.exit(1) attempted", e.getMessage());
    }
  }

  @Test
  public void testProcessInputInteractiveIgnoresFailure() {
    String input = "invalid command\n" +
            "create calendar --name Work --timezone Asia/Kolkata\n"
            +
            "exit\n";
    handler.processInput(new StringReader(input), "interactive");
    String output = outContent.toString();
    assertTrue(output.contains("Processing interactive input"));
    assertTrue(output.contains("Error: Unknown command 'invalid command'"));
    assertTrue(output.contains("Calendar 'Work' created"));
    assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testProcessInputInvalidMode() {
    String input = "exit\n";
    handler.processInput(new StringReader(input), "custom");
    String output = outContent.toString();
    assertTrue(output.contains("Processing custom input."));
    assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testProcessScript() throws Exception {
    String script = "create calendar --name Work --timezone Asia/Kolkata\n"
            +
            "use calendar --name Work\n"
            +
            "create event Meeting from 2025-03-24T09:00 to 2025-03-24T10:00";
    handler.processInput(new StringReader(script), "headless");
    String output = outContent.toString();
    assertTrue(output.contains("Processing headless input."));
    assertTrue(output.contains("Calendar 'Work' created"));
    assertTrue(output.contains("Event created"));
    assertEquals(1, cm.getCurrentCalendar().getEventScheduler()
            .retrieveAllEvents().size());
  }

  @Test
  public void testCreateCalendar() throws Exception {
    handler.createCalendar("Home", "UTC");
    String output = outContent.toString();
    assertTrue(output.contains("Calendar 'Home' created"));
    assertEquals("Home", cm.getCalendar("Home").getName());
    assertEquals("UTC", cm.getCalendar("Home").getTimezone().toString());
  }

  @Test(expected = Exception.class)
  public void testCreateCalendarInvalidTimezone() throws Exception {
    handler.createCalendar("BadTZ", "Invalid/Timezone");
  }

  @Test
  public void testCreateSingleEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createSingleEvent("Lunch", "2025-04-10T12:00",
            "2025-04-10T13:00");
    String output = outContent.toString();
    assertTrue(output.contains("Event created: Lunch from 2025-04-10T12:00[UTC] "
            + "to 2025-04-10T13:00[UTC]"));
    assertEquals(1, cm.getCurrentCalendar().getEventScheduler()
            .retrieveAllEvents().size());
  }

  @Test(expected = Exception.class)
  public void testCreateSingleEventNoCalendar() throws Exception {
    handler.createSingleEvent("NoCalEvent", "2025-04-10T12:00",
            "2025-04-10T13:00");
  }

  @Test
  public void testCreateRecurringEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createRecurringEvent("Weekly", "2025-04-07T09:00",
            "2025-04-07T10:00", "M for 3 times");
    String output = outContent.toString();
    assertTrue(output.contains("Recurring event created: 3 instances"));
    assertEquals(3, cm.getCurrentCalendar().getEventScheduler()
            .retrieveAllEvents().size());
  }

  @Test(expected = Exception.class)
  public void testCreateRecurringEventInvalidRule() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createRecurringEvent("BadRepeat", "2025-04-07T09:00",
            "2025-04-07T10:00", "X for 3 times");
  }

  @Test
  public void testEditSingleEvent() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createSingleEvent("Meeting", "2025-04-09T09:00",
            "2025-04-09T10:00");
    handler.editSingleEvent("name", "Meeting",
            "2025-04-09T09:00", "2025-04-09T10:00", "NewMeeting");
    String output = outContent.toString();
    assertTrue(output.contains("1 event(s) property \"name\" updated with \"NewMeeting\""));
    assertEquals("NewMeeting", cm.getCurrentCalendar().getEventScheduler()
            .retrieveAllEvents().get(0).getEventName());
  }

  @Test(expected = Exception.class)
  public void testEditSingleEventNonExistent() throws Exception {
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
    String output = outContent.toString();
    assertTrue(output.contains("3 event(s) property \"description\" updated with \"Team Sync\""));
    assertEquals("Team Sync", cm.getCurrentCalendar().getEventScheduler()
            .retrieveAllEvents().get(0).getDescription());
  }

  @Test
  public void testExportCalendar() throws Exception {
    handler.createCalendar("Test", "UTC");
    cm.setCurrentCalendar("Test");
    handler.createSingleEvent("Event", "2025-04-09T09:00", "2025-04-09T10:00");
    handler.exportCalendar("test.csv");
    String output = outContent.toString();
    assertTrue(output.contains("Exported"));
  }

  @Test(expected = Exception.class)
  public void testImportCalendarNotSupported() throws Exception {
    handler.importCalendar("test.csv");
  }
}