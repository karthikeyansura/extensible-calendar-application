package controller;

import model.ICalendarManager;

/**
 * The CommandParser class is responsible for parsing user input
 * and converting it into executable commands. It processes raw command
 * strings and maps them to corresponding CommandType values.
 * This class helps in interpreting user instructions and ensuring valid
 * command execution within the application.
 */
public class CommandParser {
  /**
   * Executes a command based on the given command string, mode, and calendar manager.
   * This method processes the input command, creates the corresponding command object,
   * and invokes the appropriate action on the CalendarManager class.
   *
   * @param commandStr The string representing the command to be executed.
   * @param calendarManager The CalendarManager instance to manage the calendar state.
   * @param mode The mode in which the command is to be executed.
   * @throws Exception If an error occurs during command execution, or if the command is invalid.
   */
  public static void executeCommand(String commandStr, ICalendarManager calendarManager,
                                    String mode) throws Exception {
    ICommand command = CommandFactory.createCommand(commandStr, calendarManager);
    if (command == null) {
      throw new Exception("Command creation failed for: " + commandStr);
    }
    command.execute(commandStr, calendarManager, mode);
  }
}