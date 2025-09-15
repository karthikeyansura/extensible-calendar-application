package model;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;


/**
 * This class contains unit tests for the RecurringEventManager class.
 */
public class RecurringEventManagerTest {
  private final RecurringEventManager rm = new RecurringEventManager();

  @Test
  public void testCreateRecurringEventsFor() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", false);
    assertEquals(3, events.size());
    assertEquals("Weekly", events.get(0).getEventName());
  }

  @Test
  public void testCreateRecurringEventsUntilFullDay() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 24, 0, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 23, 59, 59,
                    0, ZoneId.of("Asia/Kolkata")),
            "M until 2025-04-07", true);
    assertEquals(3, events.size());
  }

  @Test
  public void testInvalidDayCode() {
    try {
      rm.createRecurringEvents("Test",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              "X for 3 times", false);
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Invalid day code 'X'"));
    }
  }

  @Test
  public void testNoDayCode() {
    try {
      rm.createRecurringEvents("Test",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              "for 3 times", false);
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("No day code specified"));
    }
  }

  @Test
  public void testCreateRecurringEventsForAllDays() throws Exception {
    String[] days = {"M", "T", "W", "R", "F", "S", "U"};
    for (String day : days) {
      List<IEvent> events = rm.createRecurringEvents("Weekly",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              day + " for 3 times", false);
      assertEquals(3, events.size());
    }
  }

  @Test
  public void testInvalidSingleTokenRepeatRule() {
    try {
      rm.createRecurringEvents("Test",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0,
                      0, ZoneId.of("Asia/Kolkata")),
              "M", false);
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Invalid repeat format"));
    }
  }

  @Test
  public void testFullDayStartTime() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Holiday",
            ZonedDateTime.of(2025, 3, 24, 0, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 23, 59, 59, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", true);
    assertEquals(0, events.get(0).getStart().getHour());
    assertEquals(0, events.get(0).getStart().getMinute());
  }

  @Test(timeout = 1000)
  public void testCreateRecurringEventsForWithTimeout() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", false);
    assertEquals(3, events.size());
  }

  @Test(timeout = 100)
  public void testCreateRecurringEventsForWithStrictTimeout() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", false);
    assertEquals(3, events.size());
  }

  @Test(timeout = 1000)
  public void testCreateRecurringEventsForNoProgress() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", false);
    assertEquals(3, events.size());
    assertTrue(events.get(0).getStart().toLocalDate().isBefore(LocalDate.of(2025, 4, 1)));
  }

  @Test
  public void testInvalidTwoTokenRepeatRule() {
    try {
      rm.createRecurringEvents("Test",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              "M for", false);
      fail("Expected exception");
    } catch (Exception e) {
      boolean validMessage = e.getMessage().contains("Expected 'for N times'")
              || e.getMessage().contains("Repeat rule must include 'for' or 'until'")
              || e instanceof IndexOutOfBoundsException;
      assertTrue("Unexpected exception message: " + e.getMessage(), validMessage);
    }
  }

  @Test
  public void test1TwoTokenRepeatRuleEarlyFailure() {
    try {
      rm.createRecurringEvents("Test",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              "M for", false);
      fail("Expected exception");
    } catch (Exception e) {
      boolean validMessage = e.getMessage().contains("Invalid repeat format")
              || e.getMessage().contains("Expected 'for N times'")
              || e.getMessage().contains("Repeat rule must include 'for' or 'until'");
      assertTrue("Unexpected exception message: " + e.getMessage(), validMessage);
    }
  }

  @Test
  public void test2TwoTokenRepeatRuleEarlyFailure() {
    try {
      rm.createRecurringEvents("Test",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              "M for", false);
      fail("Expected exception");
    } catch (Exception e) {
      boolean validMessage = e.getMessage().contains("Repeat rule must include 'for' or 'until'");
      assertTrue("Expected 'for' or 'until' error, got: " + e.getMessage(), validMessage);
      assertFalse("Should not be 'Invalid repeat format' for 2 tokens",
              e.getMessage().contains("Invalid repeat format"));
    }
  }

  @Test
  public void testTimedEventStartTime() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Meeting",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", false);
    assertEquals(14, events.get(0).getStart().getHour());
    assertEquals(0, events.get(0).getStart().getMinute());
  }

  @Test(timeout = 1000)
  public void testLargeCountTermination() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 100 times", false);
    assertEquals(100, events.size());
  }

  @Test
  public void testLeapYearBoundary() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Leap",
            ZonedDateTime.of(2024, 2, 26, 14, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2024, 2, 26, 15, 0, 0, 0, ZoneId.of("UTC")),
            "M for 2 times", false);
    assertEquals(2, events.size());
    assertEquals(LocalDate.of(2024, 3, 4), events.get(1).getStart().toLocalDate());
  }

  @Test
  public void testZeroRepeatCount() {
    try {
      rm.createRecurringEvents("Test",
              ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
              "M for 0 times", false);
      fail("Expected exception for zero repeat count");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Invalid repeat count: 0"));
    }
  }

  @Test
  public void testGetDayCodeAllDays() {
    assertEquals('M', RecurringEventManager.getDayCode(DayOfWeek.MONDAY));
    assertEquals('T', RecurringEventManager.getDayCode(DayOfWeek.TUESDAY));
    assertEquals('W', RecurringEventManager.getDayCode(DayOfWeek.WEDNESDAY));
    assertEquals('R', RecurringEventManager.getDayCode(DayOfWeek.THURSDAY));
    assertEquals('F', RecurringEventManager.getDayCode(DayOfWeek.FRIDAY));
    assertEquals('S', RecurringEventManager.getDayCode(DayOfWeek.SATURDAY));
    assertEquals('U', RecurringEventManager.getDayCode(DayOfWeek.SUNDAY));
  }

  @Test(timeout = 100)
  public void testNoEventsAddedIfDayMismatch() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 25, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")), // Tuesday
            ZonedDateTime.of(2025, 3, 25, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 1 times", false);
    assertEquals(1, events.size());
    assertEquals(LocalDate.of(2025, 3, 31), events.get(0).getStart().toLocalDate());
    assertTrue(RecurringEventManager.matchesDay(DayOfWeek.MONDAY, "M"));
    assertFalse(RecurringEventManager.matchesDay(DayOfWeek.TUESDAY, "M"));
  }

  @Test
  public void testEmptyDaysString() throws Exception {
    List<Event> events = RecurringEventManager.buildRecurringEvents("Test",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", false);
    assertEquals(3, events.size());
  }

  @Test
  public void testFullDayEventEndTime() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Holiday",
            ZonedDateTime.of(2025, 3, 24, 0, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 23, 59, 59, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", true);
    assertEquals(23, events.get(0).getEnd().getHour());
    assertEquals(59, events.get(0).getEnd().getMinute());
    assertEquals(59, events.get(0).getEnd().getSecond());
  }

  @Test
  public void testNonFullDayEventEndTime() throws Exception {
    List<IEvent> events = rm.createRecurringEvents("Meeting",
            ZonedDateTime.of(2025, 3, 24, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 15, 30, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 3 times", false);
    assertEquals(15, events.get(0).getEnd().getHour());
    assertEquals(30, events.get(0).getEnd().getMinute());
    assertEquals(0, events.get(0).getEnd().getSecond());
  }

  @Test
  public void testMatchesDayForAllDays() {
    String allDays = "MTWRFSU";
    for (DayOfWeek day : DayOfWeek.values()) {
      assertTrue("Day " + day + " should match in " + allDays,
              RecurringEventManager.matchesDay(day, allDays));
    }
    assertFalse("Tuesday should not match in 'M'",
            RecurringEventManager.matchesDay(DayOfWeek.TUESDAY, "M"));
  }

  @Test(timeout = 1000)
  public void testInfiniteLoopPrevention() throws Exception {
    // Test a case where no days match to ensure it doesn't loop infinitely
    List<IEvent> events = rm.createRecurringEvents("Weekly",
            ZonedDateTime.of(2025, 3, 25, 14, 0, 0, 0, ZoneId.of("Asia/Kolkata")), // Tuesday
            ZonedDateTime.of(2025, 3, 25, 15, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            "M for 10 times", false);
    assertEquals(10, events.size());
  }
}