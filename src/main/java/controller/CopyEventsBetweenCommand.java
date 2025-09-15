package controller;

import model.Calendar;
import model.ICalendarManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The CopyEventsBetweenCommand class represents a command for copying events
 * from one calendar or event collection to another.
 * It extends the ACommand class and encapsulates the logic required to
 * facilitate the transfer of events between different contexts or systems.
 * This command is typically used when events need to be duplicated across
 * different resources, such as copying events from one calendar to another.
 */

public class CopyEventsBetweenCommand extends ACommand {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public CopyEventsBetweenCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    Calendar sourceCal = getCurrentCalendar();
    String[] parts = splitCommand(command, " between ");
    String[] rangeParts = parts[1].split(" and ", 2);
    String startDateStr = rangeParts[0].trim();
    String[] targetParts = parseTargetClause(rangeParts[1], command);
    String endDateStr = targetParts[0].trim();
    String targetCalName = targetParts[1].trim();
    String targetDateStr = targetParts[2].trim();

    LocalDate startDate = parseSanitizedDate(startDateStr, command);
    LocalDate endDate = parseSanitizedDate(endDateStr, command);
    LocalDate targetDate = parseSanitizedDate(targetDateStr, command);

    Calendar targetCal = getTargetCalendar(targetCalName);

    int copiedCount = sourceCal.copyEventsBetweenDates(startDate, endDate, targetCal, targetDate);
    writeLine(copiedCount + " events copied from " + startDate + " to "
            + endDate + " to " + targetCalName + " on " + targetDate);
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