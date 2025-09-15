package controller;

import model.CalendarManager;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This class contains unit tests for the CommandFactory class.
 */
public class CommandFactoryTest {
  @Test
  public void testCreateCommandUnknown() {
    try {
      CommandFactory.createCommand("invalid", new CalendarManager());
      fail("Expected exception for unknown command");
    } catch (Exception e) {
      assertEquals("Unknown command 'invalid'", e.getMessage());
    }
  }

  @Test
  public void testCreateCommandTypes() throws Exception {
    CalendarManager cm = new CalendarManager();
    assertTrue(CommandFactory.createCommand("CREATE CALENDAR extra", cm)
            instanceof CreateCalendarCommand);
    assertTrue(CommandFactory.createCommand("edit calendar", cm)
            instanceof EditCalendarCommand);
    assertTrue(CommandFactory.createCommand("USE calendar", cm)
            instanceof UseCalendarCommand);
    assertTrue(CommandFactory.createCommand("create EVENT", cm)
            instanceof CreateEventCommand);
    assertTrue(CommandFactory.createCommand("EDIT events", cm)
            instanceof EditMultipleEventsCommand);
    assertTrue(CommandFactory.createCommand("edit EVENT extra", cm)
            instanceof EditSingleEventCommand);
    assertTrue(CommandFactory.createCommand("COPY event", cm)
            instanceof CopyEventCommand);
    assertTrue(CommandFactory.createCommand("copy EVENTS on", cm)
            instanceof CopyEventsOnCommand);
    assertTrue(CommandFactory.createCommand("COPY events BETWEEN", cm)
            instanceof CopyEventsBetweenCommand);
    assertTrue(CommandFactory.createCommand("PRINT events ON", cm)
            instanceof PrintEventsOnCommand);
    assertTrue(CommandFactory.createCommand("print EVENTS from", cm)
            instanceof PrintEventsInRangeCommand);
    assertTrue(CommandFactory.createCommand("EXPORT cal", cm)
            instanceof ExportCalendarCommand);
    assertTrue(CommandFactory.createCommand("SHOW status ON", cm)
            instanceof ShowStatusCommand);
  }
}