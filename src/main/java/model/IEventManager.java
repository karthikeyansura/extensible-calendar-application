package model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * The IEventManager interface defines the operations for managing events
 * in the calendar application. It provides methods for creating, editing, deleting,
 * and retrieving events, as well as for other event-related management tasks.
 * Classes implementing this interface are responsible for providing concrete
 * implementations of these operations, ensuring that event management can be
 * performed consistently across the application.
 */
public interface IEventManager {

  void scheduleEvent(IEvent newEvent) throws Exception;

  List<IEvent> fetchEventsStartingOnDate(LocalDate date);

  List<IEvent> fetchEventsOnDate(LocalDate date);

  List<IEvent> fetchEventsInRange(ZonedDateTime start, ZonedDateTime end);

  boolean isOccupiedAt(ZonedDateTime time);

  boolean updateSingleEvent(String property, String eventName, ZonedDateTime start,
                            ZonedDateTime end, String newValue) throws Exception;

  int updateEventsFromStart(String property, String eventName, ZonedDateTime start,
                            String newValue);

  int updateEventsByName(String property, String eventName, String newValue) throws Exception;

  List<IEvent> retrieveAllEvents();

  IEvent createEvent(String eventName, ZonedDateTime start, ZonedDateTime end,
                     boolean isFullDay);

  List<IEvent> createRecurringEvents(String eventName, ZonedDateTime start, ZonedDateTime end,
                                     String repeatRule, boolean isFullDay) throws Exception;

  void adjustTimezone(ZoneId oldZone, ZoneId newZone);
}