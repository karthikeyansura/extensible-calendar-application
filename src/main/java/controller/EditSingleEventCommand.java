package controller;

import model.ICalendarManager;
import model.IEventManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The EditSingleEventCommand class represents a command that allows
 * the user to edit a single event in the calendar application. It extends
 * the AEditEventCommand class and implements the logic to modify
 * the details of a specific event.
 * This command is typically used to update attributes of a single event,
 * such as the date, time, description, or location.
 */
public class EditSingleEventCommand extends AEditEventCommand {
  public EditSingleEventCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    handleEditCommand(command, mode, false);
  }

  @Override
  protected int processTimeRange(IEventManager scheduler, String timeRange, String property,
                                 String eventName,
                                 String newValue, ZoneId timezone, String mode, boolean multiple)
          throws Exception {
    String[] partsAfterTo = timeRange.split(" to ", 2);
    ZonedDateTime start = parseTime(partsAfterTo[0], timezone);
    ZonedDateTime end = parseTime(partsAfterTo[1], timezone);
    validateTimeRange(start, end, partsAfterTo[0], partsAfterTo[1]);
    try {
      return scheduler.updateSingleEvent(property, eventName, start, end, newValue) ? 1 : 0;
    } catch (Exception e) {
      writeLine(e.getMessage());
      if (mode.equals("headless")) {
        throw e;
      }
      return 0;
    }
  }
}