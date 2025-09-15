package controller;

import model.Calendar;
import model.ICalendarManager;
import model.IEvent;
import view.IView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The GUICalendarHandler class implements the ICalendarHandler interface
 * and provides the functionality to manage calendar-related operations
 * through a graphical user interface (GUI).
 * This class is responsible for interacting with the GUI elements to
 * display, update, and manipulate calendar data. It handles user input
 * and translates it into calendar operations such as adding, removing,
 * or modifying events.
 */

public class GUICalendarHandler implements ICalendarHandler {
  private final ICalendarManager calendarManager;
  private IView view;
  private static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public GUICalendarHandler(ICalendarManager calendarManager, IView view) {
    this.calendarManager = calendarManager;
    this.view = view;
  }

  public void setView(IView view) {
    this.view = view;
  }

  @Override
  public void processInput(Reader inputSource, String mode) {
    if (view != null && "gui".equalsIgnoreCase(mode)) {
      view.startInteractiveMode();
    } else {
      new CalendarHandler(calendarManager).processInput(inputSource, mode);
    }
  }

  @Override
  public void createCalendar(String name, String timezone) throws Exception {
    ZoneId zoneId = ZoneId.of(timezone);
    calendarManager.createCalendar(name, zoneId);
    if (view != null) {
      view.updateDisplay();
    }
  }

  @Override
  public void createSingleEvent(String name, String startStr, String endStr) throws Exception {
    Calendar cal = calendarManager.getCurrentCalendar();
    ZoneId tz = cal.getTimezone();
    LocalDateTime start = LocalDateTime.parse(startStr, TIME_FORMAT);
    LocalDateTime end = LocalDateTime.parse(endStr, TIME_FORMAT);
    IEvent event = cal.getEventScheduler().createEvent(name,
            start.atZone(tz), end.atZone(tz), false);
    cal.getEventScheduler().scheduleEvent(event);
    if (view != null) {
      view.updateDisplay();
    }
  }

  @Override
  public void createRecurringEvent(String name, String startStr,
                                   String endStr, String repeatRule) throws Exception {
    Calendar cal = calendarManager.getCurrentCalendar();
    ZoneId tz = cal.getTimezone();
    LocalDateTime start = LocalDateTime.parse(startStr, TIME_FORMAT);
    LocalDateTime end = LocalDateTime.parse(endStr, TIME_FORMAT);
    List<IEvent> instances = cal.getEventScheduler().createRecurringEvents(name,
            start.atZone(tz), end.atZone(tz), repeatRule, false);
    for (IEvent instance : instances) {
      cal.getEventScheduler().scheduleEvent(instance);
    }
    if (view != null) {
      view.updateDisplay();
    }
  }

  @Override
  public void editSingleEvent(String property, String eventName,
                              String startStr, String endStr, String newValue) throws Exception {
    Calendar cal = calendarManager.getCurrentCalendar();
    ZoneId tz = cal.getTimezone();
    ZonedDateTime start = LocalDateTime.parse(startStr, TIME_FORMAT).atZone(tz);
    ZonedDateTime end = LocalDateTime.parse(endStr, TIME_FORMAT).atZone(tz);
    if (!cal.getEventScheduler().updateSingleEvent(property, eventName, start, end, newValue)) {
      throw new Exception("Event not found or update failed");
    }
    if (view != null) {
      view.updateDisplay();
    }
  }

  @Override
  public void editMultipleEvents(String property, String eventName,
                                 String fromStr, String newValue) throws Exception {
    Calendar cal = calendarManager.getCurrentCalendar();
    ZoneId tz = cal.getTimezone();
    ZonedDateTime from = LocalDateTime.parse(fromStr, TIME_FORMAT).atZone(tz);
    int count = cal.getEventScheduler().updateEventsFromStart(property, eventName, from, newValue);
    if (count == 0) {
      throw new Exception("No events updated");
    }
    if (view != null) {
      view.updateDisplay();
    }
  }

  @Override
  public void exportCalendar(String fileName) throws Exception {
    Calendar cal = calendarManager.getCurrentCalendar();
    List<IEvent> events = cal.getEventScheduler().retrieveAllEvents();
    exporter.CalendarExporter.exportToCSV(events, fileName);
  }

  @Override
  public void importCalendar(String fileName) throws Exception {
    Calendar cal = calendarManager.getCurrentCalendar();
    DateTimeFormatter csvDateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter csvTimeFormat = DateTimeFormatter.ofPattern("hh:mm a");

    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      reader.readLine(); // Skip header
      while ((line = reader.readLine()) != null) {
        String[] fields = parseCSVLine(line);
        if (fields.length < 9) {
          continue;
        }

        String name = fields[0];
        String startDateStr = fields[1];
        String startTimeStr = fields[2];
        String endDateStr = fields[3];
        String endTimeStr = fields[4];
        boolean isFullDay = Boolean.parseBoolean(fields[5]);
        String description = fields[6];
        String location = fields[7];
        boolean isPrivate = Boolean.parseBoolean(fields[8]);

        LocalDate startDate = LocalDate.parse(startDateStr, csvDateFormat);
        LocalDate endDate = LocalDate.parse(endDateStr, csvDateFormat);

        ZonedDateTime startZoned;
        ZonedDateTime endZoned;

        if (isFullDay) {
          startZoned = startDate.atStartOfDay(cal.getTimezone());
          endZoned = endDate.atTime(23, 59).atZone(cal.getTimezone());
        } else {
          LocalTime startTime = LocalTime.parse(startTimeStr, csvTimeFormat);
          LocalTime endTime = LocalTime.parse(endTimeStr, csvTimeFormat);
          startZoned = LocalDateTime.of(startDate, startTime).atZone(cal.getTimezone());
          endZoned = LocalDateTime.of(endDate, endTime).atZone(cal.getTimezone());
        }

        IEvent event = cal.getEventScheduler().createEvent(name, startZoned, endZoned, isFullDay);
        event.setDescription(description);
        event.setLocation(location);
        event.setPublic(!isPrivate);
        cal.getEventScheduler().scheduleEvent(event);
      }
    }
    if (view != null) {
      view.updateDisplay();
    }
  }

  private String[] parseCSVLine(String line) {
    return line.split(",", -1);
  }
}