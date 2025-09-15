package model;

import java.time.ZonedDateTime;

/**
 * The Event class represents a specific event in the calendar application.
 * It extends the AEvent class, providing the implementation details for
 * event properties such as name, start and end times, description, and location.
 * This class encapsulates all the relevant information for an event and is used
 * for creating, modifying, and displaying events in the calendar system.
 * It may contain additional methods for event-specific operations.
 */
public class Event extends AEvent {
  public Event(String eventName, ZonedDateTime start, ZonedDateTime end, boolean isFullDay) {
    super(eventName, start, end, isFullDay);
  }

  @Override
  public String getEventName() {
    return name;
  }

  @Override public void setEventName(String eventName) {
    this.name = eventName;
  }

  @Override public ZonedDateTime getStart() {
    return start;
  }

  @Override public void setStart(ZonedDateTime start) {
    this.start = start;
  }

  @Override public ZonedDateTime getEnd() {
    return end;
  }

  @Override public void setEnd(ZonedDateTime end) {
    this.end = end;
  }

  @Override public boolean isFullDay() {
    return isFullDay;
  }

  @Override public String getDescription() {
    return description;
  }

  @Override public void setDescription(String description) {
    this.description = description;
  }

  @Override public String getLocation() {
    return location;
  }

  @Override public void setLocation(String location) {
    this.location = location;
  }

  @Override public boolean isPublic() {
    return isPublic;
  }

  @Override public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }
}