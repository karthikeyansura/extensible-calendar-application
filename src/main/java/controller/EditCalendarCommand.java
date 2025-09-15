package controller;

import model.ICalendarManager;

/**
 * The EditCalendarCommand class represents a command that allows the user
 * to edit an existing calendar in the calendar application. It extends the ACommand
 * class and implements the logic to modify the properties or settings of a calendar.
 * This command is typically used to update the calendar details, such as the calendar's
 * name, or other relevant attributes.
 */
public class EditCalendarCommand extends ACommand {
  public EditCalendarCommand(ICalendarManager calendarManager) {
    super(calendarManager);
  }

  @Override
  public void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception {
    String[] parts = command.split(" --name ");
    if (parts.length < 2) {
      throw new Exception("Missing --name in '" + command + "'");
    }
    String[] nameParts = parts[1].split(" --property ", 2);
    if (nameParts.length < 2) {
      throw new Exception("Missing --property in '" + command + "'");
    }

    String originalName = nameParts[0].trim();
    String[] propAndValue = nameParts[1].trim().split(" ", 2);
    if (propAndValue.length < 2) {
      throw new Exception("Missing new property value in '" + command + "'");
    }
    String property = propAndValue[0].trim();
    String newValue = propAndValue[1].trim();
    this.calendarManager.editCalendar(originalName, property, newValue);
    writeLine("Calendar '" + originalName + "' updated: " + property + " = " + newValue);
  }
}