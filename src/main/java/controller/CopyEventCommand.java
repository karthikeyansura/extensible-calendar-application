package controller;

import model.Calendar;
import model.ICalendarManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The CopyEventCommand class represents a command to copy an event.
 * It extends the ACommand class and encapsulates the logic for
 * duplicating or transferring event data.
 * This command is part of a command pattern implementation and
 * executes the logic related to copying an event within the system.
 */

public class CopyEventCommand extends ACommand {
  private static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public CopyEventCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar sourceCal = getCurrentCalendar();

    String[] parts = splitCommand(command, " on ");
    String eventName = parts[0].equals("copy event") ? "" :
            parts[0].substring("copy event ".length()).trim();
    String[] targetParts = parseTargetClause(parts[1], command);
    String sourceStartStr = targetParts[0];
    String targetCalName = targetParts[1];
    String targetStartStr = targetParts[2];

    Calendar targetCal = getTargetCalendar(targetCalName);

    LocalDateTime sourceStart = LocalDateTime.parse(sourceStartStr, TIME_FORMAT);
    LocalDateTime targetStart = LocalDateTime.parse(targetStartStr, TIME_FORMAT);

    sourceCal.copyEvent(eventName, sourceStart, targetCal, targetStart);
    writeLine("Event '" + eventName + "' copied to " + targetCalName + " at " + targetStartStr);
  }
}