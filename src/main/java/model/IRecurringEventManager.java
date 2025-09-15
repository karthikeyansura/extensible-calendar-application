package model;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * The IRecurringEventManager interface defines the operations for managing
 * recurring events in the calendar application. It extends the functionality of
 * the IEventManager interface by adding methods specifically for handling
 * events that repeat on a regular basis.
 * Classes implementing this interface are responsible for providing concrete
 * implementations for creating, editing, and deleting recurring events, as well
 * as managing their recurrence patterns
 */
public interface IRecurringEventManager {
  List<IEvent> createRecurringEvents(String eventName, ZonedDateTime start, ZonedDateTime end,
                                     String repeatRule, boolean isFullDay) throws Exception;
}