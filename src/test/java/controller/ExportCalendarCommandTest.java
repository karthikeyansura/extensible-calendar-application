package controller;

import model.CalendarManager;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.ZoneId;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the ExportCalendarCommand class.
 */
public class ExportCalendarCommandTest {
  private CalendarManager cm;
  private ExportCalendarCommand command;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("WorkCal", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("WorkCal");
    new CreateEventCommand(cm).execute("create event Meeting from "
            + "2025-03-24T09:00 to 2025-03-24T10:00", cm, "interactive");
    command = new ExportCalendarCommand(cm);
    File file = new File("test.csv");
    if (file.exists()) {
      if (!file.delete()) {
        throw new Exception("Failed to delete existing test.csv file");
      }
    }
  }

  @Test
  public void testExecuteValid() throws Exception {
    command.execute("export cal test.csv", cm, "interactive");
    File file = new File("test.csv");
    assertTrue("CSV file should be created", file.exists());
  }

  @Test
  public void testExecuteInvalidFileName() {
    try {
      command.execute("export cal test.txt", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Filename must end with '.csv'", e.getMessage());
    }
  }
}