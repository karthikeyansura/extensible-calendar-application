package controller;

import model.ICalendarManager;
import view.ConsoleWriter;
import java.io.Reader;
import java.util.Scanner;

/**
 * This class handles various calendar-related operations.
 * It implements the ICalendarHandler interface and provides
 * methods to manage and manipulate calendar data.
 */

public class CalendarHandler implements ICalendarHandler {
  private final ICalendarManager calendarManager;

  public CalendarHandler(ICalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }

  @Override
  public void processInput(Reader inputSource, String mode) {
    String initialMessage = getInitialMessage(mode);
    ConsoleWriter.getInstance().writeLine(initialMessage);

    try (Scanner scanner = new Scanner(inputSource)) {
      while (scanner.hasNextLine()) {
        String input = scanner.nextLine().trim();
        ConsoleWriter.getInstance().writeLine("> " + input);
        if (input.isEmpty()) {
          ConsoleWriter.getInstance().writeLine("Error: Empty Line, ignored");
          continue;
        }
        if (input.equalsIgnoreCase("exit")) {
          ConsoleWriter.getInstance().writeLine("Exiting.");
          return;
        }
        try {
          CommandParser.executeCommand(input, calendarManager, mode);
        } catch (Exception e) {
          ConsoleWriter.getInstance().writeLine("Error: " + e.getMessage());
          if (mode.equals("headless")) {
            System.exit(1);
          }
        }
      }
    }
  }

  private String getInitialMessage(String mode) {
    switch (mode.toLowerCase()) {
      case "interactive":
        return "Processing interactive input. Type 'exit' to stop.";
      case "headless":
        return "Processing headless input.";
      default:
        return "Processing " + mode + " input.";
    }
  }

  @Override
  public void createCalendar(String name, String timezone) throws Exception {
    String command = "create calendar --name " + name + " --timezone " + timezone;
    CommandParser.executeCommand(command, calendarManager, "headless");
  }

  @Override
  public void createSingleEvent(String name, String startStr, String endStr) throws Exception {
    String command = "create event " + name + " from " + startStr + " to " + endStr;
    CommandParser.executeCommand(command, calendarManager, "headless");
  }

  @Override
  public void createRecurringEvent(String name, String startStr, String endStr,
                                   String repeatRule) throws Exception {
    String command = "create event " + name + " from " + startStr + " to " + endStr
            + " repeats " + repeatRule;
    CommandParser.executeCommand(command, calendarManager, "headless");
  }

  @Override
  public void editSingleEvent(String property, String eventName, String startStr,
                              String endStr, String newValue) throws Exception {
    String command = "edit event " + property + " " + eventName + " from " + startStr
            + " to " + endStr + " with " + newValue;
    CommandParser.executeCommand(command, calendarManager, "headless");
  }

  @Override
  public void editMultipleEvents(String property, String eventName, String fromStr,
                                 String newValue) throws Exception {
    String command = "edit events " + property + " " + eventName + " from " + fromStr
            + " with " + newValue;
    CommandParser.executeCommand(command, calendarManager, "headless");
  }

  @Override
  public void exportCalendar(String fileName) throws Exception {
    String command = "export cal " + fileName;
    CommandParser.executeCommand(command, calendarManager, "headless");
  }

  @Override
  public void importCalendar(String fileName) throws Exception {
    throw new Exception("Import not supported in interactive/headless mode");
  }
}