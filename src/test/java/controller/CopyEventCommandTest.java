package controller;

import model.CalendarManager;
import model.IEvent;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the CopyEventCommand class.
 */
public class CopyEventCommandTest {
  private CalendarManager cm;
  private CopyEventCommand command;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("Family", ZoneId.of("America/New_York"));
    cm.createCalendar("WorkCal", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("Family");
    CreateEventCommand createCommand = new CreateEventCommand(cm);
    createCommand.execute("create event Party from 2025-03-24T14:00 to 2025-03-24T16:00",
            cm, "interactive");
    cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents().get(0).setDescription("Family "
            + "gathering");
    cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents().get(0).setLocation("Home");
    cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents().get(0).setPublic(false);
    command = new CopyEventCommand(cm);
  }

  @Test
  public void testExecuteValid() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy event Party on 2025-03-24T14:00 --target WorkCal "
              + "to 2025-03-25T09:00", cm, "interactive");
      assertEquals(1, cm.getCalendar("WorkCal").getEventScheduler()
              .retrieveAllEvents().size());
      IEvent copiedEvent = cm.getCalendar("WorkCal").getEventScheduler()
              .retrieveAllEvents().get(0);
      assertEquals("Family gathering", copiedEvent.getDescription());
      assertEquals("Home", copiedEvent.getLocation());
      assertFalse(copiedEvent.isPublic());
      assertTrue(outContent.toString().contains("Event 'Party' copied "
              + "to WorkCal at 2025-03-25T09:00"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteEventNotFound() {
    try {
      command.execute("copy event None on 2025-03-24T14:00 "
              + "--target WorkCal to 2025-03-25T09:00", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Event 'None' not found"));
    }
  }

  @Test
  public void testExecuteMissingTarget() {
    try {
      command.execute("copy event Party on 2025-03-24T14:00", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Missing --target"));
    }
  }

  @Test
  public void testExecuteInvalidDate() {
    try {
      command.execute("copy event Party on invalid --target WorkCal "
              + "to 2025-03-25T09:00", cm, "interactive");
      fail("Expected DateTimeParseException due to invalid date format");
    } catch (DateTimeParseException e) {
      assertTrue("Exception message should indicate parsing failure",
              e.getMessage().contains("could not be parsed"));
    } catch (Exception e) {
      fail("Unexpected exception type: " + e.getClass().getName()
              + " with message: " + e.getMessage());
    }
  }

  @Test
  public void testCopyToSameCalendar() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("Work");
    new CreateEventCommand(cm).execute("create event Test from "
                    + "2025-03-24T09:00 to 2025-03-24T10:00",
            cm, "interactive");
    command.execute("copy event Test on 2025-03-24T09:00 "
                    + "--target Work to 2025-03-24T11:00",
            cm, "interactive");
    assertEquals(2,
            cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents().size());
  }

  @Test
  public void testCopyWithConflict() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.createCalendar("Target", ZoneId.of("UTC"));
    cm.setCurrentCalendar("Work");
    new CreateEventCommand(cm).execute("create event Test "
                    + "from 2025-03-24T09:00 to 2025-03-24T10:00",
            cm, "interactive");
    cm.setCurrentCalendar("Target");
    new CreateEventCommand(cm).execute("create event Block "
                    + "from 2025-03-24T09:30 to 2025-03-24T10:30",
            cm, "interactive");
    cm.setCurrentCalendar("Work");
    try {
      command.execute("copy event Test on 2025-03-24T09:00 "
                      + "--target Target to 2025-03-24T09:00",
              cm, "interactive");
      fail("Expected conflict exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Conflict with existing event"));
    }
  }

  @Test
  public void testCopyWithConflictOnTarget() throws Exception {
    cm.createCalendar("Target", ZoneId.of("UTC"));
    cm.setCurrentCalendar("Family");
    new CreateEventCommand(cm).execute("create event Block "
                    + "from 2025-03-25T09:00 to 2025-03-25T10:00",
            cm, "interactive");
    cm.setCurrentCalendar("Target");
    new CreateEventCommand(cm).execute("create event Existing "
                    + "from 2025-03-25T09:30 to 2025-03-25T10:30",
            cm, "interactive");
    cm.setCurrentCalendar("Family");
    try {
      command.execute("copy event Party on 2025-03-24T14:00 "
                      + "--target Target to 2025-03-25T09:00",
              cm, "interactive");
      fail("Expected conflict exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Conflict with existing event"));
      assertEquals(1,
              cm.getCalendar("Target").getEventScheduler().retrieveAllEvents().size());
    }
  }

  @Test
  public void testCopyAcrossTimezones() throws Exception {
    cm.createCalendar("Target", ZoneId.of("Pacific/Kiritimati"));
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy event Party on 2025-03-24T14:00 "
                      + "--target Target to 2025-03-25T09:00",
              cm, "interactive");
      IEvent copiedEvent =
              cm.getCalendar("Target").getEventScheduler().retrieveAllEvents().get(0);
      assertEquals(ZonedDateTime.parse("2025-03-25T09:00+14:00[Pacific/Kiritimati]"),
              copiedEvent.getStart());
      assertEquals(ZonedDateTime.parse("2025-03-25T11:00+14:00[Pacific/Kiritimati]"),
              copiedEvent.getEnd());
      assertTrue(outContent.toString().contains("Event 'Party' "
              + "copied to Target at 2025-03-25T09:00"));
    } finally {
      System.setOut(originalOut);
    }
  }
}