package controller;

import model.ICalendarManager;

import java.time.ZoneId;

/**
 * The CreateCalendarCommand class represents a command that creates a new
 * calendar in the calendar application. It extends the ACommand class and
 * implements the logic to initialize and add a new calendar.
 */
public class CreateCalendarCommand extends ACommand {
  public CreateCalendarCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    String[] parts = splitCommand(command, " --name ");
    if (parts.length < 2) {
      throw new Exception("Missing --name in '" + command + "'");
    }
    String[] nameParts = parts[1].split(" --timezone ", 2);
    if (nameParts.length < 2) {
      throw new Exception("Missing --timezone in '" + command + "'");
    }
    String name = nameParts[0].trim();
    String timezone = nameParts[1].trim();
    this.calendarManager.createCalendar(name, ZoneId.of(timezone));
    writeLine("Calendar '" + name + "' created with timezone " + timezone);
  }
}