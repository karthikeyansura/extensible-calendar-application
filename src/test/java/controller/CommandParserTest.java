package controller;

import model.CalendarManager;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the CommandParser class.
 */
public class CommandParserTest {
  @Test
  public void testExecuteCommandValid() throws Exception {
    CalendarManager cm = new CalendarManager();
    CommandParser.executeCommand("create calendar --name Test "
            + "--timezone America/New_York", cm, "interactive");
    assertNotNull(cm.getCalendar("Test"));
  }

  @Test
  public void testExecuteCommandInvalid() {
    try {
      CommandParser.executeCommand("invalid",
              new CalendarManager(), "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Unknown command 'invalid'", e.getMessage());
    }
  }
}