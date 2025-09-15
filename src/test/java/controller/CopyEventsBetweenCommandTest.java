package controller;

import model.CalendarManager;
import model.IEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the CopyEventsBetweenCommandTest class.
 */
public class CopyEventsBetweenCommandTest {
  private CalendarManager cm;
  private CopyEventsBetweenCommand command;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("Family", ZoneId.of("America/New_York"));
    cm.setCurrentCalendar("Family");
    command = new CopyEventsBetweenCommand(cm);
  }

  @Test
  public void testExecuteValid() throws Exception {
    new CreateEventCommand(cm).execute("create event Party "
                    + "from 2025-03-24T14:00 to 2025-03-24T16:00",
            cm, "interactive");
    IEvent sourceEvent = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents().get(0);
    sourceEvent.setDescription("Birthday party");
    sourceEvent.setLocation("Park");
    sourceEvent.setPublic(false);
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy events between 2025-03-23 and 2025-03-24 "
                      + "--target Family to 2025-04-08",
              cm, "interactive");
      List<IEvent> targetEvents =
              cm.getCalendar("Family").getEventScheduler().retrieveAllEvents();
      assertEquals(2, targetEvents.size());
      IEvent copiedEvent = targetEvents.get(1);
      assertEquals("Birthday party", copiedEvent.getDescription());
      assertEquals("Park", copiedEvent.getLocation());
      assertFalse(copiedEvent.isPublic());
      String output = outContent.toString().trim();
      assertEquals("1 events copied from 2025-03-23 to "
              + "2025-03-24 to Family on 2025-04-08", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteMultipleEvents() throws Exception {
    new CreateEventCommand(cm).execute("create event Party "
                    + "from 2025-03-24T14:00 to 2025-03-24T16:00",
            cm, "interactive");
    new CreateEventCommand(cm).execute("create event Meeting "
                    + "from 2025-03-23T10:00 to 2025-03-23T11:00",
            cm, "interactive");
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy events between 2025-03-23 and "
                      + "2025-03-24 --target Family to 2025-04-08",
              cm, "interactive");
      List<IEvent> targetEvents =
              cm.getCalendar("Family").getEventScheduler().retrieveAllEvents();
      assertEquals(4, targetEvents.size());
      String output = outContent.toString().trim();
      assertEquals("2 events copied from 2025-03-23 to 2025-03-24 to "
              + "Family on 2025-04-08", output);
      int copiedCount = Integer.parseInt(output.split(" ")[0]);
      assertEquals(2, copiedCount);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteNoEvents() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy events between 2025-03-01 and "
                      + "2025-03-02 --target Family to 2025-04-08",
              cm, "interactive");
      List<IEvent> targetEvents = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents();
      assertEquals(0, targetEvents.size());
      String output = outContent.toString().trim();
      assertEquals("0 events copied from 2025-03-01 to "
              + "2025-03-02 to Family on 2025-04-08", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteInvalidDate() {
    try {
      command.execute("copy events between invalid "
                      + "and 2025-03-24 --target Family to 2025-04-08",
              cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Invalid date format"));
    }
  }

  @Test
  public void testCopyAcrossExtremeTimezones() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("Work");
    new CreateEventCommand(cm).execute("create event Test "
                    + "from 2025-03-24T09:00 to 2025-03-24T10:00",
            cm, "interactive");
    cm.createCalendar("Target", ZoneId.of("Pacific/Kiritimati"));
    command.execute("copy events between 2025-03-24 and 2025-03-24 "
                    + "--target Target to 2025-03-25",
            cm, "interactive");
    IEvent copied = cm.getCalendar("Target").getEventScheduler().retrieveAllEvents().get(0);
    assertEquals(ZoneId.of("Pacific/Kiritimati"), copied.getStart().getZone());
  }

  @Test
  public void testCopyOverlappingEventsInRangeWithConflict() throws Exception {
    new CreateEventCommand(cm).execute("create event "
                    + "Event1 from 2025-03-24T14:00 to 2025-03-24T15:00",
            cm, "interactive");
    new CreateEventCommand(cm).execute("create event Event2 "
                    + "from 2025-03-24T15:00 to 2025-03-24T16:00",
            cm, "interactive");
    new CreateEventCommand(cm).execute("create event Blocker "
                    + "from 2025-04-08T14:30 to 2025-04-08T15:30",
            cm, "interactive");
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy events between 2025-03-24 and 2025-03-24 "
                      + "--target Family to 2025-04-08",
              cm, "interactive");
      fail("Expected conflict exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Conflict with existing event"));
      List<IEvent> targetEvents =
              cm.getCalendar("Family").getEventScheduler().retrieveAllEvents();
      assertEquals(3, targetEvents.size());
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testCopyNonOverlappingEventsInRange() throws Exception {
    new CreateEventCommand(cm).execute("create event"
                    + " Event1 from 2025-03-24T09:00 to 2025-03-24T10:00",
            cm, "interactive");
    new CreateEventCommand(cm).execute("create event Event2 from "
                    + "2025-03-24T11:00 to 2025-03-24T12:00",
            cm, "interactive");
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy events between 2025-03-24 and "
                      + "2025-03-24 --target Family to 2025-04-08",
              cm, "interactive");
      List<IEvent> targetEvents =
              cm.getCalendar("Family").getEventScheduler().retrieveAllEvents();
      assertEquals(4, targetEvents.size());
      assertTrue(targetEvents.stream().anyMatch(e -> e.getEventName().equals("Event1")));
      assertTrue(targetEvents.stream().anyMatch(e -> e.getEventName().equals("Event2")));
      String output = outContent.toString().trim();
      assertEquals("2 events copied from 2025-03-24 "
              + "to 2025-03-24 to Family on 2025-04-08", output);
    } finally {
      System.setOut(originalOut);
    }
  }
}