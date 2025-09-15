package controller;

import model.Calendar;
import model.ICalendarManager;
import model.IEvent;
import model.IEventManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The PrintEventsOnCommand class represents a command that retrieves
 * and prints all events scheduled on a specific day in the calendar application.
 * It extends the ACommand class and implements the logic to filter and
 * display events that occur on a particular date.
 * This command is typically used to generate a report or overview of events
 * occurring on a specific day, helping users review the events for that day.
 */
public class PrintEventsOnCommand extends ACommand {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public PrintEventsOnCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar calendar = getCurrentCalendar();
    IEventManager scheduler = calendar.getEventScheduler();
    String[] parts = splitCommand(command, " on ");
    if (parts.length < 2) {
      throw new Exception("Missing date in '" + command + "'");
    }
    try {
      LocalDate date = LocalDate.parse(parts[1].trim(), DATE_FORMAT);
      List<IEvent> events = scheduler.fetchEventsOnDate(date);
      writeLine(events.isEmpty() ? "No events on " + date :
              "Events on " + date + ":\n"
                      + events.stream().map(e -> " - "
                      + e).reduce("", (a, b) -> a + b + "\n"));
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date format: " + e.getParsedString());
    }
  }
}