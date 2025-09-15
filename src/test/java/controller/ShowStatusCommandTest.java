package controller;

import model.CalendarManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

/**
 * This class contains unit tests for the ShowStatusCommand class.
 */
public class ShowStatusCommandTest {
  private CalendarManager cm;
  private ShowStatusCommand command;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("WorkCal", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("WorkCal");
    new CreateEventCommand(cm).execute("create event Meeting from 2025-03-24T09:00 "
            + "to 2025-03-24T10:00", cm, "interactive");
    command = new ShowStatusCommand(cm);
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  public void testExecuteBusy() throws Exception {
    command.execute("show status on 2025-03-24T09:30", cm, "interactive");
    String output = outContent.toString();
    assertTrue(output.contains("Status at 2025-03-24T09:30[Asia/Kolkata]: Busy"));
  }

  @Test
  public void testExecuteAvailable() throws Exception {
    command.execute("show status on 2025-03-24T08:00", cm, "interactive");
    String output = outContent.toString();
    assertTrue(output.contains("Status at 2025-03-24T08:00[Asia/Kolkata]: Available"));
  }
}