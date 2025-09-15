package controller;

import model.Calendar;
import model.ICalendarManager;
import model.IEventManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The ShowStatusCommand class represents a command that retrieves and
 * displays the current status of the calendar application. It extends the
 * ACommand class and implements the logic to show information such as
 * the current active calendar, the number of events, or other relevant status details.
 */
public class ShowStatusCommand extends ACommand {
  private static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public ShowStatusCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar calendar = getCurrentCalendar();
    IEventManager scheduler = calendar.getEventScheduler();
    ZoneId timezone = calendar.getTimezone();
    String[] parts = splitCommand(command, " on ");
    if (parts.length < 2) {
      throw new Exception("Missing time in '" + command + "'");
    }
    try {
      ZonedDateTime time = java.time.LocalDateTime.parse(parts[1].trim(),
              TIME_FORMAT).atZone(timezone);
      boolean isBusy = scheduler.isOccupiedAt(time);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'['VV']'");
      String formattedTime = time.format(formatter);
      writeLine("Status at " + formattedTime + ": " + (isBusy ? "Busy" : "Available"));
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date/time format: " + e.getParsedString());
    }
  }
}