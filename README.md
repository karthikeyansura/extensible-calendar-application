# Virtual Calendar Application

This project implements a virtual calendar application with functionality similar to modern calendar tools such as Google Calendar. It supports creating, editing, querying, and exporting both single and recurring events. The application allows management of multiple calendars across different time zones and includes the ability to copy events between calendars.

It uses a clean, modular design emphasizing correctness, extensibility, and clarity.

## Table of Contents

- [Introduction](#introduction)
- [New Features (Assignment 6)](#new-features-assignment-6)
- [Previous Features](#previous-features)
- [Running the Application](#running-the-application)
- [Additional Notes](#additional-notes)

---

## Introduction
The goal of this project was to design and implement a fully functional calendar application in stages over the semester.  

It supports:
- Creating single, all-day, and recurring events  
- Editing and managing events with conflict detection  
- Querying and exporting calendar data to a CSV file (Google Calendar compatible)  
- Running the application in multiple modes (interactive, headless, GUI)  

Timezone support and multiple calendar management were introduced in later stages, along with a graphical user interface built with Java Swing.

---

## New Features (Assignment 6)

### Graphical User Interface
- Built with Java Swing for intuitive use.
- Month and day views with navigation controls.
- Dialogs for creating and editing events.

### Calendar Management in GUI
- Create calendars with specific timezones.
- Switch calendars via dropdown selection.
- View and edit calendar properties (name, timezone).

### Event Operations in GUI
- Display all events for a given day.
- Create and edit single or recurring events.
- Conflict detection integrated into the editing process.
- Options for handling recurring event updates.

### Import Functionality
- Import events from Google Calendar–compatible CSV files.
- Feedback provided on number of imported events.

---

## Previous Features

### Assignment 5

#### Multiple Calendar Support
- Create and manage multiple calendars with unique timezones.
- Switch calendars dynamically.

```
create calendar --name <calName> --timezone <timezone>
use calendar --name <calName>
```

#### Timezone Support
- Each calendar maintains its own timezone.
- Events shift appropriately when the calendar’s timezone changes.

```
edit calendar --name <calName> --property name <newName>
edit calendar --name <calName> --property timezone <newTimezone>
```

#### Copy Events Between Calendars
```
copy event <eventName> on <sourceDateTime> --target <targetCalendar> to <targetDateTime>
copy events on <sourceDate> --target <targetCalendar> to <targetDate>
copy events between <startDate> and <endDate> --target <targetCalendar> to <targetDate>
```

#### Enhanced Export
- Export logic refactored for cleaner design.
- Controller handles I/O separately from core models.
- Strategy Pattern applied for flexible export options.

---

### Assignment 4

#### Create Single Event
```
create event [--autoDecline] <eventName> from <dateTime> to <dateTime>
```

#### Create All-Day Event
```
create event [--autoDecline] <eventName> on <date>
```

#### Create Recurring Events
```
create event [--autoDecline] <eventName> from <dateTime> to <dateTime> repeats <weekdays> for <N> times
create event [--autoDecline] <eventName> from <dateTime> to <dateTime> repeats <weekdays> until <dateTime>
create event <eventName> on <date> repeats <weekdays> for <N> times
create event <eventName> on <date> repeats <weekdays> until <date>
```

#### Edit Events
- Edit single or recurring events.
- Update properties such as `name`, `startDateTime`, `endDateTime`, `description`, `location`, `isPublic`, `occurrences`, `weekdays`, `untilDateTime`.

```
edit event <property> <eventName> from <dateTime> to <dateTime> with <NewValue>
edit events <property> <eventName> from <dateTime> with <NewValue>
edit events <property> <eventName> <NewValue>
```

#### Query Calendar
```
print events on <date>
print events from <dateTime> to <dateTime>
```

#### Check Availability
```
show status on <dateTime>
```

#### Export Calendar
- Exports to CSV, showing the absolute filepath.

#### Modes of Operation
- **Interactive Mode**: Enter commands manually. Exit with `exit`.
- **Headless Mode**: Execute commands from a file (ending with `exit`).
- **GUI Mode**: Default graphical interface.

---

## Running the Application

### Prerequisites
- Java Development Kit (JDK) 11+

### Setup
1. Open the project in an IDE (e.g., IntelliJ).
2. Use `CalendarApp.java` as the entry point.

### Running from Artifact

The compiled JAR file `calendarApp.jar` is available in the `out/artifacts/application_jar/` directory. You can run the application directly from this JAR:

```
java -jar out/artifacts/application_jar/calendarApp.jar
```

### Running
From the compiled JAR file:

- **GUI Mode (default)**:
```bash
java -jar calendarApp.jar
```

- **Interactive Mode**:
```bash
java -jar calendarApp.jar --mode interactive
```

- **Headless Mode**:
```bash
java -jar calendarApp.jar --mode headless commands.txt
```

---

## Additional Notes
- Testing and documentation were completed at each stage.  
- The project emphasizes modularity and maintainability.
