package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The RecurringEventManager class implements the IRecurringEventManager
 * interface and is responsible for managing recurring events in the calendar application.
 * It provides functionality for creating, editing, and deleting events that repeat
 * on a specified schedule.
 * This class extends the basic event management features by adding support for recurrence
 * patterns, ensuring that recurring events are handled and stored correctly.
 */
public class RecurringEventManager implements IRecurringEventManager {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final String VALID_DAY_CODES = "MTWRFSU";

  /**
   * Returns a character code representing the specified day of the week.
   * This method converts a DayOfWeek instance to a corresponding character
   * code that represents the day.
   *
   * @param day The DayOfWeek to be converted to a character code.
   * @return A char representing the day of the week.
   */
  public static char getDayCode(DayOfWeek day) {
    switch (day) {
      case MONDAY: return 'M';
      case TUESDAY: return 'T';
      case WEDNESDAY: return 'W';
      case THURSDAY: return 'R';
      case FRIDAY: return 'F';
      case SATURDAY: return 'S';
      case SUNDAY: return 'U';
      default: throw new IllegalArgumentException("Invalid day: " + day);
    }
  }

  public static boolean matchesDay(DayOfWeek day, String days) {
    return days.indexOf(getDayCode(day)) >= 0;
  }

  /**
   * Builds a list of recurring events based on the provided event details and repeat rule.
   * This method generates a series of events that repeat according to the specified
   * repeat rule, which could include daily, weekly, monthly, or custom recurrence patterns.
   *
   * @param eventName The name of the event.
   * @param start The start date and time of the first occurrence of the event.
   * @param end The end date and time of the first occurrence of the event.
   * @param repeatRule The recurrence rule, specifying how the event should repeat.
   * @param isFullDay A boolean indicating whether the event is a full-day event.
   * @return events containing the recurring events generated according to
   *     the specified rule.
   * @throws Exception If there is an error in processing the recurrence pattern.
   */
  public static List<Event> buildRecurringEvents(String eventName, ZonedDateTime start,
                                                 ZonedDateTime end, String repeatRule,
                                                 boolean isFullDay) throws Exception {
    String[] tokens = repeatRule.trim().split(" ");
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Invalid repeat format: '" + repeatRule + "'");
    }

    String days = getDays(repeatRule, tokens);

    List<Event> events = new ArrayList<>();
    ZonedDateTime current = start.withZoneSameInstant(start.getZone());

    if (repeatRule.toLowerCase().contains(" for ")) {
      if (tokens.length < 4 || !tokens[1].equalsIgnoreCase("for")
              || !tokens[3].equalsIgnoreCase("times")) {
        throw new Exception("Expected 'for N times' in repeat rule '" + repeatRule + "'");
      }
      int count = Integer.parseInt(tokens[2]);
      if (count <=   0) {
        throw new Exception("Invalid repeat count: " + count + " (must be positive)");
      }
      while (events.size() < count) {
        if (matchesDay(current.getDayOfWeek(), days)) {
          appendEvent(events, eventName, current, start, end, isFullDay);
        }
        current = current.plusDays(1);
      }
    } else if (repeatRule.toLowerCase().contains(" until ")) {
      String untilStr = repeatRule.substring(repeatRule.toLowerCase().indexOf("until") + 5).trim();
      ZonedDateTime until = isFullDay
              ? LocalDate.parse(untilStr, DATE_FORMAT).atStartOfDay(start.getZone())
              .plusDays(1).minusSeconds(1) :
              LocalDateTime.parse(untilStr, TIME_FORMAT).atZone(start.getZone());

      while (true) {
        ZonedDateTime eventStart = ZonedDateTime.of(current.toLocalDate(),
                start.toLocalTime(), start.getZone());
        ZonedDateTime eventEnd = ZonedDateTime.of(current.toLocalDate(),
                end.toLocalTime(), start.getZone());
        if (eventStart.isAfter(until) || eventEnd.isAfter(until)) {
          break;
        }
        if (matchesDay(current.getDayOfWeek(), days)) {
          appendEvent(events, eventName, current, start, end, isFullDay);
        }
        current = current.plusDays(1);
      }
    } else {
      throw new Exception("Repeat rule must include 'for' or 'until' in '" + repeatRule + "'");
    }
    return events;
  }

  private static String getDays(String repeatRule, String[] tokens) throws Exception {
    String days = tokens[0].toUpperCase();
    if (days.equals("FOR") || days.equals("UNTIL")) {
      throw new Exception("No day code specified in repeat rule '" + repeatRule + "'. "
              + "Use M, T, W, R, F, S, or U before 'for' or 'until'.");
    }

    for (char c : days.toCharArray()) {
      if (VALID_DAY_CODES.indexOf(c) == -1) {
        throw new Exception("Invalid day code '" + c + "' in repeat rule '" + repeatRule + "'");
      }
    }
    return days;
  }

  private static void appendEvent(List<Event> events, String eventName,
                                  ZonedDateTime current, ZonedDateTime origStart,
                                  ZonedDateTime origEnd, boolean isFullDay) {
    ZonedDateTime eventStart = isFullDay ? current.withHour(0).withMinute(0).withSecond(0) :
            ZonedDateTime.of(current.toLocalDate(), origStart.toLocalTime(), origStart.getZone());
    ZonedDateTime eventEnd = isFullDay ? current.withHour(23).withMinute(59).withSecond(59) :
            ZonedDateTime.of(current.toLocalDate(), origEnd.toLocalTime(), origEnd.getZone());
    events.add(new Event(eventName, eventStart, eventEnd, isFullDay));
  }

  @Override
  public List<IEvent> createRecurringEvents(String eventName, ZonedDateTime start,
                                            ZonedDateTime end, String repeatRule,
                                            boolean isFullDay) throws Exception {
    return new ArrayList<>(buildRecurringEvents(eventName, start, end, repeatRule, isFullDay));
  }
}