package controller;

import model.CalendarManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the PrintEventsInRangeCommand class.
 */
public class PrintEventsInRangeCommandTest {
  private CalendarManager cm;
  private PrintEventsInRangeCommand command;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("WorkCal", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("WorkCal");
    new CreateEventCommand(cm).execute("create event Meeting from 2025-03-24T09:00 "
            + "to 2025-03-24T10:00", cm, "interactive");
    command = new PrintEventsInRangeCommand(cm);
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
    command.execute("print events from 2025-03-24T08:00 to 2025-03-24T11:00",
            cm, "interactive");
    String output = outContent.toString();
    assertTrue(output.contains("Events between"));
    assertTrue(output.contains("Meeting from 2025-03-24T09:00[Asia/Kolkata]"));
  }

  @Test
  public void testExecuteNoEvents() throws Exception {
    command.execute("print events from 2025-03-25T08:00 to 2025-03-25T11:00",
            cm, "interactive");
    String output = outContent.toString();
    assertTrue(output.contains("No events between"));
  }

  @Test
  public void testExecuteInvalidTimeRange() {
    try {
      command.execute("print events from 2025-03-24T11:00 to 2025-03-24T08:00",
              cm, "interactive");
      fail("Expected exception for invalid time range");
    } catch (Exception e) {
      assertEquals("End time '2025-03-24T08:00' before start '2025-03-24T11:00'",
              e.getMessage());
    }
  }
}