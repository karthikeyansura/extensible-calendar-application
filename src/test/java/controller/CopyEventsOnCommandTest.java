package controller;

import model.CalendarManager;
import model.IEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the CopyEventsOnCommand class.
 */
public class CopyEventsOnCommandTest {
  private CalendarManager cm;
  private CopyEventsOnCommand command;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("WorkCal", ZoneId.of("Asia/Kolkata"));
    cm.createCalendar("Family", ZoneId.of("America/New_York"));
    cm.setCurrentCalendar("WorkCal");
    CreateEventCommand createCommand = new CreateEventCommand(cm);
    createCommand.execute("create event Party from 2025-03-25T09:00 "
            + "to 2025-03-25T11:00", cm, "interactive");
    IEvent sourceEvent = cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents().get(0);
    sourceEvent.setDescription("Team meeting");
    sourceEvent.setLocation("Office");
    sourceEvent.setPublic(true);
    command = new CopyEventsOnCommand(cm);
  }

  @Test
  public void testExecuteValid() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy events on 2025-03-25 --target Family to 2025-03-26",
              cm, "interactive");
      LocalDate targetDate = LocalDate.parse("2025-03-25");
      List<IEvent> targetEvents = cm.getCalendar("Family")
              .getEventScheduler().fetchEventsStartingOnDate(targetDate);
      assertEquals(1, targetEvents.size());
      IEvent copiedEvent = targetEvents.get(0);
      assertEquals("Team meeting", copiedEvent.getDescription());
      assertEquals("Office", copiedEvent.getLocation());
      assertTrue(copiedEvent.isPublic());
      String output = outContent.toString().trim();
      String expectedOutput = "1 events copied from 2025-03-25 to Family on 2025-03-26";
      assertEquals(expectedOutput, output);
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
      command.execute("copy events on 2025-03-26 --target Family to 2025-03-27",
              cm, "interactive");
      LocalDate targetDate = LocalDate.parse("2025-03-27");
      assertEquals(0, cm.getCalendar("Family").getEventScheduler()
              .fetchEventsStartingOnDate(targetDate).size());
      String output = outContent.toString().trim();
      String expectedOutput = "0 events copied from 2025-03-26 to Family on 2025-03-27";
      assertEquals(expectedOutput, output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteInvalidDate() {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("copy events on invalid --target Family to 2025-03-27",
              cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Invalid date format"));
    } finally {
      System.setOut(originalOut);
    }
  }
}