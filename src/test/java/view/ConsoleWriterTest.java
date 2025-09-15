package view;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the ConsoleWriter class.
 */
public class ConsoleWriterTest {
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;

  @Before
  public void setUp() {
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  public void testWriteLine() {
    ConsoleWriter.getInstance().writeLine("Test");
    assertEquals("Test" + System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testWriteMultipleLines() {
    ConsoleWriter.getInstance().writeLine("Line1");
    ConsoleWriter.getInstance().writeLine("Line2");
    assertEquals("Line1" + System.lineSeparator() + "Line2"
            + System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testDisplayMessage() {
    ConsoleWriter.getInstance().displayMessage("Displayed");
    assertEquals("Displayed" + System.lineSeparator(), outContent.toString());
  }

  @Test
  public void testSingleton() {
    ConsoleWriter instance = ConsoleWriter.getInstance();
    assertSame(instance, ConsoleWriter.getInstance());
  }
}
