package model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The EventManager class implements the IEventManager interface
 * and is responsible for managing events in the calendar application.
 * It provides functionality to create, edit, delete, and retrieve events.
 * This class acts as a central point for managing the collection of events,
 * ensuring that event-related operations are performed efficiently and consistently.
 */
public class EventManager implements IEventManager {
  private final List<IEvent> events = new ArrayList<>();
  private final IRecurringEventManager recurringEventCreator;

  public EventManager(IRecurringEventManager recurringEventCreator) {
    this.recurringEventCreator = recurringEventCreator;
  }

  @Override
  public void scheduleEvent(IEvent newEvent) throws Exception {
    for (IEvent existing : events) {
      if (newEvent.overlapsWith(existing)) {
        throw new Exception("Conflict with existing event: " + existing.getEventName());
      }
    }
    events.add(newEvent);
    events.sort(Comparator.comparing(IEvent::getStart));
  }

  @Override
  public void adjustTimezone(ZoneId oldZone, ZoneId newZone) {
    for (IEvent event : events) {
      ZonedDateTime oldStartZoned = event.getStart();
      ZonedDateTime newStartZoned = oldStartZoned.withZoneSameInstant(newZone);
      ZonedDateTime oldEndZoned = event.getEnd();
      ZonedDateTime newEndZoned = oldEndZoned.withZoneSameInstant(newZone);

      event.setStart(newStartZoned);
      event.setEnd(newEndZoned);
    }
  }

  @Override
  public List<IEvent> fetchEventsStartingOnDate(LocalDate date) {
    List<IEvent> result = new ArrayList<>();
    for (IEvent event : events) {
      if (event.getStart().toLocalDate().equals(date)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public List<IEvent> fetchEventsOnDate(LocalDate date) {
    List<IEvent> result = new ArrayList<>();
    for (IEvent event : events) {
      LocalDate startDate = event.getStart().toLocalDate();
      LocalDate endDate = event.getEnd().toLocalDate();
      if (event.isFullDay() ? startDate.equals(date) :
              (!startDate.isAfter(date) && !endDate.isBefore(date))) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public List<IEvent> fetchEventsInRange(ZonedDateTime start, ZonedDateTime end) {
    List<IEvent> result = new ArrayList<>();
    for (IEvent event : events) {
      if (!event.getEnd().isBefore(start) && !event.getStart().isAfter(end)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public boolean isOccupiedAt(ZonedDateTime time) {
    return events.stream().anyMatch(e -> !e.getStart().isAfter(time)
            && e.getEnd().isAfter(time));
  }

  @Override
  public boolean updateSingleEvent(String property, String eventName, ZonedDateTime start,
                                   ZonedDateTime end, String newValue) throws Exception {
    for (IEvent event : events) {
      if (event.getEventName().equals(eventName) && event.getStart().equals(start)
              && event.getEnd().equals(end)) {
        return modifyProperty(event, property, newValue);
      }
    }
    throw new Exception("Event not found: " + eventName + " from " + start + " to " + end);
  }

  @Override
  public int updateEventsFromStart(String property, String eventName, ZonedDateTime start,
                                   String newValue) {
    int count = 0;
    for (IEvent event : events) {
      if (event.getEventName().equals(eventName) && !event.getStart().isBefore(start)) {
        if (modifyProperty(event, property, newValue)) {
          count++;
        }
      }
    }
    return count;
  }

  @Override
  public int updateEventsByName(String property, String eventName, String newValue)
          throws Exception {
    int count = 0;
    for (IEvent event : events) {
      if (event.getEventName().equals(eventName)) {
        if (property.equalsIgnoreCase("public")
                &&
                !newValue.equalsIgnoreCase("true")
                && !newValue.equalsIgnoreCase("false")) {
          throw new Exception("Invalid value for 'public': '" + newValue
                  + "' (must be 'true' or 'false')");
        }
        if (modifyProperty(event, property, newValue)) {
          count++;
        }
      }
    }
    return count;
  }

  private boolean modifyProperty(IEvent event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "name":
        event.setEventName(newValue);
        return true;
      case "description":
        event.setDescription(newValue);
        return true;
      case "location":
        event.setLocation(newValue);
        return true;
      case "public":
        event.setPublic(Boolean.parseBoolean(newValue));
        return true;
      default:
        return false;
    }
  }

  @Override
  public List<IEvent> retrieveAllEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public IEvent createEvent(String eventName, ZonedDateTime start,
                            ZonedDateTime end, boolean isFullDay) {
    return new Event(eventName, start, end, isFullDay);
  }

  @Override
  public List<IEvent> createRecurringEvents(String eventName, ZonedDateTime start,
                                            ZonedDateTime end, String repeatRule,
                                            boolean isFullDay) throws Exception {
    return recurringEventCreator.createRecurringEvents(eventName, start,
            end, repeatRule, isFullDay);
  }
}