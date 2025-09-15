package controller;

import model.CalendarManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class contains unit tests for the PrintEventsOnCommand class.
 */
public class PrintEventsOnCommandTest {
  private CalendarManager cm;
  private PrintEventsOnCommand command;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("WorkCal", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("WorkCal");
    command = new PrintEventsOnCommand(cm);
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  public void testExecuteValid() throws Exception {
    new CreateEventCommand(cm).execute("create event Vacation on 2025-03-27",
            cm, "interactive");
    outContent.reset();
    command.execute("print events on 2025-03-27", cm, "interactive");
    String output = outContent.toString().trim();
    String expected = "Events on 2025-03-27:\n"
            +
            " - Vacation from 2025-03-27T00:00[Asia/Kolkata] to "
            +
            "2025-03-28T00:00[Asia/Kolkata], Full Day, Public";
    assertEquals("testExecuteValid failed. Expected: [" + expected + "], Actual: ["
                    + output + "]",
            expected, output);
  }

  @Test
  public void testExecuteNoEvents() throws Exception {
    command.execute("print events on 2025-03-28", cm, "interactive");
    String output = outContent.toString().trim();
    assertEquals("No events on 2025-03-28", output);
  }

  @Test
  public void testPrintOverlappingEvents() throws Exception {
    new CreateEventCommand(cm).execute("create event Event1 from 2025-03-27T09:00 "
                    + "to 2025-03-27T10:00",
            cm, "interactive");
    new CreateEventCommand(cm).execute("create event Event2 from 2025-03-27T10:00 "
                    + "to 2025-03-27T11:00",
            cm, "interactive");
    outContent.reset();
    command.execute("print events on 2025-03-27", cm, "interactive");
    String output = outContent.toString().trim();
    String expected = "Events on 2025-03-27:\n"
            +
            " - Event1 from 2025-03-27T09:00[Asia/Kolkata] to "
            + "2025-03-27T10:00[Asia/Kolkata], Public\n"
            +
            " - Event2 from 2025-03-27T10:00[Asia/Kolkata] to 2025-03-27T11:00[Asia/Kolkata], "
            + "Public";
    assertEquals("testPrintOverlappingEvents failed. Expected: [" + expected + "], "
                    + "Actual: [" + output + "]",
            expected, output);
  }

  @Test
  public void testPrintMultiDayEvent() throws Exception {
    new CreateEventCommand(cm).execute("create event Conference from 2025-03-26T09:00 "
                    + "to 2025-03-28T17:00",
            cm, "interactive");
    outContent.reset();
    command.execute("print events on 2025-03-27", cm, "interactive");
    String output = outContent.toString().trim();
    String expected = "Events on 2025-03-27:\n"
            +
            " - Conference from 2025-03-26T09:00[Asia/Kolkata] "
            + "to 2025-03-28T17:00[Asia/Kolkata], Public";
    assertEquals("testPrintMultiDayEvent failed. Expected: "
                    + "[" + expected + "], Actual: [" + output + "]",
            expected, output);
  }

  @Test
  public void testInvalidDateFormat() {
    try {
      command.execute("print events on invalid-date", cm, "interactive");
      fail("Expected exception for invalid date");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Invalid date format"));
    }
  }
}