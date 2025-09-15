package controller;

import model.ICalendarManager;

/**
 * The CommandFactory class is responsible for creating instances of
 * command objects based on specified parameters. It follows the Factory
 * design pattern to encapsulate the logic of command instantiation.
 * This class helps in managing command creation dynamically and ensures
 * loose coupling between different command implementations.
 */
public class CommandFactory {
  private enum CommandType {
    CREATE_CALENDAR("create calendar"),
    EDIT_CALENDAR("edit calendar"),
    USE_CALENDAR("use calendar"),
    CREATE_EVENT("create event"),
    EDIT_MULTIPLE("edit events"),
    EDIT_SINGLE("edit event"),
    COPY_EVENT("copy event"),
    COPY_EVENTS_ON("copy events on"),
    COPY_EVENTS_BETWEEN("copy events between"),
    PRINT_ON("print events on"),
    PRINT_RANGE("print events from"),
    EXPORT("export cal"),
    STATUS("show status on"),
    UNKNOWN("");

    private final String prefix;

    CommandType(String prefix) {
      this.prefix = prefix;
    }

    static CommandType fromCommand(String command) {
      String lower = command.toLowerCase();
      if (lower.startsWith("copy events between")) {
        return COPY_EVENTS_BETWEEN;
      }
      if (lower.startsWith("copy events on")) {
        return COPY_EVENTS_ON;
      }
      if (lower.startsWith("copy event")) {
        return COPY_EVENT;
      }
      for (CommandType type : values()) {
        if (lower.startsWith(type.prefix) && !type.equals(UNKNOWN)) {
          return type;
        }
      }
      return UNKNOWN;
    }
  }

  /**
   * Creates and returns an instance of a command based on the specified command.
   * This method initializes the appropriate command object and associates it with
   * the given CalendarManager.
   *
   * @param command The name of the command to be created.
   * @param calendarManager The CalendarManager instance to be used by the command.
   * @return An instance of ICommand corresponding to the given command.
   * @throws Exception If the command type is invalid or an error occurs during instantiation.
   */
  public static ICommand createCommand(String command, ICalendarManager calendarManager)
          throws Exception {
    CommandType type = CommandType.fromCommand(command);
    switch (type) {
      case CREATE_CALENDAR:
        return new CreateCalendarCommand(calendarManager);
      case EDIT_CALENDAR:
        return new EditCalendarCommand(calendarManager);
      case USE_CALENDAR:
        return new UseCalendarCommand(calendarManager);
      case CREATE_EVENT:
        return new CreateEventCommand(calendarManager);
      case EDIT_MULTIPLE:
        return new EditMultipleEventsCommand(calendarManager);
      case EDIT_SINGLE:
        return new EditSingleEventCommand(calendarManager);
      case COPY_EVENT:
        return new CopyEventCommand(calendarManager);
      case COPY_EVENTS_ON:
        return new CopyEventsOnCommand(calendarManager);
      case COPY_EVENTS_BETWEEN:
        return new CopyEventsBetweenCommand(calendarManager);
      case PRINT_ON:
        return new PrintEventsOnCommand(calendarManager);
      case PRINT_RANGE:
        return new PrintEventsInRangeCommand(calendarManager);
      case EXPORT:
        return new ExportCalendarCommand(calendarManager);
      case STATUS:
        return new ShowStatusCommand(calendarManager);
      case UNKNOWN:
        throw new Exception("Unknown command '" + command + "'");
      default:
        // No-op: just a default requirement
    }
    return null;
  }
}