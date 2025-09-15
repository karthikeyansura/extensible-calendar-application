package controller;

import model.CalendarManager;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the EditSingleEventCommand class.
 */
public class EditSingleEventCommandTest {
  private CalendarManager cm;
  private EditSingleEventCommand command;

  @Before
  public void setUp() throws Exception {
    cm = new CalendarManager();
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("Work");
    new CreateEventCommand(cm).execute("create event Meeting from 2025-03-24T09:00 to "
            + "2025-03-24T10:00", cm, "interactive");
    command = new EditSingleEventCommand(cm);
  }

  @Test
  public void testExecuteValid() throws Exception {
    command.execute("edit event description Meeting from 2025-03-24T09:00 to "
            + "2025-03-24T10:00 with PDP", cm, "interactive");
    assertEquals("PDP",
            cm.getCurrentCalendar().getEventScheduler().retrieveAllEvents()
                    .get(0).getDescription());
  }

  @Test
  public void testExecuteInvalidTimeRange() {
    try {
      command.execute("edit event description Meeting "
              + "from 2025-03-24T10:00 to 2025-03-24T09:00 with PDP", cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("End time '2025-03-24T09:00' "
              + "before start '2025-03-24T10:00'"));
    }
  }

  @Test
  public void testExecuteInvalidProperty() throws Exception {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      String invalidPropertyCommand = "edit event invalid Meeting "
              + "from 2025-03-24T09:00 to 2025-03-24T10:00 with PDP";
      command.execute(invalidPropertyCommand, cm, "interactive");
      fail("Expected exception for invalid property");
    } catch (Exception e) {
      assertEquals("Event not found", e.getMessage());
      assertEquals("", cm.getCurrentCalendar().getEventScheduler()
              .retrieveAllEvents().get(0).getDescription());
      String output = outContent.toString().trim();
      assertEquals("", output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void testExecuteOnlyProperty() {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try {
      String noEventNameCommand = "edit event description from 2025-03-24T09:00 "
              + "to 2025-03-24T10:00 with Generic";
      command.execute(noEventNameCommand, cm, "interactive");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Event not found", e.getMessage());
      String output = outContent.toString().trim();
      assertEquals("Event not found:  from 2025-03-24T09:00+05:30[Asia/Kolkata] "
              + "to 2025-03-24T10:00+05:30[Asia/Kolkata]", output);
    } finally {
      System.setOut(originalOut);
    }
  }
}