package model;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * This class contains unit tests for the CalendarManager class.
 */
public class CalendarManagerTest {
  private CalendarManager cm;

  @Before
  public void setUp() {
    cm = new CalendarManager();
  }

  @Test
  public void testCreateCalendar() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    assertNotNull(cm.getCalendar("Work"));
  }

  @Test
  public void testSetCurrentCalendar() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("Work");
    assertEquals("Work", cm.getCurrentCalendar().getName());
  }

  @Test
  public void testEditCalendarName() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.setCurrentCalendar("Work");
    cm.editCalendar("Work", "name", "WorkCal");
    assertNotNull(cm.getCalendar("WorkCal"));
    assertEquals("WorkCal", cm.getCurrentCalendar().getName());
  }

  @Test
  public void testGetCurrentCalendarNoSelection() {
    try {
      cm.getCurrentCalendar();
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("No calendar selected", e.getMessage());
    }
  }

  @Test
  public void testDuplicateCalendarCreation() throws Exception {
    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    try {
      cm.createCalendar("Work", ZoneId.of("America/New_York"));
      fail("Expected exception for duplicate calendar");
    } catch (Exception e) {
      assertEquals("Calendar name already exists: Work", e.getMessage());
    }
  }

  @Test
  public void testEditNonExistentCalendar() {
    try {
      cm.editCalendar("NonExistent", "name", "NewName");
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("Calendar not found: NonExistent", e.getMessage());
    }
  }

  @Test
  public void testGetCalendars() throws Exception {
    Map<String, Calendar> calendarsEmpty = cm.getCalendars();
    assertTrue("Map should be empty when no calendars are created",
            calendarsEmpty.isEmpty());

    cm.createCalendar("Work", ZoneId.of("Asia/Kolkata"));
    cm.createCalendar("Home", ZoneId.of("America/New_York"));

    Map<String, Calendar> calendars = cm.getCalendars();
    assertEquals("Map should contain 2 calendars", 2, calendars.size());
    assertTrue("Map should contain 'Work'", calendars.containsKey("Work"));
    assertTrue("Map should contain 'Home'", calendars.containsKey("Home"));
    assertEquals("Work calendar should have correct name", "Work",
            calendars.get("Work").getName());
    assertEquals("Home calendar should have correct name", "Home",
            calendars.get("Home").getName());

    calendars.remove("Work");
    assertEquals("Original manager should still have 2 calendars", 2,
            cm.getCalendars().size());
  }
}