package controller;

import java.io.Reader;

/**
 * The ICalendarHandler interface defines the operations for handling
 * calendar-related tasks such as adding, removing, or modifying events.
 * Implementing classes must provide concrete implementations for the
 * methods declared by this interface.
 * This interface serves as a contract for different calendar handlers,
 * which may vary in implementation depending on the context (e.g., GUI,
 * server-side logic, database interaction).
 */

public interface ICalendarHandler {

  void processInput(Reader inputSource, String mode);

  void createCalendar(String name, String timezone) throws Exception;

  void createSingleEvent(String name, String startStr, String endStr) throws Exception;

  void createRecurringEvent(String name,
                            String startStr, String endStr, String repeatRule) throws Exception;

  void editSingleEvent(String property, String eventName,
                       String startStr, String endStr, String newValue) throws Exception;

  void editMultipleEvents(String property,
                          String eventName, String fromStr, String newValue) throws Exception;

  void exportCalendar(String fileName) throws Exception;

  void importCalendar(String fileName) throws Exception;

}