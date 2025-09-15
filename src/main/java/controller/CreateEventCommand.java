package controller;

import model.Calendar;
import model.ICalendarManager;
import model.IEvent;
import model.IEventManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The CreateEventCommand class represents a command that creates a new event
 * in the calendar application. It extends the ACommand class and implements
 * the logic to initialize and add a new event to the calendar.
 */
public class CreateEventCommand extends ACommand {
  private static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public CreateEventCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar calendar = getCurrentCalendar();
    IEventManager scheduler = calendar.getEventScheduler();
    ZoneId timezone = calendar.getTimezone();
    String cleanedCommand = command.replace("--autoDecline", "").trim();

    if (cleanedCommand.contains(" from ")) {
      String[] partsAfterFrom = splitCommand(cleanedCommand, " from ");
      String eventName = partsAfterFrom[0].replace("create event", "").trim();
      String timeDetails = partsAfterFrom[1].trim();

      try {
        if (timeDetails.contains(" to ")) {
          String[] partsAfterTo = timeDetails.split(" to ", 2);
          String startStr = partsAfterTo[0].trim();
          String endDetails = partsAfterTo[1].trim();

          if (endDetails.toLowerCase().contains(" repeats ")) {
            String[] repeatParts = endDetails.split(" repeats ", 2);
            String endStr = repeatParts[0].trim();
            String repeatRule = repeatParts[1].trim();
            LocalDateTime start = LocalDateTime.parse(startStr, TIME_FORMAT);
            LocalDateTime end = LocalDateTime.parse(endStr, TIME_FORMAT);
            if (end.isBefore(start)) {
              throw new Exception("End time '" + endStr + "' before start '" + startStr + "'");
            }
            if (!start.toLocalDate().equals(end.toLocalDate())) {
              throw new Exception("Recurring event template must span a single day; start '"
                      + startStr + "' and end '" + endStr + "' are on different days");
            }
            List<IEvent> instances = scheduler.createRecurringEvents(eventName,
                    start.atZone(timezone), end.atZone(timezone), repeatRule, false);
            verifyNoRecurringConflicts(instances, scheduler, eventName);
            for (IEvent instance : instances) {
              scheduler.scheduleEvent(instance);
            }
            writeLine("Recurring event created: " + instances.size() + " instances");
          } else {
            LocalDateTime start = LocalDateTime.parse(startStr, TIME_FORMAT);
            LocalDateTime end = LocalDateTime.parse(endDetails, TIME_FORMAT);
            createAndScheduleEvent(scheduler, eventName, start, end, false, timezone);
          }
        } else {
          LocalDateTime start = LocalDateTime.parse(timeDetails, TIME_FORMAT);
          LocalDateTime end = start.toLocalDate().plusDays(1).atStartOfDay();
          createAndScheduleEvent(scheduler, eventName, start, end, true, timezone);
        }
      } catch (DateTimeParseException e) {
        throw new Exception("Invalid date/time format: " + e.getParsedString());
      }
    } else if (cleanedCommand.contains(" on ")) {
      String[] partsAfterOn = splitCommand(cleanedCommand, " on ");
      String eventName = partsAfterOn[0].replace("create event", "").trim();
      String dateDetails = partsAfterOn[1].trim();

      try {
        if (dateDetails.toLowerCase().contains(" repeats ")) {
          String[] repeatParts = dateDetails.split(" repeats ", 2);
          String dateStr = repeatParts[0].trim();
          String repeatRule = repeatParts[1].trim();

          LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
          LocalDateTime start = date.atStartOfDay();
          LocalDateTime end = date.plusDays(1).atStartOfDay();

          List<IEvent> instances = scheduler.createRecurringEvents(eventName,
                  start.atZone(timezone), end.atZone(timezone), repeatRule, true);
          verifyNoRecurringConflicts(instances, scheduler, eventName);

          for (IEvent instance : instances) {
            scheduler.scheduleEvent(instance);
          }
          writeLine("Recurring all-day event created: " + instances.size() + " instances");
        } else {
          LocalDate date = LocalDate.parse(dateDetails, DATE_FORMAT);
          LocalDateTime start = date.atStartOfDay();
          LocalDateTime end = date.plusDays(1).atStartOfDay();
          createAndScheduleEvent(scheduler, eventName, start, end, true, timezone);
        }
      } catch (DateTimeParseException e) {
        throw new Exception("Invalid date format: " + e.getParsedString());
      }
    } else {
      throw new Exception("Must include 'from' or 'on' in '" + command + "'");
    }
  }

  private void createAndScheduleEvent(IEventManager scheduler, String eventName,
                                      LocalDateTime start, LocalDateTime end,
                                      boolean isFullDay, ZoneId timezone) throws Exception {
    ZonedDateTime zonedStart = start.atZone(timezone);
    ZonedDateTime zonedEnd = end.atZone(timezone);
    if (zonedEnd.isBefore(zonedStart)) {
      throw new Exception("End time '" + zonedEnd + "' before start '" + zonedStart + "'");
    }
    IEvent event = scheduler.createEvent(eventName, zonedStart, zonedEnd, isFullDay);
    scheduler.scheduleEvent(event);
    writeLine("Event created: " + event);
  }

  private void verifyNoRecurringConflicts(List<IEvent> instances, IEventManager scheduler,
                                          String eventName) throws Exception {
    List<IEvent> existingEvents = scheduler.retrieveAllEvents();
    for (IEvent instance : instances) {
      for (IEvent existing : existingEvents) {
        if (instance.overlapsWith(existing)) {
          throw new Exception("Recurring event '" + eventName + "' conflicts with '"
                  + existing.getEventName() + "' at " + existing.getStart());
        }
      }
    }
  }
}