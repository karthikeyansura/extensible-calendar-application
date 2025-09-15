package controller;

import model.Calendar;
import model.ICalendarManager;
import model.IEventManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The AEditEventCommand abstract class serves as an abstract base class
 * for commands that modify event details in the calendar application.
 * It extends ACommand abstract class and provides a foundation for
 * concrete event-editing commands.
 */
public abstract class AEditEventCommand extends ACommand {
  protected static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public AEditEventCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  protected void handleEditCommand(String command, String mode, boolean multiple)
          throws Exception {
    Calendar calendar = getCurrentCalendar();
    IEventManager scheduler = calendar.getEventScheduler();
    ZoneId timezone = calendar.getTimezone();
    String prefix = multiple ? "edit events" : "edit event";
    String details = command.substring(prefix.length()).trim();

    String[] parts;
    String criteria;
    String newValue;
    if (details.contains(" with ")) {
      parts = details.split(" with ", 2);
      criteria = parts[0].trim();
      newValue = parts[1].trim();
    } else {
      parts = details.trim().split(" ", 3);
      if (parts.length < 3) {
        throw new Exception("Invalid format in '" + command + "': missing new value");
      }
      criteria = parts[0] + " " + parts[1];
      newValue = parts[2];
    }

    if (criteria.contains(" from ")) {
      String[] partsAfterFrom = criteria.split(" from ", 2);
      String[] propAndName = partsAfterFrom[0].trim().split(" ", 2);
      String property = propAndName[0].trim();
      String eventName = propAndName.length > 1 ? propAndName[1].trim() : "";
      String timeRange = partsAfterFrom[1].trim();

      if (!multiple && !timeRange.contains(" to ")) {
        throw new Exception("Single edit requires 'to' in '" + command + "'");
      }

      try {
        int count = processTimeRange(scheduler, timeRange, property,
                eventName, newValue, timezone, mode, multiple);
        if (count == 0) {
          throw new Exception("Event not found");
        }
        writeLine(count + " event(s) property \"" + property + "\" updated with \""
                + newValue + "\"");
      } catch (DateTimeParseException e) {
        throw new Exception("Invalid date/time format: " + e.getParsedString());
      }
    } else {
      String[] propAndName = criteria.split(" ", 2);
      String property = propAndName[0].trim();
      String eventName = propAndName.length > 1 ? propAndName[1].trim() : "";
      int count = scheduler.updateEventsByName(property, eventName, newValue);
      if (count == 0) {
        throw new Exception("Event not found");
      }
      writeLine(count + " event(s) property \"" + property + "\" updated with \""
              + newValue + "\"");
    }
  }

  protected abstract int processTimeRange(IEventManager scheduler, String timeRange,
                                          String property, String eventName,
                                          String newValue, ZoneId timezone,
                                          String mode, boolean multiple) throws Exception;

  protected ZonedDateTime parseTime(String timeStr, ZoneId timezone)
          throws DateTimeParseException {
    return LocalDateTime.parse(timeStr.trim(), TIME_FORMAT).atZone(timezone);
  }

  protected void validateTimeRange(ZonedDateTime start, ZonedDateTime end,
                                   String startStr, String endStr) throws Exception {
    if (end.isBefore(start)) {
      throw new Exception("End time '" + endStr.trim() + "' before start '"
              + startStr.trim() + "'");
    }
  }
}