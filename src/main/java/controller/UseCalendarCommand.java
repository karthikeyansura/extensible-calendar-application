package controller;

import model.ICalendarManager;

/**
 * The UseCalendarCommand class represents a command that allows the user
 * to select and use a specific calendar from a collection of available calendars.
 * It extends the ACommand class and implements the logic to set the
 * active calendar for the application.
 * This command is typically used to switch between different calendars in the
 * application, enabling the user to perform operations on the selected calendar.
 */
public class UseCalendarCommand extends ACommand {
  public UseCalendarCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    String[] parts = splitCommand(command, " --name ");
    if (parts.length < 2) {
      throw new Exception("Missing --name in '" + command + "'");
    }
    String name = parts[1].trim();
    this.calendarManager.setCurrentCalendar(name);
    writeLine("Using calendar: " + name);
  }
}