package view;

import model.Calendar;
import model.ICalendarManager;
import model.IEvent;

import controller.GUICalendarHandler;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A graphical user interface (GUI) view for the calendar application.
 * Implements the IView interface to provide user interaction and display capabilities.
 */

public class CalendarGUIView implements IView {
  private final JFrame frame;
  private final ICalendarManager calendarManager;
  private final GUICalendarHandler controller;
  private JPanel calendarPanel;
  private JLabel monthLabel;
  private JComboBox<String> calendarDropdown;
  private JLabel timezoneLabel;
  private YearMonth currentMonth;
  private String currentCalendarName;
  private boolean isProgrammaticChange = false;
  private static final DateTimeFormatter TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final DateTimeFormatter DATE_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DISPLAY_TIME_FORMAT =
          DateTimeFormatter.ofPattern("HH:mm");
  private final Map<String, Color> calendarColors = new HashMap<>();
  private static final String NO_EVENTS_MESSAGE = "No events to edit";

  /**
   * Constructs a CalendarGUIView with the specified calendar manager and controller.
   * @param calendarManager the calendar manager responsible for managing calendar data
   * @param controller the GUI controller that handles user interaction
   */

  public CalendarGUIView(ICalendarManager calendarManager, GUICalendarHandler controller) {
    this.calendarManager = calendarManager;
    this.controller = controller;
    this.frame = new JFrame("Calendar Application");
    this.currentMonth = YearMonth.now();
    initializeUI();
  }

  private void initializeUI() {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setLayout(new BorderLayout());

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
    topPanel.setBackground(new Color(220, 220, 220));
    topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JButton prevButton = new JButton("<");
    prevButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
    JButton nextButton = new JButton(">");
    nextButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
    monthLabel = new JLabel();
    monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
    calendarDropdown = new JComboBox<>();
    calendarDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    timezoneLabel = new JLabel();
    timezoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    JButton calendarOperationsButton = new JButton("Calendar Operations");
    calendarOperationsButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    JButton eventOperationsButton = new JButton("Event Operations");
    eventOperationsButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    JButton exportButton = new JButton("Export to CSV");
    exportButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    JButton importButton = new JButton("Import from CSV");
    importButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));

    topPanel.add(prevButton);
    topPanel.add(monthLabel);
    topPanel.add(nextButton);
    topPanel.add(calendarDropdown);
    topPanel.add(timezoneLabel);
    topPanel.add(calendarOperationsButton);
    topPanel.add(eventOperationsButton);
    topPanel.add(exportButton);
    topPanel.add(importButton);
    frame.add(topPanel, BorderLayout.NORTH);

    calendarPanel = new JPanel();
    calendarPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    calendarPanel.setBackground(new Color(245, 245, 245));
    frame.add(calendarPanel, BorderLayout.CENTER);

    prevButton.addActionListener(e -> changeMonth(-1));
    nextButton.addActionListener(e -> changeMonth(1));
    calendarDropdown.addActionListener(e -> changeCalendar());
    calendarOperationsButton.addActionListener(e -> showCalendarOperations());
    eventOperationsButton.addActionListener(e -> showEventOperations());
    exportButton.addActionListener(e -> exportCalendar());
    importButton.addActionListener(e -> importCalendar());

    initializeCalendars();
    updateDisplay();
    frame.setVisible(true);
  }

  private void initializeCalendars() {
    try {
      String defaultName = "Default";
      ZoneId systemZone = ZoneId.systemDefault();
      calendarManager.createCalendar(defaultName, systemZone);
      currentCalendarName = defaultName;
      calendarManager.setCurrentCalendar(defaultName);
      calendarColors.put(defaultName, generateCalendarColor(0, 1));
    } catch (Exception e) {
      displayMessage("Error initializing : " + e.getMessage());
    }
    updateCalendarDropdown();
  }

  private void updateCalendarDropdown() {
    isProgrammaticChange = true;
    calendarDropdown.removeAllItems();
    List<String> sortedCalNames = new ArrayList<>(calendarManager.getCalendars().keySet());
    Collections.sort(sortedCalNames);
    int totalCalendars = sortedCalNames.size();
    for (int i = 0; i < totalCalendars; i++) {
      String calName = sortedCalNames.get(i);
      calendarDropdown.addItem(calName);
      if (!calendarColors.containsKey(calName)) {
        calendarColors.put(calName, generateCalendarColor(i, totalCalendars));
      }
    }
    if (currentCalendarName != null) {
      calendarDropdown.setSelectedItem(currentCalendarName);
    }
    isProgrammaticChange = false;
  }

  private Color generateCalendarColor(int index, int totalCalendars) {
    if (totalCalendars <= 0) {
      totalCalendars = 1;
    }
    float hue = (float) index / (float) totalCalendars;
    float saturation = 0.7f;
    float brightness = 0.9f;
    Color rgbColor = Color.getHSBColor(hue % 1.0f, saturation, brightness);
    return new Color(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), 50);
  }

  @Override
  public void updateDisplay() {
    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(0, 7, 8, 8));
    monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

    calendarPanel.setBackground(calendarColors.getOrDefault(currentCalendarName,
            new Color(245, 245, 245)));

    String[] dayLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayLabels) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Segoe UI", Font.BOLD, 16));
      label.setOpaque(true);
      label.setBackground(new Color(200, 200, 200));
      label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
      label.setPreferredSize(new Dimension(100, 40));
      calendarPanel.add(label);
    }

    try {
      Calendar currentCal = calendarManager.getCurrentCalendar();
      timezoneLabel.setText("Timezone: " + currentCal.getTimezone().toString());
      LocalDate firstDay = currentMonth.atDay(1);
      LocalDate today = LocalDate.now();
      int offset = firstDay.getDayOfWeek().getValue() % 7;
      for (int i = 0; i < offset; i++) {
        calendarPanel.add(new JLabel(""));
      }

      int daysInMonth = currentMonth.lengthOfMonth();
      Border defaultBorder = BorderFactory.createLineBorder(Color.GRAY, 1);
      Border todayBorder = BorderFactory.createLineBorder(Color.RED, 3);

      for (int day = 1; day <= daysInMonth; day++) {
        LocalDate date = currentMonth.atDay(day);
        JButton dayButton = new JButton(String.valueOf(day));
        dayButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        dayButton.setOpaque(true);
        dayButton.setBorderPainted(true);

        if (date.equals(today)) {
          dayButton.setBorder(todayBorder);
          dayButton.setBackground(new Color(255, 215, 0));
          dayButton.setForeground(Color.BLACK);
        } else {
          dayButton.setBorder(defaultBorder);
          dayButton.setBackground(Color.WHITE);
          dayButton.setForeground(Color.BLACK);
        }

        List<IEvent> events = currentCal.getEventScheduler().fetchEventsOnDate(date);
        if (!events.isEmpty()) {
          dayButton.setToolTipText(events.size() + " event(s)");
          dayButton.setForeground(new Color(0, 120, 215));
        }

        dayButton.setBorder(BorderFactory.createCompoundBorder(
                dayButton.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        dayButton.setPreferredSize(new Dimension(100, 80));
        dayButton.addActionListener(e -> showDayOptions(date));
        calendarPanel.add(dayButton);
      }
    } catch (Exception e) {
      displayMessage("Error updating : " + e.getMessage());
    }

    frame.revalidate();
    frame.repaint();
  }

  private void changeMonth(int offset) {
    currentMonth = currentMonth.plusMonths(offset);
    updateDisplay();
  }

  private void changeCalendar() {
    if (!isProgrammaticChange) {
      String selected = (String) calendarDropdown.getSelectedItem();
      if (selected != null && !selected.equals(currentCalendarName)) {
        try {
          calendarManager.setCurrentCalendar(selected);
          currentCalendarName = selected;
          updateDisplay();
        } catch (Exception e) {
          displayMessage("Error switching : " + e.getMessage());
        }
      }
    }
  }

  private void showCalendarOperations() {
    String[] options = {"Create Calendar", "Edit Calendar"};
    int choice = JOptionPane.showOptionDialog(frame, "Select a calendar action:",
            "Calendar Operations", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

    switch (choice) {
      case 0:
        createNewCalendar();
        break;
      case 1:
        editCalendar();
        break;
      default:
        break;
    }
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    return panel;
  }

  private JPanel createSubPanel(Component... components) {
    JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    for (Component comp : components) {
      subPanel.add(comp);
    }
    return subPanel;
  }

  private JTextField createTextField() {
    JTextField textField = new JTextField(20);
    textField.setPreferredSize(new Dimension(200, 25));
    textField.setMaximumSize(new Dimension(200, 25));
    return textField;
  }

  private JComboBox<String> createSearchableComboBox(List<String> items) {
    JComboBox<String> comboBox = new JComboBox<>(items.toArray(new String[0]));
    configureComboBoxForSearch(comboBox, items);
    return comboBox;
  }

  private void configureComboBoxForSearch(JComboBox<String> comboBox, List<String> items) {
    comboBox.setEditable(true);
    comboBox.setPreferredSize(new Dimension(200, 25));
    JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
    editor.addKeyListener(new java.awt.event.KeyAdapter() {
      @Override
      public void keyReleased(java.awt.event.KeyEvent e) {
        String input = editor.getText().toLowerCase();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String item : items) {
          if (item.toLowerCase().contains(input)) {
            model.addElement(item);
          }
        }
        comboBox.setModel(model);
        comboBox.showPopup();
        editor.setText(input);
      }
    });
  }

  private JScrollPane createScrollPane(JPanel panel, int height) {
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(new Dimension(350, height));
    return scrollPane;
  }

  private JComboBox<String> createEventNameComboBox(List<String> eventNames) {
    if (eventNames.isEmpty()) {
      displayMessage(NO_EVENTS_MESSAGE);
      return null;
    }
    return createSearchableComboBox(eventNames);
  }

  private void createNewCalendar() {
    JPanel panel = createMainPanel();

    JTextField nameField = createTextField();
    panel.add(createSubPanel(new JLabel("Calendar Name:"), nameField));

    List<String> sortedZones = new ArrayList<>(ZoneId.getAvailableZoneIds());
    Collections.sort(sortedZones);
    JComboBox<String> tzComboBox = new JComboBox<>(sortedZones.toArray(new String[0]));
    tzComboBox.setSelectedItem(ZoneId.systemDefault().toString());
    configureComboBoxForSearch(tzComboBox, sortedZones);
    tzComboBox.setPreferredSize(new Dimension(200, 25));
    panel.add(createSubPanel(new JLabel("Timezone:"), tzComboBox));

    JScrollPane scrollPane = createScrollPane(panel, 150);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane,
            "Create Calendar", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      String name = nameField.getText().trim();
      if (name.isEmpty()) {
        displayMessage("Calendar name is required.");
        return;
      }
      String timezone = (String) tzComboBox.getSelectedItem();
      if (timezone == null || timezone.trim().isEmpty()) {
        displayMessage("Please select a valid timezone.");
        return;
      }
      try {
        controller.createCalendar(name, timezone);
        updateCalendarDropdown();
        updateDisplay();
        displayMessage("Calendar '" + name + "' created successfully");
      } catch (Exception e) {
        displayMessage("Error creating : " + e.getMessage());
      }
    }
  }

  private void editCalendar() {
    String[] calendarNames = calendarManager.getCalendars().keySet().toArray(new String[0]);
    String selectedName = (String) JOptionPane.showInputDialog(frame,
            "Select calendar to edit:",
            "Edit Calendar", JOptionPane.PLAIN_MESSAGE, null, calendarNames,
            currentCalendarName);
    if (selectedName == null) {
      return;
    }

    String[] properties = {"Name", "Timezone"};
    String selectedProperty = (String) JOptionPane.showInputDialog(frame,
            "Select property to edit:",
            "Select Property", JOptionPane.PLAIN_MESSAGE, null, properties,
            properties[0]);
    if (selectedProperty == null) {
      return;
    }

    JPanel panel = createMainPanel();
    JScrollPane scrollPane;

    if (selectedProperty.equals("name")) {
      JTextField nameField = createTextField();
      nameField.setText("");
      panel.add(createSubPanel(new JLabel("New name of calendar:"), nameField));
    } else {
      List<String> sortedZones = new ArrayList<>(ZoneId.getAvailableZoneIds());
      Collections.sort(sortedZones);
      JComboBox<String> tzComboBox = new JComboBox<>(sortedZones.toArray(new String[0]));
      try {
        tzComboBox.setSelectedItem(calendarManager
                .getCalendar(selectedName).getTimezone().toString());
      } catch (Exception e) {
        tzComboBox.setSelectedItem(ZoneId.systemDefault().toString());
      }
      configureComboBoxForSearch(tzComboBox, sortedZones);
      tzComboBox.setPreferredSize(new Dimension(200, 25));
      panel.add(createSubPanel(new JLabel("New Timezone:"), tzComboBox));
    }
    scrollPane = createScrollPane(panel, 100);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane,
            "Edit Calendar", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String newValue;
        if (selectedProperty.equals("name")) {
          newValue = ((JTextField) ((JPanel) panel.getComponent(0)).getComponent(1))
                  .getText().trim();
          if (newValue.isEmpty()) {
            displayMessage("Calendar name cannot be empty");
            return;
          }
        } else {
          newValue = (String) ((JComboBox<?>) ((JPanel) panel.getComponent(0)).getComponent(1))
                  .getSelectedItem();
          if (newValue == null || newValue.trim().isEmpty()) {
            displayMessage("Please select a valid timezone");
            return;
          }
        }

        calendarManager.editCalendar(selectedName, selectedProperty, newValue);
        if (selectedProperty.equals("name")) {
          Color color = calendarColors.remove(selectedName);
          calendarColors.put(newValue, color);
          if (currentCalendarName.equals(selectedName)) {
            currentCalendarName = newValue;
            calendarManager.setCurrentCalendar(newValue);
          }
        }
        updateCalendarDropdown();
        updateDisplay();
        displayMessage("Calendar " + selectedProperty + " updated to '" + newValue + "'");
      } catch (Exception e) {
        displayMessage("Error editing : " + e.getMessage());
      }
    }
  }

  private void showEventOperations() {
    String[] options = {"Create Event", "Edit Event", "Copy Events", "Check Availability",
                        "Print Events"};
    int choice = JOptionPane.showOptionDialog(frame, "Select an event action:",
            "Event Operations", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

    switch (choice) {
      case 0:
        createEventDialog();
        break;
      case 1:
        List<String> eventNames = getUniqueEventNames();
        if (eventNames.isEmpty()) {
          displayMessage(NO_EVENTS_MESSAGE);
          break;
        }
        editEventDialog();
        break;
      case 2:
        copyEvents();
        break;
      case 3:
        checkAvailability();
        break;
      case 4:
        printEventsDialog();
        break;
      default:
        break;
    }
  }

  private void printEventsDialog() {
    JPanel panel = createMainPanel();

    JRadioButton singleDayRadio = new JRadioButton("Events on a Single Day", true);
    JRadioButton rangeRadio = new JRadioButton("Events in a Range");
    ButtonGroup group = new ButtonGroup();
    group.add(singleDayRadio);
    group.add(rangeRadio);

    panel.add(createSubPanel(singleDayRadio));

    JPanel datePanel = createDatePanel("Date:", LocalDate.now());
    JPanel dateWrapper = createSubPanel(datePanel);

    panel.add(createSubPanel(rangeRadio));

    JPanel startPanel = createDateTimePanel("Start:", LocalDateTime.now());
    JPanel startWrapper = createSubPanel(startPanel);

    JPanel endPanel = createDateTimePanel("End:", LocalDateTime.now().plusDays(1));
    JPanel endWrapper = createSubPanel(endPanel);

    panel.add(dateWrapper);
    panel.add(startWrapper);
    panel.add(endWrapper);

    startWrapper.setVisible(false);
    endWrapper.setVisible(false);

    singleDayRadio.addActionListener(e -> {
      dateWrapper.setVisible(true);
      startWrapper.setVisible(false);
      endWrapper.setVisible(false);
    });

    rangeRadio.addActionListener(e -> {
      dateWrapper.setVisible(false);
      startWrapper.setVisible(true);
      endWrapper.setVisible(true);
    });

    JScrollPane scrollPane = createScrollPane(panel, 250);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane, "Print Events",
            JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        Calendar cal = calendarManager.getCurrentCalendar();
        StringBuilder eventList = new StringBuilder();

        if (singleDayRadio.isSelected()) {
          String dateStr = getDateFromPanel(datePanel);
          LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
          List<IEvent> events = cal.getEventScheduler().fetchEventsOnDate(date);
          eventList.append(formatEventList(events, "Events on "
                  + dateStr + ":", true));
        } else {
          String startStr = getDateTimeFromPanel(startPanel);
          String endStr = getDateTimeFromPanel(endPanel);
          ZonedDateTime start = LocalDateTime.parse(startStr, TIME_FORMAT)
                  .atZone(cal.getTimezone());
          ZonedDateTime end = LocalDateTime.parse(endStr, TIME_FORMAT)
                  .atZone(cal.getTimezone());
          List<IEvent> events = cal.getEventScheduler().fetchEventsInRange(start, end);
          eventList.append(formatEventList(events, "Events from " + startStr + " to "
                  + endStr + ":", false));
        }

        JTextArea textArea = new JTextArea(eventList.toString(), 15, 40);
        textArea.setEditable(false);
        JScrollPane eventScrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(frame, eventScrollPane, "Event List",
                JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        displayMessage("Error displaying : " + e.getMessage());
      }
    }
  }

  private JPanel createDatePickerPanel(String label, LocalDate defaultDate, boolean includeTime) {
    JPanel panel = new JPanel(new FlowLayout());
    JTextField yearField = new JTextField(String.valueOf(defaultDate.getYear()), 4);
    yearField.setPreferredSize(new Dimension(60, 25));
    yearField.setMaximumSize(new Dimension(60, 25));
    JComboBox<String> monthCombo = new JComboBox<>(Arrays.stream(Month.values())
            .map(Month::toString)
            .toArray(String[]::new));
    JComboBox<Integer> dayCombo = new JComboBox<>();

    monthCombo.setSelectedItem(defaultDate.getMonth().toString());
    updateDayCombo(dayCombo, YearMonth.of(defaultDate.getYear(),
            defaultDate.getMonth().getValue()));
    dayCombo.setSelectedItem(defaultDate.getDayOfMonth());

    Runnable updateDayComboRunnable = () -> {
      String yearText = yearField.getText().trim();
      try {
        int selectedYear = Integer.parseInt(yearText);
        String selectedMonthStr = (String) monthCombo.getSelectedItem();
        if (selectedMonthStr != null) {
          Month selectedMonth = Month.valueOf(selectedMonthStr);
          updateDayCombo(dayCombo, YearMonth.of(selectedYear, selectedMonth.getValue()));
        }
      } catch (NumberFormatException ignored) {
      }
    };

    yearField.addActionListener(e -> updateDayComboRunnable.run());
    monthCombo.addActionListener(e -> updateDayComboRunnable.run());

    panel.add(new JLabel(label));
    panel.add(yearField);
    panel.add(monthCombo);
    panel.add(dayCombo);

    if (includeTime) {
      String[] hours = new String[24];
      for (int i = 0; i < 24; i++) {
        hours[i] = String.format("%02d", i);
      }
      String[] minutes = new String[60];
      for (int i = 0; i < 60; i++) {
        minutes[i] = String.format("%02d", i);
      }
      JComboBox<String> hourCombo = new JComboBox<>(hours);
      JComboBox<String> minuteCombo = new JComboBox<>(minutes);

      LocalDateTime defaultTime = defaultDate.atStartOfDay();
      hourCombo.setSelectedItem(String.format("%02d", defaultTime.getHour()));
      minuteCombo.setSelectedItem(String.format("%02d", defaultTime.getMinute()));

      panel.add(hourCombo);
      panel.add(new JLabel(":"));
      panel.add(minuteCombo);
    }

    return panel;
  }

  private JPanel createDateTimePanel(String label, LocalDateTime defaultTime) {
    return createDatePickerPanel(label, defaultTime.toLocalDate(), true);
  }

  private JPanel createDatePanel(String label, LocalDate defaultDate) {
    return createDatePickerPanel(label, defaultDate, false);
  }

  private void updateDayCombo(JComboBox<Integer> dayCombo, YearMonth yearMonth) {
    dayCombo.removeAllItems();
    int daysInMonth = yearMonth.lengthOfMonth();
    for (int d = 1; d <= daysInMonth; d++) {
      dayCombo.addItem(d);
    }
  }

  private static class DateTimeComponents {
    final int year;
    final String monthStr;
    final int day;
    final String hour;
    final String minute;

    DateTimeComponents(int year, String monthStr, int day, String hour, String minute) {
      this.year = year;
      this.monthStr = monthStr;
      this.day = day;
      this.hour = hour;
      this.minute = minute;
    }
  }

  private DateTimeComponents extractDateTimeComponents(JPanel panel, boolean includeTime)
          throws IllegalStateException {
    Component yearComp = panel.getComponent(1);
    Component monthComp = panel.getComponent(2);
    Component dayComp = panel.getComponent(3);

    if (!(yearComp instanceof JTextField) || !(monthComp instanceof JComboBox)
            || !(dayComp instanceof JComboBox)) {
      throw new IllegalStateException("Invalid panel component types");
    }

    JTextField yearField = (JTextField) yearComp;
    @SuppressWarnings("unchecked")
    JComboBox<String> monthCombo = (JComboBox<String>) monthComp;
    @SuppressWarnings("unchecked")
    JComboBox<Integer> dayCombo = (JComboBox<Integer>) dayComp;

    String yearText = yearField.getText().trim();
    Object monthObj = monthCombo.getSelectedItem();
    Object dayObj = dayCombo.getSelectedItem();

    if (yearText.isEmpty() || monthObj == null || dayObj == null) {
      throw new IllegalStateException("All date fields must be selected");
    }

    int year;
    try {
      year = Integer.parseInt(yearText);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid year format");
    }
    String monthStr = (String) monthObj;
    int day = (Integer) dayObj;

    if (includeTime) {
      Component hourComp = panel.getComponent(4);
      Component minuteComp = panel.getComponent(6);

      if (!(hourComp instanceof JComboBox) || !(minuteComp instanceof JComboBox)) {
        throw new IllegalStateException("Invalid panel component types for time");
      }

      @SuppressWarnings("unchecked")
      JComboBox<String> hourCombo = (JComboBox<String>) hourComp;
      @SuppressWarnings("unchecked")
      JComboBox<String> minuteCombo = (JComboBox<String>) minuteComp;

      Object hourObj = hourCombo.getSelectedItem();
      Object minuteObj = minuteCombo.getSelectedItem();

      if (hourObj == null || minuteObj == null) {
        throw new IllegalStateException("All time fields must be selected");
      }

      return new DateTimeComponents(year, monthStr, day, (String) hourObj, (String) minuteObj);
    }

    return new DateTimeComponents(year, monthStr, day, null, null);
  }

  private String getDateTimeFromPanel(JPanel panel) throws IllegalStateException {
    DateTimeComponents components = extractDateTimeComponents(panel, true);
    Month month = Month.valueOf(components.monthStr);
    return String.format("%04d-%02d-%02dT%s:%s",
            components.year, month.getValue(), components.day, components.hour, components.minute);
  }

  private String getDateFromPanel(JPanel panel) throws IllegalStateException {
    DateTimeComponents components = extractDateTimeComponents(panel, false);
    Month month = Month.valueOf(components.monthStr);
    return String.format("%04d-%02d-%02d", components.year, month.getValue(), components.day);
  }

  private void createEventDialog() {
    String[] options = {"Create Single Event", "Create Recurring Event"};
    int choice = JOptionPane.showOptionDialog(frame, "Select event type:",
            "Create Event", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

    if (choice == -1) {
      return;
    }

    if (choice == 0) {
      createSingleEventDialog();
    } else {
      createRecurringEventDialog();
    }
  }

  private static class EventCreationComponents {
    final JTextField nameField;
    final JPanel startPanel;
    final JPanel endPanel;
    final JPanel panel;

    EventCreationComponents(JTextField nameField, JPanel startPanel, JPanel endPanel,
                            JPanel panel) {
      this.nameField = nameField;
      this.startPanel = startPanel;
      this.endPanel = endPanel;
      this.panel = panel;
    }
  }

  private EventCreationComponents setupEventCreationDialog() {
    JPanel panel = createMainPanel();
    JTextField nameField = createTextField();
    panel.add(createSubPanel(new JLabel("Event Name:"), nameField));
    LocalDateTime now = LocalDateTime.now();
    JPanel startPanel = createDateTimePanel("Start:", now);
    panel.add(createSubPanel(startPanel));
    JPanel endPanel = createDateTimePanel("End:", now.plusHours(1));
    panel.add(createSubPanel(endPanel));
    return new EventCreationComponents(nameField, startPanel, endPanel, panel);
  }

  private void createSingleEventDialog() {
    EventCreationComponents components = setupEventCreationDialog();
    JScrollPane scrollPane = createScrollPane(components.panel, 200);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane, "Create Single Event",
            JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String name = components.nameField.getText().trim();
        if (name.isEmpty()) {
          displayMessage("Event name is missing.");
          return;
        }

        String startStr = getDateTimeFromPanel(components.startPanel);
        String endStr = getDateTimeFromPanel(components.endPanel);

        controller.createSingleEvent(name, startStr, endStr);
        displayMessage("Event '" + name + "' created successfully");
        updateDisplay();
      } catch (Exception e) {
        displayMessage("Error creating : " + e.getMessage());
      }
    }
  }

  private void createRecurringEventDialog() {
    EventCreationComponents components = setupEventCreationDialog();

    JPanel recurringPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JCheckBox[] dayChecks = new JCheckBox[7];
    String[] days = {"M", "T", "W", "R", "F", "S", "U"};
    for (int i = 0; i < 7; i++) {
      dayChecks[i] = new JCheckBox(days[i]);
      recurringPanel.add(dayChecks[i]);
    }
    components.panel.add(createSubPanel(new JLabel("Recurring Days:"), recurringPanel));

    JTextField countField = createTextField();
    countField.setText("");
    JPanel countPanel = createSubPanel(new JLabel("Repeat Count:"), countField);

    LocalDateTime now = LocalDateTime.now();
    JPanel untilPanel = createDateTimePanel("Until Date:", now);
    JPanel untilWrapper = createSubPanel(untilPanel);

    JRadioButton forTimesRadio = new JRadioButton("For N Times", true);
    JRadioButton untilRadio = new JRadioButton("Until Date");
    ButtonGroup repeatGroup = new ButtonGroup();
    repeatGroup.add(forTimesRadio);
    repeatGroup.add(untilRadio);

    components.panel.add(createSubPanel(forTimesRadio));
    components.panel.add(countPanel);
    components.panel.add(createSubPanel(untilRadio));
    components.panel.add(untilWrapper);

    countPanel.setVisible(true);
    untilWrapper.setVisible(false);

    forTimesRadio.addActionListener(e -> {
      countPanel.setVisible(true);
      untilWrapper.setVisible(false);
    });

    untilRadio.addActionListener(e -> {
      countPanel.setVisible(false);
      untilWrapper.setVisible(true);
    });

    JScrollPane scrollPane = createScrollPane(components.panel, 400);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane, "Create Recurring Event",
            JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String name = components.nameField.getText().trim();
        if (name.isEmpty()) {
          displayMessage("Event name is missing");
          return;
        }

        String startStr = getDateTimeFromPanel(components.startPanel);
        String endStr = getDateTimeFromPanel(components.endPanel);
        LocalDateTime startTime = LocalDateTime.parse(startStr, TIME_FORMAT);
        LocalDateTime endTime = LocalDateTime.parse(endStr, TIME_FORMAT);

        if (!startTime.toLocalDate().equals(endTime.toLocalDate())) {
          throw new Exception("Single event on a recurring series must span one day only");
        }

        StringBuilder daysStr = new StringBuilder();
        for (JCheckBox cb : dayChecks) {
          if (cb.isSelected()) {
            daysStr.append(cb.getText());
          }
        }
        if (daysStr.length() == 0) {
          throw new Exception("No days selected for recurring event.");
        }

        String repeatRule;
        if (forTimesRadio.isSelected()) {
          String countStr = countField.getText().trim();
          int count;
          try {
            count = Integer.parseInt(countStr);
            if (count <= 0) {
              throw new NumberFormatException();
            }
          } catch (NumberFormatException e) {
            throw new Exception("Repeat count must be a positive integer.");
          }
          repeatRule = daysStr + " for " + count + " times";
          controller.createRecurringEvent(name, startStr, endStr, repeatRule);
          displayMessage("Recurring event '" + name + "' created successfully");
        } else {
          String untilStr = getDateTimeFromPanel(untilPanel);
          repeatRule = daysStr + " until " + untilStr;
          controller.createRecurringEvent(name, startStr, endStr, repeatRule);
          displayMessage("Recurring event '" + name + "' created successfully");
        }
        updateDisplay();
      } catch (Exception e) {
        displayMessage("Error creating : " + e.getMessage());
      }
    }
  }

  private List<String> getUniqueEventNames() {
    try {
      List<IEvent> events = calendarManager.getCurrentCalendar().getEventScheduler()
              .retrieveAllEvents();
      return events.stream()
              .map(IEvent::getEventName)
              .distinct()
              .sorted()
              .collect(Collectors.toList());
    } catch (Exception e) {
      displayMessage("Error : " + e.getMessage());
      return new ArrayList<>();
    }
  }

  private void editEventDialog() {
    String[] options = {"Edit Single Event", "Edit Multiple from Date", "Edit All Instances"};
    int choice = JOptionPane.showOptionDialog(frame, "Select the type of edit:",
            "Edit Event", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

    if (choice == -1) {
      return;
    }

    try {
      Calendar cal = calendarManager.getCurrentCalendar();
      List<IEvent> events = cal.getEventScheduler().retrieveAllEvents();

      switch (choice) {
        case 0:
          editSingleEventDialog(events, null);
          break;
        case 1:
          editMultipleFromDateDialog();
          break;
        case 2:
          editAllInstancesDialog();
          break;
        default:
          break;
      }
    } catch (Exception e) {
      displayMessage("Error editing : " + e.getMessage());
    }
  }

  private JPanel createEventSelectionPanel(JComboBox<String> eventCombo, String labelText) {
    JPanel panel = createMainPanel();
    panel.add(createSubPanel(new JLabel(labelText)));
    panel.add(createSubPanel(eventCombo));
    return panel;
  }

  private JPanel createEditEventPanel(JComboBox<String> propCombo, JTextField valueField) {
    JPanel panel = createMainPanel();
    panel.add(createSubPanel(new JLabel("Property to edit:")));
    panel.add(createSubPanel(propCombo));
    panel.add(createSubPanel(new JLabel("New Value:")));
    panel.add(createSubPanel(valueField));
    return panel;
  }

  private String getSelectedEventName(JComboBox<String> eventCombo) {
    String selected = (String) eventCombo.getSelectedItem();
    if (selected == null || selected.trim().isEmpty()) {
      displayMessage("Please select an event name.");
      return null;
    }
    return selected;
  }

  private void editSingleEventDialog(List<IEvent> events, LocalDate selectedDate) {
    List<String> uniqueEventNames = events.stream()
            .map(IEvent::getEventName)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    if (uniqueEventNames.isEmpty()) {
      displayMessage(NO_EVENTS_MESSAGE);
      return;
    }

    JComboBox<String> eventNameCombo = createEventNameComboBox(uniqueEventNames);
    if (eventNameCombo == null) {
      return;
    }

    JPanel panel = createMainPanel();
    panel.add(createEventSelectionPanel(eventNameCombo, "Event to edit:"));

    JComboBox<String> timeCombo = new JComboBox<>();
    timeCombo.setPreferredSize(new Dimension(200, 25));
    panel.add(createSubPanel(new JLabel(selectedDate != null
            ? "Time of event:" : "Date and time of event:"), timeCombo));

    String[] properties = {"Name", "Description", "Location", "Public"};
    JComboBox<String> propCombo = new JComboBox<>(properties);
    JTextField valueField = createTextField();
    panel.add(createEditEventPanel(propCombo, valueField));

    eventNameCombo.addActionListener(e -> {
      String selectedName = getSelectedEventName(eventNameCombo);
      if (selectedName != null) {
        List<IEvent> filteredEvents = events.stream()
                .filter(event -> event.getEventName().equals(selectedName))
                .collect(Collectors.toList());
        timeCombo.removeAllItems();
        for (IEvent event : filteredEvents) {
          String timeText = selectedDate != null
                  ?
                  String.format("%s to %s",
                          event.getStart().toLocalDateTime().format(DISPLAY_TIME_FORMAT),
                          event.getEnd().toLocalDateTime().format(DISPLAY_TIME_FORMAT)) :
                  String.format("%s %s to %s",
                          event.getStart().toLocalDate().format(DATE_FORMAT),
                          event.getStart().toLocalDateTime().format(DISPLAY_TIME_FORMAT),
                          event.getEnd().toLocalDateTime().format(DISPLAY_TIME_FORMAT));
          timeCombo.addItem(timeText);
        }
      }
    });

    if (!uniqueEventNames.isEmpty()) {
      eventNameCombo.setSelectedIndex(0);
    }

    JScrollPane scrollPane = createScrollPane(panel, 250);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane, "Edit Single Event",
            JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      String selectedName = getSelectedEventName(eventNameCombo);
      String selectedTime = (String) timeCombo.getSelectedItem();
      if (selectedName == null || selectedTime == null) {
        displayMessage("Please select both an event and a time.");
        return;
      }

      IEvent selectedEvent = null;
      for (IEvent event : events) {
        String timeText = selectedDate != null
                ?
                String.format("%s to %s",
                        event.getStart().toLocalDateTime().format(DISPLAY_TIME_FORMAT),
                        event.getEnd().toLocalDateTime().format(DISPLAY_TIME_FORMAT)) :
                String.format("%s %s to %s",
                        event.getStart().toLocalDate().format(DATE_FORMAT),
                        event.getStart().toLocalDateTime().format(DISPLAY_TIME_FORMAT),
                        event.getEnd().toLocalDateTime().format(DISPLAY_TIME_FORMAT));
        if (event.getEventName().equals(selectedName) && timeText.equals(selectedTime)) {
          selectedEvent = event;
          break;
        }
      }

      if (selectedEvent == null) {
        displayMessage("Selected event not found.");
        return;
      }

      String property = (String) propCombo.getSelectedItem();
      String newValue = valueField.getText().trim();

      if (newValue.isEmpty()) {
        displayMessage("New value cannot be empty.");
        return;
      }

      try {
        controller.editSingleEvent(property, selectedEvent.getEventName(),
                selectedEvent.getStart().toLocalDateTime().format(TIME_FORMAT),
                selectedEvent.getEnd().toLocalDateTime().format(TIME_FORMAT), newValue);
        displayMessage("Event '" + selectedEvent.getEventName() + "' updated successfully.");
        updateDisplay();
      } catch (Exception e) {
        displayMessage("Error editing : " + e.getMessage());
      }
    }
  }

  private JComboBox<String> getEventComboBoxForEdit() {
    List<String> eventNames = getUniqueEventNames();
    if (eventNames.isEmpty()) {
      displayMessage(NO_EVENTS_MESSAGE);
      return null;
    }
    return createEventNameComboBox(eventNames);
  }

  private static class EditEventComponents {
    final JComboBox<String> eventCombo;
    final JComboBox<String> propCombo;
    final JTextField valueField;
    final JPanel panel;

    EditEventComponents(JComboBox<String> eventCombo, JComboBox<String> propCombo,
                        JTextField valueField, JPanel panel) {
      this.eventCombo = eventCombo;
      this.propCombo = propCombo;
      this.valueField = valueField;
      this.panel = panel;
    }
  }

  private EditEventComponents setupEditEventDialogComponents() {
    JComboBox<String> eventCombo = getEventComboBoxForEdit();
    if (eventCombo == null) {
      return null;
    }

    String[] properties = {"Name", "Description", "Location", "Public"};
    JComboBox<String> propCombo = new JComboBox<>(properties);
    JTextField valueField = createTextField();

    JPanel panel = createMainPanel();
    panel.add(createEventSelectionPanel(eventCombo, "Event Name (type to search):"));
    panel.add(createEditEventPanel(propCombo, valueField));

    return new EditEventComponents(eventCombo, propCombo, valueField, panel);
  }

  private static class EditValidationResult {
    final String eventName;
    final String property;
    final String newValue;

    EditValidationResult(String eventName, String property, String newValue) {
      this.eventName = eventName;
      this.property = property;
      this.newValue = newValue;
    }

    boolean isInvalid() {
      return eventName == null || property == null || newValue.isEmpty();
    }
  }

  private EditValidationResult validateEditEventInputs(EditEventComponents components) {
    String eventName = getSelectedEventName(components.eventCombo);
    String property = (String) components.propCombo.getSelectedItem();
    String newValue = components.valueField.getText().trim();

    if (eventName == null) {
      return new EditValidationResult(null, null,
              null);
    }
    if (newValue.isEmpty()) {
      displayMessage("New value cannot be empty.");
      return new EditValidationResult(null, null, null);
    }

    return new EditValidationResult(eventName, property, newValue);
  }

  private void editMultipleFromDateDialog() {
    EditEventComponents components = setupEditEventDialogComponents();
    if (components == null) {
      return;
    }

    JPanel fromPanel = createDateTimePanel("From Date:", LocalDateTime.now());
    components.panel.add(createSubPanel(fromPanel));

    JScrollPane scrollPane = createScrollPane(components.panel, 250);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane, "Edit Multiple from Date",
            JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      EditValidationResult validation = validateEditEventInputs(components);
      if (validation.isInvalid()) {
        return;
      }

      String fromStr = getDateTimeFromPanel(fromPanel);

      try {
        controller.editMultipleEvents(validation.property, validation.eventName, fromStr,
                validation.newValue);
        displayMessage("Multiple events '" + validation.eventName + "' updated from "
                + fromStr + ".");
        updateDisplay();
      } catch (Exception e) {
        displayMessage("Error editing : " + e.getMessage());
      }
    }
  }

  private void editAllInstancesDialog() {
    EditEventComponents components = setupEditEventDialogComponents();
    if (components == null) {
      return;
    }

    JScrollPane scrollPane = createScrollPane(components.panel, 200);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane, "Edit All Instances",
            JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      EditValidationResult validation = validateEditEventInputs(components);
      if (validation.isInvalid()) {
        return;
      }

      try {
        int count = calendarManager.getCurrentCalendar().getEventScheduler()
                .updateEventsByName(validation.property, validation.eventName, validation.newValue);
        displayMessage(count + " events '" + validation.eventName + "' updated successfully.");
        updateDisplay();
      } catch (Exception e) {
        displayMessage("Error editing : " + e.getMessage());
      }
    }
  }

  private void editEventsForDay(LocalDate date) {
    try {
      Calendar cal = calendarManager.getCurrentCalendar();
      List<IEvent> events = cal.getEventScheduler().fetchEventsOnDate(date);
      editSingleEventDialog(events, date);
    } catch (Exception e) {
      displayMessage("Error editing : " + e.getMessage());
    }
  }

  private void copyEvents() {
    String[] options = {"Copy Single Event", "Copy Events on Date", "Copy Events Between Dates"};
    int choice = JOptionPane.showOptionDialog(frame, "Select copy option:",
            "Copy Events", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

    try {
      switch (choice) {
        case 0:
          copySingleEvent();
          break;
        case 1:
          copyEventsOnDate();
          break;
        case 2:
          copyEventsBetweenDates();
          break;
        default:
          break;
      }
    } catch (Exception e) {
      displayMessage("Error copying : " + e.getMessage());
    }
  }

  private ZonedDateTime adjustEventStartTime(IEvent sourceEvent,
                                             long daysDiff, ZoneId targetZone) {
    return sourceEvent.getStart().plusDays(daysDiff).withZoneSameInstant(targetZone);
  }

  private void copyEventToCalendar(IEvent sourceEvent, ZonedDateTime newStart, Calendar targetCal)
          throws Exception {
    long secondsDuration = java.time.Duration.between(sourceEvent.getStart(),
            sourceEvent.getEnd()).getSeconds();
    ZonedDateTime newEnd = newStart.plusSeconds(secondsDuration);
    IEvent newEvent = targetCal.getEventScheduler().createEvent(sourceEvent.getEventName(),
            newStart, newEnd, sourceEvent.isFullDay());
    newEvent.setDescription(sourceEvent.getDescription());
    newEvent.setLocation(sourceEvent.getLocation());
    newEvent.setPublic(sourceEvent.isPublic());
    targetCal.getEventScheduler().scheduleEvent(newEvent);
  }

  private void copySingleEvent() throws Exception {
    JPanel panel = createMainPanel();

    JTextField eventNameField = createTextField();
    panel.add(createSubPanel(new JLabel("Event Name:"), eventNameField));

    JPanel sourcePanel = createDateTimePanel("Source Date:", LocalDateTime.now());
    panel.add(createSubPanel(sourcePanel));

    String[] calendarNames = calendarManager.getCalendars().keySet().toArray(new String[0]);
    JComboBox<String> targetCalCombo = new JComboBox<>(calendarNames);
    panel.add(createSubPanel(new JLabel("Target Calendar:"), targetCalCombo));

    JPanel targetPanel = createDateTimePanel("Target Date:", LocalDateTime.now());
    panel.add(createSubPanel(targetPanel));

    JScrollPane scrollPane = createScrollPane(panel, 250);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane,
            "Copy Single Event", JOptionPane.OK_CANCEL_OPTION);
    if (result == JFileChooser.APPROVE_OPTION) {
      String eventName = eventNameField.getText().trim();
      String sourceDateTime = getDateTimeFromPanel(sourcePanel);
      String targetCalName = (String) targetCalCombo.getSelectedItem();
      String targetDateTime = getDateTimeFromPanel(targetPanel);

      Calendar sourceCal = calendarManager.getCurrentCalendar();
      Calendar targetCal = calendarManager.getCalendar(targetCalName);
      LocalDateTime sourceStart = LocalDateTime.parse(sourceDateTime, TIME_FORMAT);
      LocalDateTime targetStart = LocalDateTime.parse(targetDateTime, TIME_FORMAT);

      IEvent sourceEvent = sourceCal.getEventScheduler().retrieveAllEvents().stream()
              .filter(e -> e.getEventName().equals(eventName)
                      && e.getStart().toLocalDateTime().equals(sourceStart))
              .findFirst()
              .orElseThrow(() -> new Exception("Event not found"));

      ZonedDateTime newStart = targetStart.atZone(targetCal.getTimezone());
      copyEventToCalendar(sourceEvent, newStart, targetCal);
      updateDisplay();
      displayMessage("Event '" + eventName + "' copied to " + targetCalName + " at "
              + targetDateTime + ".");
    }
  }

  private void copyEventsOnDate() throws Exception {
    JPanel panel = createMainPanel();

    JPanel sourcePanel = createDatePanel("Source Date:", LocalDate.now());
    panel.add(createSubPanel(sourcePanel));

    String[] calendarNames = calendarManager.getCalendars().keySet().toArray(new String[0]);
    JComboBox<String> targetCalCombo = new JComboBox<>(calendarNames);
    panel.add(createSubPanel(new JLabel("Target Calendar:"), targetCalCombo));

    JPanel targetPanel = createDatePanel("Target Date:", LocalDate.now());
    panel.add(createSubPanel(targetPanel));

    JScrollPane scrollPane = createScrollPane(panel, 200);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane,
            "Copy Events on Date", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      String sourceDateStr = getDateFromPanel(sourcePanel);
      String targetCalName = (String) targetCalCombo.getSelectedItem();
      String targetDateStr = getDateFromPanel(targetPanel);

      LocalDate sourceDate = LocalDate.parse(sourceDateStr, DATE_FORMAT);
      LocalDate targetDate = LocalDate.parse(targetDateStr, DATE_FORMAT);

      Calendar sourceCal = calendarManager.getCurrentCalendar();
      Calendar targetCal = calendarManager.getCalendar(targetCalName);
      List<IEvent> sourceEvents = sourceCal.getEventScheduler()
              .fetchEventsStartingOnDate(sourceDate);

      for (IEvent sourceEvent : sourceEvents) {
        long daysDiff = ChronoUnit.DAYS.between(sourceEvent.getStart()
                .toLocalDate(), targetDate);
        ZonedDateTime newStart = adjustEventStartTime(sourceEvent, daysDiff,
                targetCal.getTimezone());
        copyEventToCalendar(sourceEvent, newStart, targetCal);
      }
      updateDisplay();
      displayMessage(sourceEvents.size() + " events copied to " + targetCalName + " on "
              + targetDateStr + ".");
    }
  }

  private void copyEventsBetweenDates() throws Exception {
    JPanel panel = createMainPanel();

    JPanel startPanel = createDatePanel("Start Date:", LocalDate.now());
    panel.add(createSubPanel(startPanel));

    JPanel endPanel = createDatePanel("End Date:", LocalDate.now().plusDays(1));
    panel.add(createSubPanel(endPanel));

    String[] calendarNames = calendarManager.getCalendars().keySet().toArray(new String[0]);
    JComboBox<String> targetCalCombo = new JComboBox<>(calendarNames);
    panel.add(createSubPanel(new JLabel("Target Calendar:"), targetCalCombo));

    JPanel targetPanel = createDatePanel("Target Start Date:", LocalDate.now());
    panel.add(createSubPanel(targetPanel));

    JScrollPane scrollPane = createScrollPane(panel, 250);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane,
            "Copy Events Between Dates", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      String startDateStr = getDateFromPanel(startPanel);
      String endDateStr = getDateFromPanel(endPanel);
      String targetCalName = (String) targetCalCombo.getSelectedItem();
      String targetStartDateStr = getDateFromPanel(targetPanel);

      LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMAT);
      LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMAT);
      LocalDate targetStartDate = LocalDate.parse(targetStartDateStr, DATE_FORMAT);

      Calendar sourceCal = calendarManager.getCurrentCalendar();
      Calendar targetCal = calendarManager.getCalendar(targetCalName);
      ZonedDateTime rangeStart = startDate.atStartOfDay(sourceCal.getTimezone());
      ZonedDateTime rangeEnd = endDate.atTime(23, 59, 59)
              .atZone(sourceCal.getTimezone());
      List<IEvent> events = sourceCal.getEventScheduler().fetchEventsInRange(rangeStart, rangeEnd);

      long daysBetween = ChronoUnit.DAYS.between(startDate, targetStartDate);
      for (IEvent event : events) {
        ZonedDateTime newStart = adjustEventStartTime(event, daysBetween, targetCal.getTimezone());
        copyEventToCalendar(event, newStart, targetCal);
      }
      updateDisplay();
      displayMessage(events.size() + " events copied to " + targetCalName + " starting "
              + targetStartDateStr + ".");
    }
  }

  private void checkAvailability() {
    JPanel panel = createMainPanel();

    JPanel timePanel = createDateTimePanel("Check Time:", LocalDateTime.now());
    panel.add(createSubPanel(timePanel));

    JScrollPane scrollPane = createScrollPane(panel, 150);

    int result = JOptionPane.showConfirmDialog(frame, scrollPane,
            "Check Availability", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String timeStr = getDateTimeFromPanel(timePanel);
        ZonedDateTime time = LocalDateTime.parse(timeStr, TIME_FORMAT)
                .atZone(calendarManager.getCurrentCalendar().getTimezone());
        boolean isOccupied = calendarManager.getCurrentCalendar().getEventScheduler()
                .isOccupiedAt(time);
        displayMessage("Status at " + timeStr + ": " + (isOccupied ? "Busy" : "Available"));
      } catch (Exception e) {
        displayMessage("Error : " + e.getMessage());
      }
    }
  }

  private String formatEventList(List<IEvent> events, String header, boolean singleDay) {
    StringBuilder eventList = new StringBuilder(header + "\n");
    if (events.isEmpty()) {
      eventList.append(singleDay ? "No events." : "No events in this range.");
    } else {
      for (IEvent event : events) {
        String startTime = event.getStart().toLocalDateTime().format(DISPLAY_TIME_FORMAT);
        String endTime = event.getEnd().toLocalDateTime().format(DISPLAY_TIME_FORMAT);
        String startDate = event.getStart().toLocalDate().format(DATE_FORMAT);
        String endDate = event.getEnd().toLocalDate().format(DATE_FORMAT);

        eventList.append("- ").append(event.getEventName())
                .append(" from (").append(startDate).append(") ").append(startTime)
                .append(" to (").append(endDate).append(") ").append(endTime)
                .append(", ").append(event.isPublic() ? "Public" : "Private");

        String location = event.getLocation();
        if (location != null && !location.trim().isEmpty()) {
          eventList.append(", Location: ").append(location);
        }

        String description = event.getDescription();
        if (description != null && !description.trim().isEmpty()) {
          eventList.append(", Description: ").append(description);
        }

        eventList.append("\n");
      }
    }
    return eventList.toString();
  }

  private void showDayOptions(LocalDate date) {
    try {
      Calendar cal = calendarManager.getCurrentCalendar();
      List<IEvent> events = cal.getEventScheduler().fetchEventsOnDate(date);
      String eventListText = formatEventList(events, "Events :",
              true);

      JTextArea textArea = new JTextArea(eventListText, 15, 40);
      textArea.setEditable(false);
      JScrollPane scrollPane = new JScrollPane(textArea);
      String[] options = {"Ok", "Edit Events"};
      int choice = JOptionPane.showOptionDialog(frame, scrollPane,
              "Events", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
              null, options, options[0]);

      if (choice == 1) {
        editEventsForDay(date);
      }
    } catch (Exception e) {
      displayMessage("Error : " + e.getMessage());
    }
  }

  private void exportCalendar() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV Files", "csv"));
    if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String fileName = chooser.getSelectedFile().getPath();
      if (!fileName.endsWith(".csv")) {
        fileName += ".csv";
      }
      try {
        controller.exportCalendar(fileName);
        displayMessage("Exported to " + fileName);
      } catch (Exception e) {
        displayMessage("Error exporting : " + e.getMessage());
      }
    }
  }

  private void importCalendar() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV Files", "csv"));
    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String fileName = chooser.getSelectedFile().getPath();
      try {
        controller.importCalendar(fileName);
        updateDisplay();
        displayMessage("Imported from " + fileName);
      } catch (Exception e) {
        displayMessage("Error importing : " + e.getMessage());
      }
    }
  }

  @Override
  public void displayMessage(String message) {
    JOptionPane.showMessageDialog(frame, message);
  }

  @Override
  public void startInteractiveMode() {
    frame.setVisible(true);
  }
}