package model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class contains unit tests for the Calendar class.
 */
public class CalendarTest {
  private Calendar calendar;

  @Before
  public void setUp() {
    // Initialize EventManager with a RecurringEventManager
    IEventManager eventManager = new EventManager(new RecurringEventManager());
    calendar = new Calendar("Test", ZoneId.of("Asia/Kolkata"), eventManager);
  }

  @Test
  public void testGettersAndSetters() {
    calendar.setName("New");
    assertEquals("New", calendar.getName());
    calendar.setTimezone(ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());
  }

  @Test
  public void testGetEventScheduler() {
    assertNotNull(calendar.getEventScheduler());
  }

  @Test
  public void testTimezoneAdjustment() throws Exception {
    Event event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")), false);
    calendar.getEventScheduler().scheduleEvent(event);
    calendar.setTimezone(ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"),
            calendar.getEventScheduler().retrieveAllEvents().get(0).getStart().getZone());
  }

  @Test
  public void testInvalidTimezone() {
    try {
      calendar.setTimezone(ZoneId.of("Invalid/Zone"));
      fail("Expected exception");
    } catch (java.time.zone.ZoneRulesException e) {
      assertTrue(e.getMessage().contains("Unknown time-zone ID"));
    }
  }

  // Additional tests to ensure full coverage of the Calendar class

  @Test
  public void testCopyEvent_Success() throws Exception {
    Event event = new Event("Meeting",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")), false);
    calendar.getEventScheduler().scheduleEvent(event);

    Calendar targetCal = new Calendar("Target", ZoneId.of("UTC"), new EventManager(
            new RecurringEventManager()));
    LocalDateTime sourceStart = LocalDateTime.of(2025, 3, 24, 9, 0);
    LocalDateTime targetStart = LocalDateTime.of(2025, 3, 25, 9, 0);

    calendar.copyEvent("Meeting", sourceStart, targetCal, targetStart);

    List<IEvent> targetEvents = targetCal.getEventScheduler().retrieveAllEvents();
    assertEquals(1, targetEvents.size());
    assertEquals("Meeting", targetEvents.get(0).getEventName());
    assertEquals(ZonedDateTime.of(2025, 3, 25, 9, 0, 0,
            0, ZoneId.of("UTC")), targetEvents.get(0).getStart());
  }

  @Test
  public void testCopyEventsOnDate() throws Exception {
    Event event = new Event("Daily",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")), false);
    calendar.getEventScheduler().scheduleEvent(event);

    Calendar targetCal = new Calendar("Target", ZoneId.of("UTC"), new EventManager(
            new RecurringEventManager()));
    int copied = calendar.copyEventsOnDate(LocalDate.of(2025, 3, 24),
            targetCal, LocalDate.of(2025, 3, 25));
    assertEquals(1, copied);

    List<IEvent> targetEvents = targetCal.getEventScheduler().retrieveAllEvents();
    assertEquals(1, targetEvents.size());
    assertEquals(ZonedDateTime.of(2025, 3, 25, 3, 30, 0,
            0, ZoneId.of("UTC")), targetEvents.get(0).getStart());
  }

  @Test
  public void testCopyEventsBetweenDates() throws Exception {
    Event event1 = new Event("Event1",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")), false);
    Event event2 = new Event("Event2",
            ZonedDateTime.of(2025, 3, 25, 9, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 25, 10, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")), false);
    calendar.getEventScheduler().scheduleEvent(event1);
    calendar.getEventScheduler().scheduleEvent(event2);

    Calendar targetCal = new Calendar("Target", ZoneId.of("UTC"), new EventManager(
            new RecurringEventManager()));
    int copied = calendar.copyEventsBetweenDates(LocalDate.of(2025, 3, 24),
            LocalDate.of(2025, 3, 24),
            targetCal, LocalDate.of(2025, 3, 26));
    assertEquals(1, copied);

    List<IEvent> targetEvents = targetCal.getEventScheduler().retrieveAllEvents();
    assertEquals(1, targetEvents.size());
  }
}