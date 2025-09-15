package exporter;

import model.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * This class contains unit tests for the CalendarExporter class.
 */
public class CalendarExporterTest {
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
  public void testExportToCSVEmpty() throws IOException {
    CalendarExporter.exportToCSV(Collections.emptyList(), "empty.csv");
    File file = new File("empty.csv");
    assertTrue("File should exist", file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    assertEquals("Header should be written",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
                    + "Description,Location,Private" + System.lineSeparator(),
            content);
    String output = outContent.toString();
    assertTrue("Should confirm export", output.contains("Exported to CSV: "
            + file.getAbsolutePath()));
    assertTrue("Should include timezone note", output.contains("Note: Adjust your "
            + "Google Calendar timezone"));
  }

  @Test
  public void testExportToCSVWithTimedEvent() throws IOException {
    Event event = new Event("Meeting",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")), false);
    event.setDescription("Test");
    event.setLocation("Office");
    event.setPublic(true);
    CalendarExporter.exportToCSV(Collections.singletonList(event), "test.csv");
    File file = new File("test.csv");
    assertTrue("File should exist", file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    String expected = "Subject,Start Date,Start Time,End Date,"
            + "End Time,All Day Event,Description,Location,Private" + System.lineSeparator()
            +
            "Meeting,03/24/2025,09:00 AM,03/24/2025,10:00 AM,False,Test,Office,False"
            + System.lineSeparator();
    assertEquals("CSV should match expected format", expected, content);
    String output = outContent.toString();
    assertTrue("Should confirm export", output.contains("Exported to CSV: "
            + file.getAbsolutePath()));
    assertTrue("Should include timezone note", output.contains("Note: Adjust your "
            + "Google Calendar timezone"));
  }

  @Test
  public void testExportToCSVWithFullDayEvent() throws IOException {
    Event event = new Event("Vacation",
            ZonedDateTime.of(2025, 3, 24, 0, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 23, 59, 59, 0,
                    ZoneId.of("Asia/Kolkata")), true);
    event.setDescription("Holiday");
    event.setPublic(false);
    CalendarExporter.exportToCSV(Collections.singletonList(event), "test.csv");
    File file = new File("test.csv");
    assertTrue("File should exist", file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    String expected = "Subject,Start Date,Start Time,End Date,End Time,"
            + "All Day Event,Description,Location,Private" + System.lineSeparator()
            + "Vacation,03/24/2025,,03/24/2025,,True,Holiday,,True" + System.lineSeparator();
    assertEquals("CSV should match full-day format", expected, content);
    String output = outContent.toString();
    assertTrue("Should confirm export", output.contains("Exported to CSV: "
            + file.getAbsolutePath()));
  }

  @Test
  public void testExportToCSVWithCommaInField() throws IOException {
    Event event = new Event("Meeting,Party",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")), false);
    event.setDescription("Test,Details");
    CalendarExporter.exportToCSV(Collections.singletonList(event), "test.csv");
    File file = new File("test.csv");
    assertTrue("File should exist", file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    String expected = "Subject,Start Date,Start Time,End Date,End Time,All Day "
            + "Event,Description,Location,Private" + System.lineSeparator()
            + "\"Meeting,Party\",03/24/2025,09:00 AM,"
            + "03/24/2025,10:00 AM,False,\"Test,Details\",,False" + System.lineSeparator();
    assertEquals("CSV should escape commas", expected, content);
  }

  @Test
  public void testExportToCSVIOException() {
    String invalidPath = "invalid_dir/test.csv";
    CalendarExporter.exportToCSV(Collections.emptyList(), invalidPath);
    String output = outContent.toString();
    assertTrue("Should report error", output.contains("Error exporting CSV: "));
    assertTrue("Should include specific IO error",
            output.contains("No such file or directory")
                    || output.contains("The system cannot find the path specified"));
  }

  @Test
  public void testExportWithSpecialCharacters() throws IOException {
    Event event = new Event("Meeting,Party",
            ZonedDateTime.of(2025, 3, 24, 9, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 3, 24, 10, 0, 0, 0,
                    ZoneId.of("Asia/Kolkata")), false);
    event.setDescription("Test,Details");
    event.setLocation("Office,Room1");
    event.setPublic(true);
    CalendarExporter.exportToCSV(Collections.singletonList(event), "test.csv");
    File file = new File("test.csv");
    assertTrue("File should exist", file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    String expected = "Subject,Start Date,Start Time,End Date,End Time,All Day "
            + "Event,Description,Location,Private" + System.lineSeparator()
            + "\"Meeting,Party\",03/24/2025,09:00 AM,"
            + "03/24/2025,10:00 AM,False,\"Test,Details\",\"Office,Room1\",False"
            + System.lineSeparator();
    assertEquals("CSV should escape commas", expected, content);
    String output = outContent.toString();
    assertTrue("Should confirm export", output.contains("Exported to CSV: "
            + file.getAbsolutePath()));
  }
}