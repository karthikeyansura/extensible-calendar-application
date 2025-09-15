package model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The AEvent class is an abstract base class that implements the
 * IEvent interface. It provides a common structure and shared behavior
 * for all event-related classes in the calendar application.
 * Subclasses of AEvent are expected to define specific event details
 * such as the event's name, start time, end time, location, description,
 * and visibility.
 */
public abstract class AEvent implements IEvent {
  protected String name;
  protected ZonedDateTime start;
  protected ZonedDateTime end;
  protected boolean isFullDay;
  protected String description;
  protected String location;
  protected boolean isPublic;

  protected AEvent(String eventName, ZonedDateTime start, ZonedDateTime end, boolean isFullDay) {
    validateTimes(start, end);
    this.name = eventName;
    this.start = start;
    this.end = end;
    this.isFullDay = isFullDay;
    this.description = "";
    this.location = "";
    this.isPublic = true;
  }

  private void validateTimes(ZonedDateTime start, ZonedDateTime end) {
    if (end != null && end.isBefore(start)) {
      throw new IllegalArgumentException("End time cannot be before start time.");
    }
  }

  @Override
  public boolean overlapsWith(IEvent other) {
    return (this.getStart().isBefore(other.getEnd()) && other.getStart().isBefore(this.getEnd()))
            || this.getStart().equals(other.getStart());
  }

  @Override
  public String toString() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'['VV']'");
    String base = String.format("%s from %s to %s", getEventName(), getStart().format(formatter),
            getEnd().format(formatter));
    if (isFullDay()) {
      base += ", Full Day";
    }
    base += (getDescription().isEmpty() ? "" : ", Description: " + getDescription())
            + (getLocation().isEmpty() ? "" : ", Location: " + getLocation())
            + ", " + (isPublic() ? "Public" : "Private");
    return base;
  }

  @Override
  public abstract String getEventName();

  @Override
  public abstract void setEventName(String eventName);

  @Override
  public abstract ZonedDateTime getStart();

  @Override
  public abstract void setStart(ZonedDateTime start);

  @Override
  public abstract ZonedDateTime getEnd();

  @Override
  public abstract void setEnd(ZonedDateTime end);

  @Override
  public abstract boolean isFullDay();

  @Override
  public abstract String getDescription();

  @Override
  public abstract void setDescription(String description);

  @Override
  public abstract String getLocation();

  @Override
  public abstract void setLocation(String location);

  @Override
  public abstract boolean isPublic();

  @Override
  public abstract void setPublic(boolean isPublic);
}