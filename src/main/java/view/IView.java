package view;

/**
 * The IView interface defines the contract for all view components in the system.
 * Implementing classes must provide concrete implementations for displaying information
 * and handling user interactions within the view.
 * This interface is typically used in the context of an MVC (Model-View-Controller) pattern,
 * where the view is responsible for rendering the UI and receiving input from the user.
 */

public interface IView {

  void displayMessage(String message);

  void startInteractiveMode();

  void updateDisplay();
}