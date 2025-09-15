package calendar;

import controller.GUICalendarHandler;
import view.CalendarGUIView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the CalendarApp class.
 */
public class CalendarAppTest {
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;
  private SecurityManager originalSecurityManager;

  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // Allow all permissions except exit
    }

    @Override
    public void checkExit(int status) {
      throw new SecurityException("System.exit(" + status + ") attempted");
    }
  }

  @Before
  public void setUp() {
    outContent = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    originalSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setSecurityManager(originalSecurityManager);
    System.setIn(System.in);
  }

  @Test
  public void testMainGUIMode() {
    try (
            MockedConstruction<GUICalendarHandler> mockedController =
                    mockConstruction(GUICalendarHandler.class,
                      (mock, context) -> {
                        doNothing().when(mock).setView(any(CalendarGUIView.class));
                        doNothing().when(mock).processInput(any(), eq("gui"));
                      }
            );
            MockedConstruction<CalendarGUIView> mockedView =
                    mockConstruction(CalendarGUIView.class)
    ) {
      String[] args = {}; // triggers GUI mode
      CalendarApp.main(args);

      // Assert controller was created and methods were invoked
      GUICalendarHandler controller = mockedController.constructed().get(0);
      verify(controller).setView(any(CalendarGUIView.class));
      verify(controller).processInput(null, "gui");

      assertFalse(mockedView.constructed().isEmpty());
    }
  }

  @Test
  public void testMainInteractiveModeUpperCase() {
    String[] args = {"--mode", "INTERACTIVE"};
    String input = "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue(output.contains("Processing interactive input. Type 'exit' to stop."));
    assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testMainHeadlessModeValidFile() throws Exception {
    java.io.File tempFile = java.io.File.createTempFile("test", ".txt");
    try (java.io.PrintWriter writer = new java.io.PrintWriter(tempFile)) {
      writer.write("create calendar --name Test --timezone UTC\nexit\n");
    }
    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue(output.contains("Processing headless input."));
    assertTrue(output.contains("Calendar 'Test' created"));
  }

  @Test
  public void testMainHeadlessModeFileNotFound() {
    String[] args = {"--mode", "headless", "nonexistent.txt"};
    try {
      CalendarApp.main(args);
      fail("Expected SecurityException due to System.exit");
    } catch (SecurityException e) {
      String output = outContent.toString();
      assertTrue(output.contains("Error: File not found: nonexistent.txt"));
      assertEquals("System.exit(1) attempted", e.getMessage());
    }
  }

  @Test
  public void testMainInvalidMode() {
    String[] args = {"--mode", "invalid"};
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue(output.contains("Use '--mode interactive' or '--mode headless <file>' only."));
  }

  @Test
  public void testMainIncompleteArgs() {
    String[] args = {"--mode"};
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue(output.contains("Try: java calendar.CalendarApp --mode interactive or "
            + "--mode headless <commandFile.txt>"));
  }

  @Test
  public void testMainInvalidArgs() {
    String[] args = {"random"};
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue(output.contains("Try: java calendar.CalendarApp --mode interactive or"
            + " --mode headless <commandFile.txt>"));
  }

  @Test
  public void testMainHeadlessMissingFile() {
    String[] args = {"--mode", "headless"};
    CalendarApp.main(args);
    String output = outContent.toString();
    assertTrue(output.contains("Use '--mode interactive' or '--mode headless <file>' only."));
  }
}
