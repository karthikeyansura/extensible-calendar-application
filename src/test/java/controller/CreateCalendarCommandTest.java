package controller;

import model.CalendarManager;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This class contains unit tests for the CreateCalendarCommand class.
 */
public class CreateCalendarCommandTest {
  private CalendarManager cm;
  private CreateCalendarCommand command;

  @Before
  public void setUp() {
    cm = new CalendarManager();
    command = new CreateCalendarCommand(cm);
  }

  @Test
  public void testExecuteValid() throws Exception {
    command.execute("create calendar --name Work --timezone Asia/Kolkata",
            cm, "interactive");
    assertNotNull(cm.getCalendar("Work"));
  }

  @Test
  public void testExecuteMissingName() {
    try {
      command.execute("create calendar --timezone Asia/Kolkata", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Missing --name in 'create calendar --timezone Asia/Kolkata'",
              e.getMessage());
    }
  }

  @Test
  public void testExecuteMissingTimezone() {
    try {
      command.execute("create calendar --name Work", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Missing --timezone in 'create calendar --name Work'", e.getMessage());
    }
  }

  @Test
  public void testExecuteDuplicateName() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    try {
      command.execute("create calendar --name Work --timezone America/New_York",
              cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Calendar name already exists: Work", e.getMessage());
    }
  }

  @Test
  public void testMissingTimezone() {
    try {
      command.execute("create calendar --name Work", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Missing --timezone in 'create calendar --name Work'", e.getMessage());
    }
  }
}