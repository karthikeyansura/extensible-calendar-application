package exporter;

import model.IEvent;
import view.ConsoleWriter;

import java.io.File;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The CalendarExporter class is responsible for exporting calendar data
 * to various file formats. It provides methods to save the calendar's events to
 * external files, such as CSV, JSON, or other supported formats.
 * This class enables users to back up their calendar data, share it with others,
 * or integrate with other applications by exporting the calendar's events.
 */
public class CalendarExporter {
  /**
   * Exports the given list of events to a CSV file.
   * This method converts the list of events into a CSV format and saves
   * it to the specified file.
   *
   * @param events The list of {@code IEvent} objects to be exported.
   * @param fileName The name of the CSV file where the events will be saved.
   */
  public static void exportToCSV(List<IEvent> events, String fileName) {
    try (PrintWriter writer = new PrintWriter(fileName)) {
      DateTimeFormatter dateF = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      DateTimeFormatter timeF = DateTimeFormatter.ofPattern("hh:mm a");
      writer.println("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
              + "Description,Location,Private");
      for (IEvent event : events) {
        String eventName = event.getEventName().replaceAll("^\"|\"$", "");
        String[] fields = event.isFullDay()
                ? new String[]{eventName, event.getStart().format(dateF), "",
                event.getStart().format(dateF), "", "True",
                event.getDescription(), event.getLocation(),
                event.isPublic() ? "False" : "True"} :
                new String[]{
                    eventName,
                        event.getStart().format(dateF),
                        event.getStart().format(timeF),
                        event.getEnd().format(dateF),
                        event.getEnd().format(timeF),
                    "False",
                        event.getDescription(),
                        event.getLocation(),
                        event.isPublic() ? "False" : "True"
                };
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
          String field = fields[i];
          if (field.contains(",") || field.contains("\"")) {
            field = "\"" + field.replace("\"", "\"\"") + "\"";
          }
          line.append(field);
          if (i < fields.length - 1) {
            line.append(",");
          }
        }
        writer.println(line);
      }
      ConsoleWriter.getInstance().writeLine("Exported to CSV: "
              + new File(fileName).getAbsolutePath());
      ConsoleWriter.getInstance().writeLine("Note: Adjust your Google Calendar "
              + "timezone to match to the calendar being used.");
    } catch (Exception e) {
      ConsoleWriter.getInstance().writeLine("Error exporting CSV: " + e.getMessage());
    }
  }
}