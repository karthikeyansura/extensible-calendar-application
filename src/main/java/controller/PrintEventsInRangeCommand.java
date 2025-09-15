package controller;

import model.Calendar;
import model.ICalendarManager;
import model.IEvent;
import model.IEventManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The PrintEventsInRangeCommand class represents a command that retrieves
 * and prints all events within a specified date range in the calendar application.
 * It extends the ACommand class and implements the logic to filter and
 * display events that fall within a given start and end date.
 * This command is typically used to generate a report or overview of events
 * occurring between two dates, helping users review events within a specific
 * timeframe.
 */
public class PrintEventsInRangeCommand extends ACommand {
  private static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public PrintEventsInRangeCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar calendar = getCurrentCalendar();
    IEventManager scheduler = calendar.getEventScheduler();
    ZoneId timezone = calendar.getTimezone();
    String[] partsAfterFrom = splitCommand(command, " from ");
    if (partsAfterFrom.length < 2) {
      throw new Exception("Missing 'from' in '" + command + "'");
    }
    String[] partsAfterTo = partsAfterFrom[1].split(" to ", 2);
    if (partsAfterTo.length < 2) {
      throw new Exception("Missing 'to' in '" + command + "'");
    }

    try {
      ZonedDateTime start = parseTime(partsAfterTo[0], timezone);
      ZonedDateTime end = parseTime(partsAfterTo[1], timezone);
      validateTimeRange(start, end, partsAfterTo[0], partsAfterTo[1]);
      List<IEvent> events = scheduler.fetchEventsInRange(start, end);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'['VV']'");
      String startStr = start.format(formatter);
      String endStr = end.format(formatter);
      writeLine(events.isEmpty()
              ? "No events between " + startStr + " and " + endStr :
              "Events between " + startStr + " and " + endStr + ":\n"
                      + events.stream().map(e -> " - " + e).reduce("",
                        (a, b) -> a + b + "\n"));
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date/time format: " + e.getParsedString());
    }
  }

  private ZonedDateTime parseTime(String timeStr, ZoneId timezone) throws DateTimeParseException {
    return java.time.LocalDateTime.parse(timeStr.trim(), TIME_FORMAT).atZone(timezone);
  }

  private void validateTimeRange(ZonedDateTime start, ZonedDateTime end,
                                 String startStr, String endStr) throws Exception {
    if (end.isBefore(start)) {
      throw new Exception("End time '" + endStr.trim() + "' before start '"
              + startStr.trim() + "'");
    }
  }
}