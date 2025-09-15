package calendar;

import controller.CalendarHandler;
import controller.GUICalendarHandler;
import model.CalendarManager;
import model.ICalendarManager;
import view.CalendarGUIView;
import view.ConsoleWriter;

import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * The CalendarApp class serves as the entry point for the calendar application.
 */
public class CalendarApp {
  /**
   * The main method initializes and starts the application.
   *
   * @param args Command-line arguments passed to the application.
   */
  public static void main(String[] args) {
    ICalendarManager calendarManager = new CalendarManager();

    // If no arguments provided, default to GUI mode
    if (args.length == 0) {
      GUICalendarHandler controller = new GUICalendarHandler(calendarManager, null);
      CalendarGUIView view = new CalendarGUIView(calendarManager, controller);
      controller.setView(view);
      controller.processInput(null, "gui");
      return;
    }

    // Handle command-line arguments with --mode
    if (args.length < 2 || !args[0].equalsIgnoreCase("--mode")) {
      ConsoleWriter.getInstance().writeLine(
              "Try: java calendar.CalendarApp --mode interactive or"
                      + " --mode headless <commandFile.txt>");
      return;
    }

    String mode = args[1].toLowerCase();
    CalendarHandler controller = new CalendarHandler(calendarManager);

    if (mode.equals("interactive")) {
      controller.processInput(new InputStreamReader(System.in), "interactive");
    } else if (mode.equals("headless") && args.length == 3) {
      try {
        controller.processInput(new FileReader(args[2]), "headless");
      } catch (java.io.FileNotFoundException e) {
        ConsoleWriter.getInstance().writeLine("Error: File not found: " + args[2]);
        System.exit(1);
      }
    } else {
      ConsoleWriter.getInstance().writeLine(
              "Use '--mode interactive' or '--mode headless <file>' only.");
    }
  }
}
