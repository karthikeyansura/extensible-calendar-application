package view;

/**
 * The ConsoleWriter class implements the IView interface
 * and is responsible for writing output to the console in the calendar application.
 */
public class ConsoleWriter implements IView {
  private static final ConsoleWriter INSTANCE = new ConsoleWriter();

  private ConsoleWriter() {}

  public static ConsoleWriter getInstance() {
    return INSTANCE;
  }

  public void writeLine(String text) {
    System.out.println(text);
  }

  @Override
  public void displayMessage(String message) {
    writeLine(message);
  }

  @Override
  public void startInteractiveMode() {
    // This method is overridden by subclasses as needed
  }

  @Override
  public void updateDisplay() {
    // This method is overridden by subclasses as needed
  }
}