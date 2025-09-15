package controller;

import exporter.CalendarExporter;
import model.Calendar;
import model.ICalendarManager;
import model.IEventManager;
import java.util.List;

/**
 * The ExportCalendarCommand class represents a command that allows
 * the user to export the calendar's events to an external file. It extends the
 * ACommand class and implements the logic to save the calendar's
 * events in a specified format (such as CSV, JSON, etc.).
 * This command is typically used to back up or share calendar data with
 * other systems or applications.
 */
public class ExportCalendarCommand extends ACommand {
  public ExportCalendarCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar calendar = getCurrentCalendar();
    IEventManager scheduler = calendar.getEventScheduler();
    String[] tokens = command.split(" ", 3);
    if (tokens.length < 3) {
      throw new Exception("Missing filename in '" + command + "'");
    }
    String fileName = tokens[2].trim();
    if (!fileName.endsWith(".csv")) {
      throw new Exception("Filename must end with '.csv'");
    }
    List<model.IEvent> events = scheduler.retrieveAllEvents();
    CalendarExporter.exportToCSV(events, fileName);
  }
}