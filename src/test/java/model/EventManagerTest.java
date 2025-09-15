package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class contains unit tests for the EventManager class.
 */
public class EventManagerTest {
  private EventManager em;
  private ZoneId tz;

  @Before
  public void setUp() {
    // Use a real RecurringEventManager instance to satisfy the constructor
    em = new EventManager(new RecurringEventManager());
    tz = ZoneId.of("Asia/Kolkata");
  }

  @Test
  public void testScheduleEvent() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    assertEquals(1, em.retrieveAllEvents().size());
    assertEquals("Test", em.retrieveAllEvents().get(0).getEventName());
  }

  @Test
  public void testConflict() throws Exception {
    Event event1 = new Event("Test1",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event1);
    try {
      em.scheduleEvent(new Event("Test2",
              ZonedDateTime.of(2025, 3, 24, 9, 30, 0, 0, tz),
              ZonedDateTime.of(2025, 3, 24, 10, 30, 0, 0, tz), false));
      fail("Expected conflict");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Conflict with existing event: Test1"));
    }
  }

  @Test
  public void testAdjustTimezone() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    em.adjustTimezone(tz, ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"),
            em.retrieveAllEvents().get(0).getStart().getZone());
    assertEquals(ZoneId.of("America/New_York"),
            em.retrieveAllEvents().get(0).getEnd().getZone());
  }

  @Test
  public void testFetchEventsStartingOnDate() throws Exception {
    Event event1 = new Event("Test1",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    Event event2 = new Event("Test2",
            ZonedDateTime.of(2025, 3, 25, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 25, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event1);
    em.scheduleEvent(event2);
    List<IEvent> events = em.fetchEventsStartingOnDate(LocalDate.of(2025, 3, 24));
    assertEquals(1, events.size());
    assertEquals("Test1", events.get(0).getEventName());
  }

  @Test
  public void testFetchEventsOnDateFullDay() throws Exception {
    Event event = new Event("Vacation",
            ZonedDateTime.of(2025, 3, 24, 0, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 23, 59, 59, 0, tz), true);
    em.scheduleEvent(event);
    List<IEvent> events = em.fetchEventsOnDate(LocalDate.of(2025, 3, 24));
    assertEquals(1, events.size());
    assertEquals("Vacation", events.get(0).getEventName());
  }

  @Test
  public void testFetchEventsInRange() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    List<IEvent> events = em.fetchEventsInRange(
            ZonedDateTime.of(2025, 3, 24, 8, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 11, 0, 0, 0, tz));
    assertEquals(1, events.size());
    assertEquals("Test", events.get(0).getEventName());
  }

  @Test
  public void testIsOccupiedAt() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    assertTrue(em.isOccupiedAt(ZonedDateTime.of(2025, 3, 24, 9, 30, 0, 0, tz)));
    assertFalse(em.isOccupiedAt(ZonedDateTime.of(2025, 3, 24, 8, 0, 0, 0, tz)));
  }

  @Test
  public void testUpdateSingleEvent() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    assertTrue(em.updateSingleEvent("description", "Test", event.getStart(),
            event.getEnd(), "New"));
    assertEquals("New", em.retrieveAllEvents().get(0).getDescription());
  }

  @Test
  public void testUpdateSingleEventNotFound() {
    try {
      em.updateSingleEvent("description", "Test",
              ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
              ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), "New");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Event not found"));
    }
  }

  @Test
  public void testUpdateEventsFromStart() throws Exception {
    Event event1 = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    Event event2 = new Event("Test",
            ZonedDateTime.of(2025, 3, 25, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 25, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event1);
    em.scheduleEvent(event2);
    int updated = em.updateEventsFromStart("description", "Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz), "New");
    assertEquals(2, updated);
    assertEquals("New", em.retrieveAllEvents().get(0).getDescription());
    assertEquals("New", em.retrieveAllEvents().get(1).getDescription());
  }

  @Test
  public void testUpdateEventsByNameInvalidProperty() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    int updated = em.updateEventsByName("invalid", "Test", "New");
    assertEquals(0, updated);
    assertEquals("", em.retrieveAllEvents().get(0).getDescription());
  }

  @Test
  public void testScheduleEventSortOrder() throws Exception {
    Event event1 = new Event("Late",
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 11, 0, 0, 0, tz), false);
    Event event2 = new Event("Early",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event1);
    em.scheduleEvent(event2);
    List<IEvent> events = em.retrieveAllEvents();
    assertEquals("Early", events.get(0).getEventName());
    assertEquals("Late", events.get(1).getEventName());
  }

  @Test
  public void testFetchEventsOnDateTimedSpanning() throws Exception {
    Event event = new Event("Long",
            ZonedDateTime.of(2025, 3, 24, 23, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 25, 1, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    List<IEvent> events = em.fetchEventsOnDate(LocalDate.of(2025, 3, 25));
    assertEquals(1, events.size());
    assertEquals("Long", events.get(0).getEventName());
  }

  @Test
  public void testUpdateEventsByNameMultiple() throws Exception {
    Event event1 = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    Event event2 = new Event("Test",
            ZonedDateTime.of(2025, 3, 25, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 25, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event1);
    em.scheduleEvent(event2);
    int updated = em.updateEventsByName("description", "Test", "New");
    assertEquals(2, updated);
    assertEquals("New", em.retrieveAllEvents().get(0).getDescription());
    assertEquals("New", em.retrieveAllEvents().get(1).getDescription());
  }

  @Test
  public void testUpdateEventsByNameAllProperties() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    assertEquals(1, em.updateEventsByName("name", "Test", "NewName"));
    assertEquals("NewName", em.retrieveAllEvents().get(0).getEventName());
    assertEquals(1, em.updateEventsByName("location", "NewName", "Office"));
    assertEquals("Office", em.retrieveAllEvents().get(0).getLocation());
    assertEquals(1, em.updateEventsByName("public", "NewName", "false"));
    assertFalse(em.retrieveAllEvents().get(0).isPublic());
  }

  @Test
  public void testDSTTransition() throws Exception {
    ZoneId zone = ZoneId.of("America/New_York");
    Event event = new Event("DST Test",
            ZonedDateTime.of(2025, 11, 2, 1, 0, 0, 0, zone),
            ZonedDateTime.of(2025, 11, 2, 3, 0, 0, 0, zone), false);
    em.scheduleEvent(event);
    assertEquals(3, java.time.Duration.between(event.getStart(), event.getEnd()).toHours());
  }

  @Test
  public void testUpdateInvalidPublicValue() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.now(tz),
            ZonedDateTime.now(tz).plusHours(1), false);
    em.scheduleEvent(event);
    try {
      em.updateEventsByName("public", "Test", "maybe");
      fail("Expected exception for invalid public value");
    } catch (Exception e) {
      assertEquals("Invalid value for 'public': 'maybe' (must be 'true' or 'false')",
              e.getMessage());
    }
  }

  @Test
  public void testScheduleSingleEventMultipleDay() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 26, 10, 0, 0, 0, tz), false);
    em.scheduleEvent(event);
    assertEquals(1, em.retrieveAllEvents().size());
    assertEquals("Test", em.retrieveAllEvents().get(0).getEventName());
  }

  // New tests for uncovered methods

  @Test
  public void testCreateEvent() {
    IEvent event = em.createEvent("NewEvent",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), false);
    assertEquals("NewEvent", event.getEventName());
    assertEquals(ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz), event.getStart());
    assertEquals(ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz), event.getEnd());
    assertFalse(event.isFullDay());
  }

  @Test
  public void testCreateRecurringEvents() throws Exception {
    List<IEvent> recurringEvents = em.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0, tz),
            "M for 3 times", false);
    assertEquals(3, recurringEvents.size());
    assertEquals(ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0, tz), recurringEvents.get(0).getStart());
    assertEquals(ZonedDateTime.of(2025, 3, 31, 9, 0, 0, 0, tz), recurringEvents.get(1).getStart());
    assertEquals(ZonedDateTime.of(2025, 4, 7, 9, 0, 0, 0, tz), recurringEvents.get(2).getStart());
  }
}