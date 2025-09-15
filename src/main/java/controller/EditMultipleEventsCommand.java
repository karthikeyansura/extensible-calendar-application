package controller;

import model.ICalendarManager;
import model.IEventManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The EditMultipleEventsCommand class represents a command that allows
 * the user to edit multiple events at once in the calendar application. It extends
 * the AEditEventCommand class and implements the logic to modify several
 * events based on specified criteria.
 */
public class EditMultipleEventsCommand extends AEditEventCommand {
  public EditMultipleEventsCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    handleEditCommand(command, mode, true);
  }

  @Override
  protected int processTimeRange(IEventManager scheduler, String timeRange,
                                 String property, String eventName,
                                 String newValue, ZoneId timezone, String mode, boolean multiple) {
    ZonedDateTime start = parseTime(timeRange, timezone);
    return scheduler.updateEventsFromStart(property, eventName, start, newValue);
  }
}