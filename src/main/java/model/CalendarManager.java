package model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * The CalendarManager class implements the ICalendarManager interface and is responsible
 * for managing
 * and coordinating calendar-related operations. It provides methods for adding, removing,
 * and updating
 * events, as well as other calendar management tasks.
 * This class acts as a central point for interacting with calendar data, managing calendar
 * instances,
 * and ensuring that operations on the calendar are handled in a consistent and efficient manner.
 */

public class CalendarManager implements ICalendarManager {
  private final Map<String, Calendar> calendars = new HashMap<>();
  private String currentCalendarName;

  @Override
  public void createCalendar(String name, ZoneId timezone) throws Exception {
    if (calendars.containsKey(name)) {
      throw new Exception("Calendar name already exists: " + name);
    }
    IRecurringEventManager recurringEventManager = new RecurringEventManager();
    IEventManager eventManager = new EventManager(recurringEventManager);
    Calendar cal = new Calendar(name, timezone, eventManager);
    calendars.put(name, cal);
  }

  @Override
  public void setCurrentCalendar(String name) throws Exception {
    if (!calendars.containsKey(name)) {
      throw new Exception("Calendar not found: " + name);
    }
    currentCalendarName = name;
  }

  @Override
  public Calendar getCurrentCalendar() throws Exception {
    if (currentCalendarName == null) {
      throw new Exception("No calendar selected");
    }
    return calendars.get(currentCalendarName);
  }

  @Override
  public Calendar getCalendar(String name) throws Exception {
    Calendar cal = calendars.get(name);
    if (cal == null) {
      throw new Exception("Calendar not found: " + name);
    }
    return cal;
  }

  @Override
  public void editCalendar(String name, String property, String newValue) throws Exception {
    Calendar cal = getCalendar(name);
    if (property.equalsIgnoreCase("name")) {
      if (calendars.containsKey(newValue)) {
        throw new Exception("New name already exists: " + newValue);
      }
      calendars.remove(name);
      cal.setName(newValue);
      calendars.put(newValue, cal);
      if (currentCalendarName != null && currentCalendarName.equals(name)) {
        currentCalendarName = newValue;
      }
    } else if (property.equalsIgnoreCase("timezone")) {
      ZoneId newZone = ZoneId.of(newValue);
      cal.setTimezone(newZone);
    } else {
      throw new Exception("Invalid property: " + property + ". Use 'name' or 'timezone'");
    }
  }

  @Override
  public Map<String, Calendar> getCalendars() {
    return new HashMap<>(calendars);
  }
}