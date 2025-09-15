package controller;

import model.CalendarManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the UseCalendarCommand class.
 */
public class UseCalendarCommandTest {
  private CalendarManager cm;
  private UseCalendarCommand command;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    command = new UseCalendarCommand(cm);
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @Test
  public void testExecuteValid() throws Exception {
    command.execute("use calendar --name Work", cm, "interactive");
    assertEquals("Work", cm.getCurrentCalendar().getName());
    String output = outContent.toString().trim();
    assertEquals("Using calendar: Work", output);
  }

  @Test
  public void testExecuteMissingName() {
    try {
      command.execute("use calendar", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Missing --name in 'use calendar'", e.getMessage());
    }
  }

  @Test
  public void testExecuteNonExistent() {
    try {
      command.execute("use calendar --name Family", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Calendar not found: Family", e.getMessage());
    }
  }

  @Test
  public void testUseBeforeCreation() {
    CalendarManager emptyCm = new CalendarManager();
    command = new UseCalendarCommand(emptyCm);
    try {
      command.execute("use calendar --name Work", emptyCm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Calendar not found: Work", e.getMessage());
    }
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }
}