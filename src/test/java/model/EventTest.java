package model;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the Event class.
 */
public class EventTest {
  private Event event;

  @Before
  public void setUp() {
    event = new Event("Test",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")), false);
  }

  @Test
  public void testGettersAndSetters() {
    event.setEventName("New");
    assertEquals("New", event.getEventName());
    event.setDescription("Desc");
    assertEquals("Desc", event.getDescription());
    event.setLocation("Loc");
    assertEquals("Loc", event.getLocation());
    event.setPublic(false);
    assertFalse(event.isPublic());
  }

  @Test
  public void testOverlapsWithInside() {
    Event other = new Event("Other",
            ZonedDateTime.of(2025, 3, 24, 9, 30, 0, 0,
                    ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 30, 0, 0,
                    ZoneId.of("Asia/Kolkata")), false);
    assertTrue(event.overlapsWith(other));
    assertTrue(other.overlapsWith(event));
  }

  @Test
  public void testOverlapsWithNoOverlap() {
    Event other = new Event("Other",
            ZonedDateTime.of(2025, 3, 24, 11, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 12, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")), false);
    assertFalse(event.overlapsWith(other));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTime() {
    new Event("Invalid",
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0,
            ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")), false);
  }

  @Test
  public void testToStringFullDay() {
    Event event = new Event("Vacation",
            ZonedDateTime.parse("2025-03-24T00:00+05:30[Asia/Kolkata]"),
            ZonedDateTime.parse("2025-03-24T23:59+05:30[Asia/Kolkata]"), true);
    event.setDescription("");
    event.setLocation("");
    event.setPublic(false);
    String expected =
            "Vacation from 2025-03-24T00:00[Asia/Kolkata] to 2025-03-24T23:59[Asia/Kolkata], "
                    + "Full Day, Private";
    assertEquals(expected, event.toString());
  }

  @Test
  public void testToStringTimedEvent() {
    Event event = new Event("Meeting",
            ZonedDateTime.parse("2025-03-24T09:00+05:30[Asia/Kolkata]"),
            ZonedDateTime.parse("2025-03-24T10:00+05:30[Asia/Kolkata]"), false);
    event.setDescription("Test");
    event.setLocation("Office");
    event.setPublic(true);
    String expected = "Meeting from 2025-03-24T09:00[Asia/Kolkata] to "
            + "2025-03-24T10:00[Asia/Kolkata], Description: Test, Location: Office, Public";
    assertEquals(expected, event.toString());
  }
}