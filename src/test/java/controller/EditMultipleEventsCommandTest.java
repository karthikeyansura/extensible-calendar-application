package controller;

import model.CalendarManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.security.Permission;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the EditMultipleEventsCommand class.
 */
public class EditMultipleEventsCommandTest {
  private CalendarManager cm;
  private EditMultipleEventsCommand command;
  private java.io.ByteArrayOutputStream outContent;
  private java.io.PrintStream originalOut;
  private SecurityManager originalSecurityManager;

  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // No-op: Allow all permissions except exit to proceed without interference
    }

    @Override
    public void checkExit(int status) {
      throw new SecurityException("System.exit(" + status + ") attempted");
    }
  }

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("Family", ZoneId.of("America/New_York"));
    cm.setCurrentCalendar("Family");
    new CreateEventCommand(cm).execute("create event meet from 2025-03-19T09:00 "
            + "to 2025-03-19T10:00", cm, "interactive");
    new CreateEventCommand(cm).execute("create event meet from 2025-03-20T09:00 "
            + "to 2025-03-20T10:00", cm, "interactive");
    command = new EditMultipleEventsCommand(cm);
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
  public void testExecuteFromDate() throws Exception {
    command.execute("edit events description meet from 2025-03-19T09:00 with Karthik",
            cm, "interactive");
    assertEquals("Karthik",
            cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents()
                    .get(0).getDescription());
    assertEquals("Karthik",
            cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents()
                    .get(1).getDescription());
    String output = outContent.toString();
    assertTrue("Should confirm update",
            output.contains("2 event(s) property \"description\" updated with \"Karthik\""));
  }

  @Test
  public void testExecuteAllEvents() throws Exception {
    command.execute("edit events description meet Karthik", cm, "interactive");
    assertEquals(2,
            cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents().stream()
            .filter(e -> "Karthik".equals(e.getDescription())).count());
    String output = outContent.toString();
    assertTrue("Should confirm update",
            output.contains("2 event(s) property \"description\" updated with \"Karthik\""));
  }

  @Test
  public void testExecuteNoEventsHeadless() {
    try {
      command.execute("edit events description none Karthik", cm, "headless");
      fail("Expected exception due to no events found in headless mode");
    } catch (Exception e) {
      assertEquals("Event not found", e.getMessage());
      String output = outContent.toString();
      assertFalse("Should not report updates",
              output.contains("event(s) property \"description\" updated with \"Karthik\""));
    }
  }

  @Test
  public void testExecuteNoEventName() {
    try {
      command.execute("edit events description with Karthik", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Event not found", e.getMessage());
      String output = outContent.toString();
      assertFalse("Should not report updates",
              output.contains("event(s) property \"description\" updated with \"Karthik\""));
    }
  }
}