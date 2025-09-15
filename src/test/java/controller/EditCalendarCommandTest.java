package controller;

import model.CalendarManager;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the EditCalendarCommand class.
 */
public class EditCalendarCommandTest {
  private CalendarManager cm;
  private EditCalendarCommand command;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    command = new EditCalendarCommand(cm);
  }

  @Test
  public void testExecuteEditName() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("edit calendar --name Work --property name WorkCal",
              cm, "interactive");
      assertNotNull(cm.getCalendar("WorkCal"));
      try {
        cm.getCalendar("Work");
        fail("Expected 'Work' to be renamed and not found");
      } catch (Exception e) {
        assertEquals("Calendar not found: Work", e.getMessage());
      }
      String output = outContent.toString().trim();
      assertEquals("Calendar 'Work' updated: name = WorkCal", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteEditTimezone() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      command.execute("edit calendar --name Work --property timezone America/New_York",
              cm, "interactive");
      assertEquals(ZoneId.of("America/New_York"),
              cm.getCalendar("Work").getTimezone());
      String output = outContent.toString().trim();
      assertEquals("Calendar 'Work' updated: timezone = America/New_York", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteMissingName() {
    try {
      command.execute("edit calendar --property name WorkCal", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Missing --name in 'edit calendar --property name WorkCal'",
              e.getMessage());
    }
  }

  @Test
  public void testExecuteMissingProperty() {
    try {
      command.execute("edit calendar --name Work", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Missing --property in 'edit calendar --name Work'", e.getMessage());
    }
  }

  @Test
  public void testExecuteInvalidProperty() {
    try {
      command.execute("edit calendar --name Work --property invalid value",
              cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Invalid property: invalid. Use 'name' or 'timezone'", e.getMessage());
    }
  }
}