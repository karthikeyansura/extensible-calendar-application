package model;

import java.time.ZoneId;
import java.util.Map;

/**
 * The ICalendarManager interface defines the core operations for managing calendar data.
 * Implementing classes must provide concrete implementations for the methods declared
 * by this interface,
 * such as adding, removing, and updating events, as well as other calendar management tasks.
 * This interface is intended to be implemented by classes that manage a calendar system,
 * either for
 * a single user or multiple users, allowing for the manipulation of calendar events in a
 * consistent way.
 */

public interface ICalendarManager {

  void createCalendar(String name, ZoneId timezone) throws Exception;

  void setCurrentCalendar(String name) throws Exception;

  Calendar getCurrentCalendar() throws Exception;

  Calendar getCalendar(String name) throws Exception;

  void editCalendar(String name, String property, String newValue) throws Exception;

  Map<String, Calendar> getCalendars();

}