package controller;

import model.Calendar;
import model.ICalendarManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The CopyEventsOnCommand class represents a command to copy events
 * on a specific date or time.
 * It extends the ACommand class and encapsulates the logic for copying
 * events that occur on a particular date or set of dates.
 * This command is used when there is a need to duplicate events for a
 * specific date or time range, such as when scheduling events for the same
 * date across different calendars or systems.
 */

public class CopyEventsOnCommand extends ACommand {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public CopyEventsOnCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar sourceCal = getCurrentCalendar();

    String[] parts = splitCommand(command, " on ");
    String[] targetParts = parseTargetClause(parts[1], command);
    String sourceDateStr = targetParts[0].trim();
    String targetCalName = targetParts[1];
    String targetDateStr = targetParts[2].trim();

    LocalDate sourceDate = parseSanitizedDate(sourceDateStr, command);
    LocalDate targetDate = parseSanitizedDate(targetDateStr, command);

    Calendar targetCal = getTargetCalendar(targetCalName);

    int copiedCount = sourceCal.copyEventsOnDate(sourceDate, targetCal, targetDate);
    writeLine(copiedCount + " events copied from " + sourceDateStr + " to "
            + targetCalName + " on " + targetDateStr);
  }

  private LocalDate parseSanitizedDate(String dateStr, String command) throws Exception {
    String sanitizedDateStr = dateStr.replaceAll("[^0-9-]", "");
    try {
      return LocalDate.parse(sanitizedDateStr, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date format in '" + command + "': " + e.getParsedString());
    }
  }
}