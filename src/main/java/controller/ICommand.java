package controller;

import model.ICalendarManager;

/**
 * The ICommand interface defines the contract for all command objects
 * in the application. It provides a method for executing a command, which
 * can be implemented by various concrete command classes.
 * Implementing classes are expected to define specific actions or logic
 * that are executed when the command is invoked.
 */
public interface ICommand {
  void execute(String command, ICalendarManager calendarManager, String mode) throws Exception;
}