package controller;

import model.Calendar;
import model.ICalendarManager;
import view.ConsoleWriter;

/**
 * The ACommand abstract class provides a base implementation for commands
 * in the application. It implements the ICommand interface and
 * serves as a foundation for concrete command classes.
 */
public abstract class ACommand implements ICommand {
  protected final ICalendarManager calendarManager;

  public ACommand(ICalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }

  @Override
  public abstract void execute(String command, ICalendarManager calendarManager, String mode)
          throws Exception;

  protected Calendar getCurrentCalendar() throws Exception {
    Calendar calendar = calendarManager.getCurrentCalendar();
    if (calendar == null) {
      throw new Exception("No calendar selected");
    }
    return calendar;
  }

  protected Calendar getTargetCalendar(String targetCalName) throws Exception {
    Calendar targetCal = calendarManager.getCalendar(targetCalName);
    if (targetCal == null) {
      throw new Exception("Target calendar '" + targetCalName + "' not found");
    }
    return targetCal;
  }

  protected String[] splitCommand(String command, String separator) {
    return command.split(separator, 2);
  }

  protected String[] parseTargetClause(String input, String command) throws Exception {
    String[] targetParts = input.split(" --target ", 2);
    if (targetParts.length < 2) {
      throw new Exception("Missing --target in '" + command + "'");
    }
    String[] toParts = targetParts[1].split(" to ", 2);
    if (toParts.length < 2) {
      throw new Exception("Missing 'to' in '" + command + "'");
    }
    return new String[] { targetParts[0].trim(), toParts[0].trim(), toParts[1].trim() };
  }

  protected void writeLine(String message) {
    ConsoleWriter.getInstance().writeLine(message);
  }
}