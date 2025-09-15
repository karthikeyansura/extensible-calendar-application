package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * The Calendar class represents a calendar and provides methods for
 * managing and manipulating calendar events.
 * It allows for the creation, removal, and modification of events as well
 * as the ability to view events on specific dates.
 * This class may include functionality such as event scheduling, event
 * retrieval, and managing calendar-related data.
 */

public class Calendar {
  private String name;
  private ZoneId timezone;
  private final IEventManager eventManager;

  /**
   * Constructs a new Calendar instance with the specified name, timezone,
   * and event manager.
   *
   * @param name The name of the calendar (e.g., "Work Calendar", "Personal Calendar").
   * @param timezone The timezone to associate with the calendar (e.g., ZoneId.of
   *                 ("America/New_York")).
   * @param eventManager The event manager responsible for handling calendar events.
   */

  public Calendar(String name, ZoneId timezone, IEventManager eventManager) {
    this.name = name;
    this.timezone = timezone;
    this.eventManager = eventManager;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ZoneId getTimezone() {
    return timezone;
  }

  public void setTimezone(ZoneId newTimezone) {
    eventManager.adjustTimezone(timezone, newTimezone);
    this.timezone = newTimezone;
  }

  public IEventManager getEventScheduler() {
    return eventManager;
  }

  private long calculateEventDuration(IEvent event) {
    return java.time.Duration.between(event.getStart(), event.getEnd()).getSeconds();
  }

  private void createAndScheduleEvent(IEvent sourceEvent, ZonedDateTime newZonedStart,
                                      long secondsDuration, Calendar targetCal) throws Exception {
    ZonedDateTime newZonedEnd = newZonedStart.plusSeconds(secondsDuration);
    IEvent newEvent = targetCal.eventManager.createEvent(
            sourceEvent.getEventName(), newZonedStart, newZonedEnd, sourceEvent.isFullDay());
    newEvent.setDescription(sourceEvent.getDescription());
    newEvent.setLocation(sourceEvent.getLocation());
    newEvent.setPublic(sourceEvent.isPublic());
    targetCal.eventManager.scheduleEvent(newEvent);
  }

  private ZonedDateTime calculateNewStartTime(IEvent sourceEvent, LocalDate targetDate,
                                              ZoneId targetTimezone) {
    long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
            sourceEvent.getStart().toLocalDate(), targetDate);
    ZonedDateTime newStartUtc = sourceEvent.getStart()
            .withZoneSameInstant(ZoneId.of("UTC")).plusDays(daysDiff);
    return newStartUtc.withZoneSameInstant(targetTimezone);
  }

  /**
   * Copies an event from the source calendar to the target calendar.
   * The event is identified by its name and start time, and it is copied
   * to the target calendar, preserving the event details.
   *
   * @param eventName The name of the event to be copied.
   * @param sourceStart The start time of the event in the source calendar.
   * @param targetCal The target calendar to which the event will be copied.
   * @param targetStart The start time for the event in the target calendar.\
   */

  public void copyEvent(String eventName, LocalDateTime sourceStart, Calendar targetCal,
                        LocalDateTime targetStart) throws Exception {
    IEvent sourceEvent = null;
    for (IEvent event : eventManager.retrieveAllEvents()) {
      if (event.getEventName().equals(eventName)
              && event.getStart().toLocalDateTime().equals(sourceStart)) {
        sourceEvent = event;
        break;
      }
    }
    if (sourceEvent == null) {
      throw new Exception("Event '" + eventName + "' not found at " + sourceStart);
    }

    ZonedDateTime newZonedStart = targetStart.atZone(targetCal.getTimezone());
    long secondsDuration = calculateEventDuration(sourceEvent);
    createAndScheduleEvent(sourceEvent, newZonedStart, secondsDuration, targetCal);
  }

  /**
   * Copies all events from the specified source date to the target date in the target calendar.
   * This method iterates through all events on the source date and copies them to the target date
   * in the specified target calendar. If an error occurs during the process, an exception is
   * thrown.
   *
   * @param sourceDate The date from which events are copied.
   * @param targetCal The calendar to which the events will be copied.
   * @param targetDate The date in the target calendar where the events will be placed.
   * @return The number of events successfully copied.
   * @throws Exception If an error occurs during the copying process.
   */

  public int copyEventsOnDate(LocalDate sourceDate, Calendar targetCal,
                              LocalDate targetDate) throws Exception {
    List<IEvent> sourceEvents = eventManager.fetchEventsStartingOnDate(sourceDate);
    for (IEvent sourceEvent : sourceEvents) {
      ZonedDateTime newZonedStart = calculateNewStartTime(sourceEvent, targetDate,
              targetCal.getTimezone());
      long secondsDuration = calculateEventDuration(sourceEvent);
      createAndScheduleEvent(sourceEvent, newZonedStart, secondsDuration, targetCal);
    }
    return sourceEvents.size();
  }

  /**
   * Copies all events from the specified date range (startDate to endDate) to a single target date
   * in the target calendar.
   * This method iterates through all events between the given start and end dates and copies them
   * to the specified target
   * date in the target calendar. If an error occurs during the copying process, an exception is
   * thrown.
   *
   * @param startDate The start date of the range from which events are copied.
   * @param endDate The end date of the range from which events are copied.
   * @param targetCal The calendar to which the events will be copied.
   * @param targetDate The target date in the target calendar where all the events will be copied.
   * @return The number of events successfully copied.
   * @throws Exception If an error occurs during the copying process.
   */

  public int copyEventsBetweenDates(LocalDate startDate, LocalDate endDate,
                                    Calendar targetCal, LocalDate targetDate) throws Exception {
    List<IEvent> eventsToCopy = eventManager.fetchEventsInRange(
            ZonedDateTime.of(startDate.atStartOfDay(), timezone),
            ZonedDateTime.of(endDate.plusDays(1).atStartOfDay(), timezone)
    );

    int copiedCount = 0;
    for (IEvent event : eventsToCopy) {
      ZonedDateTime newZonedStart = calculateNewStartTime(event, targetDate,
              targetCal.getTimezone());
      long secondsDuration = calculateEventDuration(event);
      createAndScheduleEvent(event, newZonedStart, secondsDuration, targetCal);
      copiedCount++;
    }
    return copiedCount;
  }
}