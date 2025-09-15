package controller;

import model.Calendar;
import model.CalendarManager;
import model.IEvent;
import view.ConsoleWriter;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the CreateEventCommand class.
 */
public class CreateEventCommandTest {
  private CalendarManager cm;
  private CreateEventCommand command;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("Work");
    command = new CreateEventCommand(cm);
  }

  @Test
  public void testExecuteSingleEvent() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Meeting from 2025-03-24T09:00 to 2025-03-24T10:00",
              cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(1, cal.getEventScheduler().retrieveAllEvents().size());
      String output = outContent.toString().trim();
      assertTrue(output.contains("Event created: Meeting from 2025-03-24T09:00[Asia/Kolkata] "
              + "to 2025-03-24T10:00[Asia/Kolkata]"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteAllDayEvent() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Vacation on 2025-03-27", cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertTrue(cal.getEventScheduler().retrieveAllEvents().get(0).isFullDay());
      String output = outContent.toString().trim();
      assertTrue(output.contains("Event created: Vacation from 2025-03-27T00:00[Asia/Kolkata] "
              + "to 2025-03-28T00:00[Asia/Kolkata], Full Day"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteRecurringFor() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Weekly from 2025-03-25T14:00 to "
              + "2025-03-25T15:00 repeats T for 3 times", cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(3, cal.getEventScheduler().retrieveAllEvents().size());
      String output = outContent.toString().trim();
      assertEquals("Recurring event created: 3 instances", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteRecurringUntil() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Weekly from 2025-03-24T14:00 to "
              + "2025-03-24T15:00 repeats M until 2025-04-07T15:00", cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(3, cal.getEventScheduler().retrieveAllEvents().size());
      String output = outContent.toString().trim();
      assertEquals("Recurring event created: 3 instances", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteRecurringConflict() throws Exception {
    command.execute("create event Existing from 2025-03-25T14:00 to 2025-03-25T15:00",
            cm, "interactive");
    try {
      command.execute("create event Weekly from 2025-03-25T14:00 to 2025-03-25T15:00 "
              + "repeats T for 3 times", cm, "interactive");
      fail("Expected conflict exception");
    } catch (Exception e) {
      String expectedMessage = "Recurring event 'Weekly' conflicts with 'Existing' at"
              + " 2025-03-25T14:00+05:30[Asia/Kolkata]";
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public void testExecuteFromWithoutTo() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Short from 2025-03-24T09:00", cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(1, cal.getEventScheduler().retrieveAllEvents().size());
      assertTrue(cal.getEventScheduler().retrieveAllEvents().get(0).isFullDay());
      String output = outContent.toString().trim();
      assertTrue(output.contains("Event created: Short from 2025-03-24T09:00[Asia/Kolkata] "
              + "to 2025-03-25T00:00[Asia/Kolkata], Full Day"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteRecurringAllDay() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Holiday on 2025-03-25 repeats T for 3 times",
              cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(3, cal.getEventScheduler().retrieveAllEvents().size());
      assertTrue(cal.getEventScheduler().retrieveAllEvents().get(0).isFullDay());
      String output = outContent.toString().trim();
      assertEquals("Recurring all-day event created: 3 instances", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteRecurringAllDayConflict() throws Exception {
    command.execute("create event Existing on 2025-03-25", cm, "interactive");
    try {
      command.execute("create event Holiday on 2025-03-25 repeats T for 3 times",
              cm, "interactive");
      fail("Expected conflict exception for recurring all-day event");
    } catch (Exception e) {
      String expectedMessage = "Recurring event 'Holiday' conflicts with 'Existing' "
              + "at 2025-03-25T00:00+05:30[Asia/Kolkata]";
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public void testExecuteConflict() throws Exception {
    command.execute("create event Meeting from 2025-03-24T09:00 to 2025-03-24T10:00",
            cm, "interactive");
    try {
      command.execute("create event Overlap from 2025-03-24T09:30 to 2025-03-24T10:30",
              cm, "interactive");
      fail("Expected conflict exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Conflict with existing event"));
    }
  }

  @Test
  public void testExecuteInvalidRepeatRule() {
    try {
      command.execute("create event Test from 2025-03-24T14:00 to "
              + "2025-03-24T15:00 repeats M", cm, "interactive");
      fail("Expected exception due to invalid repeat rule");
    } catch (Exception e) {
      assertTrue("Exception message should indicate invalid repeat format",
              e.getMessage().contains("Invalid repeat format: 'M'"));
    }
  }

  @Test
  public void testExecuteRecurringNoConflict() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Early from 2025-03-25T09:00 to 2025-03-25T10:00",
              cm, "interactive");
      command.execute("create event Weekly from 2025-03-25T14:00 to 2025-03-25T15:00 "
              + "repeats T for 3 times", cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(4, cal.getEventScheduler().retrieveAllEvents().size());
      String output = outContent.toString().trim();
      String[] lines = output.split("\n");
      assertTrue(lines[0].contains("Event created: Early from 2025-03-25T09:00[Asia/Kolkata] "
              + "to 2025-03-25T10:00[Asia/Kolkata]"));
      assertEquals("Recurring event created: 3 instances", lines[1]);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteRecurringMultiDayTemplateRejected() {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    CalendarManager cm = new CalendarManager();

    try {
      new CreateCalendarCommand(cm).execute(
              "create calendar --name Work --timezone Asia/Kolkata",
              cm, "interactive");
      new UseCalendarCommand(cm).execute(
              "use calendar --name Work", cm, "interactive");
    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }

    try {
      new CreateEventCommand(cm).execute(
              "create event WeeklyMeeting from 2025-09-15T14:00 "
                      +  "to 2025-09-16T15:00 repeats M until 2025-10-07T15:00",
              cm, "interactive");
      fail("Expected exception for multi-day template");
    } catch (Exception e) {
      ConsoleWriter.getInstance().writeLine("Error: " + e.getMessage());
      assertEquals("Recurring event template must span a single day; "
                      + "start '2025-09-15T14:00' and end "
                      + "'2025-09-16T15:00' are on different days",
              e.getMessage());
      String output = outContent.toString();
      assertTrue(output.contains("Error: Recurring event template must span a single day"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testCreateBeforeCalendarSelection() {
    CalendarManager emptyCm = new CalendarManager();
    CreateEventCommand cmd = new CreateEventCommand(emptyCm);
    try {
      cmd.execute("create event Test from 2025-03-24T09:00 to 2025-03-24T10:00",
              emptyCm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("No calendar selected", e.getMessage());
    }
  }

  @Test
  public void testRecurringNegativeCount() throws Exception {
    cm.setCurrentCalendar("Work");
    try {
      command.execute("create event Weekly from 2025-03-24T14:00 "
                      + "to 2025-03-24T15:00 repeats M for -1 times",
              cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Invalid repeat count"));
    }
  }

  @Test
  public void testExecuteMultiDayEvent() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("create event Conference from 2025-03-24T09:00 to 2025-03-26T17:00",
              cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(1, cal.getEventScheduler().retrieveAllEvents().size());
      IEvent event = cal.getEventScheduler().retrieveAllEvents().get(0);
      assertEquals("Conference", event.getEventName());
      assertEquals(ZonedDateTime.parse("2025-03-24T09:00+05:30[Asia/Kolkata]"),
              event.getStart());
      assertEquals(ZonedDateTime.parse("2025-03-26T17:00+05:30[Asia/Kolkata]"),
              event.getEnd());
      String output = outContent.toString().trim();
      assertTrue(output.contains("Event created: Conference from 2025-03-24T09:00[Asia/Kolkata] "
              + "to 2025-03-26T17:00[Asia/Kolkata]"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteDSTTransition() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      cm.createCalendar("DSTCal", ZoneId.of("America/New_York"));
      cm.setCurrentCalendar("DSTCal");
      command.execute("create event DSTEvent from 2025-11-02T01:00 to 2025-11-02T03:00",
              cm, "interactive");
      Calendar cal = cm.getCurrentCalendar();
      assertEquals(1, cal.getEventScheduler().retrieveAllEvents().size());
      IEvent event = cal.getEventScheduler().retrieveAllEvents().get(0);
      assertEquals(3, Duration.between(event.getStart(), event.getEnd()).toHours());
      String output = outContent.toString().trim();
      assertTrue(output.contains("Event created: DSTEvent "
              + "from 2025-11-02T01:00[America/New_York] to 2025-11-02T03:00[America/New_York]"));
    } finally {
      System.setOut(originalOut);
    }
  }
}