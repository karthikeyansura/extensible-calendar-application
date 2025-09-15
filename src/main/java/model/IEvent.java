package model;

import java.time.ZonedDateTime;

/**
 * The IEvent interface defines the structure and behavior for event objects
 * in the calendar application. Any class that implements this interface must provide
 * implementations for the methods that manage event details such as name, start time,
 * end time, description, and location.
 * This interface serves as a contract for event-related classes, ensuring that
 * all events adhere to a common structure and can be interacted with uniformly.
 */
public interface IEvent {

  String getEventName();

  void setEventName(String eventName);

  ZonedDateTime getStart();

  void setStart(ZonedDateTime start);

  ZonedDateTime getEnd();

  void setEnd(ZonedDateTime end);

  boolean isFullDay();

  String getDescription();

  void setDescription(String description);

  String getLocation();

  void setLocation(String location);

  boolean isPublic();

  void setPublic(boolean isPublic);

  boolean overlapsWith(IEvent other);
}