# Packaging Management System

A local Spring Boot web application for packaging factory attendance, production entry, summaries, and reports.

## Requirements

- Java 17 or later. The project compiles with Java 17 language/API compatibility (`maven.compiler.release` is set by Spring Boot from `java.version`).
- Maven 3.6.3 or later, or IntelliJ IDEA's Maven integration.
- The project uses Spring Boot 3.4.2, Thymeleaf, Spring JDBC, and H2. No Excel or PDF library is needed.

This workspace has Java 8 on the system `PATH`, while IntelliJ IDEA includes JDK 25. Spring Boot 3 requires Java 17 or later to run. Select the IDE's bundled JDK 25 (or another approved JDK 17+) as the project and run JDK. The Maven compiler still targets Java 17. Maven 3.9.6 is cached locally in this workspace, but `mvn` is not currently on `PATH`; use IntelliJ's Maven tool window or configure an already approved Maven installation for terminal commands. No software was installed by this project.

## Project structure

```text
pom.xml
src/main/java/com/packaging/
  controller/       Web routes for the four modules
  model/            Shared view models
  repository/       Parameterized JDBC queries
  service/          Validation and business calculations
src/main/resources/
  schema.sql        Persistent H2 schema and indexes
  data.sql          Four teams, sample operators, sample attendance/production, markets
  templates/        Thymeleaf screens
  static/css/       Plain shared styling
src/test/java/      Business and web integration tests
```

## Database

H2 stores data persistently at `%USERPROFILE%\.packaging-management\packaging.mv.db` by default. The application creates the parent folder on startup. Set `PACKAGING_DB_PATH` to change the database base path; for example, set it to `D:/PackagingData/packaging` (omit the `.mv.db` suffix). Schema initialization is automatic. The four teams are initialized as `Team 1` through `Team 4`. When the database has no business records, one clearly labeled demo dataset is added: eight `DEMO-` sample operators, `DEMO-` parts and marks, attendance for today, and production entries with `DEMO SAMPLE` remarks. These names and quantities are fictional and must be replaced before operational use. Existing non-empty databases are not populated with sample records, and the seed marker prevents sample data from being re-added on later starts.

## Run

From a terminal with an approved Java 17+ and Maven installation:

```powershell
mvn clean test
mvn spring-boot:run
```

Then open `http://localhost:8080`. In IntelliJ, open the project folder or `pom.xml`, load the Maven project, select a JDK 17+ project SDK, then run `com.packaging.PackagingApplication` or the `spring-boot:run` Maven goal. In VS Code, open the same project folder and run the same Maven commands from its terminal.

## Modules and behavior

- **Attendance** — select a team and date, enter the employee number and name, choose P (Present) or A (Absent), and submit. Employee details are added or updated automatically. Submitting the same employee/date updates that single attendance record. Team and employee setup remains available in the expandable configuration section.
- **Production Entry** — select or type an operator, select a configured part and market, enter a non-negative whole-number quantity, and submit. AA, KIT, Main, and Htz are configured as starter market options. Saved rows can be edited or deleted after confirmation; Sum/View Total recalculates the matching total.
- **Summary** — calculates manpower, present/absent counts, attendance percentage, market-wise production, and grand total from saved records for the selected date. For this workflow, manpower availability means employees marked Present; productivity is total production divided by present employees, or 0 when no employees are present.
- **Report** — loads the full saved production list by default and filters by date range, operator, part, and market. The report columns are Name, Part Number, Market, and Quantity. Excel exports from Summary and Report are Excel-readable SpreadsheetML `.xls` workbooks containing the same filtered data. Print Report opens a print view; choose “Save as PDF” in the browser print dialog.

The handwritten names are not used. Starter employee and production records are explicitly labeled as fictional demo values; replace them with actual factory data before operational use. The market label is represented internally by the existing mark lookup table to avoid a destructive database migration. Production shift remains optional for historical compatibility and is not required in the current workflow. No machine field is used.

## Troubleshooting

- **Java version error:** run with JDK 17 or later. The system default Java 8 cannot run Spring Boot 3.
- **`mvn` is not recognized:** use the IntelliJ Maven tool window or point the terminal at an already approved Maven installation. This project does not include a wrapper that downloads Maven.
- **Port 8080 is busy:** stop the other service or run with `--server.port=8081`.
- **Database locked:** close other application instances using the same database, then restart. Do not delete the H2 files to resolve a lock if the records need to be preserved.
- **Dependency resolution fails:** use your company's configured Maven repository and network policy. No repository or proxy settings are changed by this project.
