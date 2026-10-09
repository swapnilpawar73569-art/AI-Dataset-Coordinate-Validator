# 📍 AI Dataset Coordinate Validator & Geographic Anomaly Detector
### Pure Java 17 Object-Oriented Programming (OOP) Project

A clean, standard, 100% pure Java project that ingests, validates, and reports geographic coordinate datasets (CSV / JSON) for AI and machine learning pipelines.

> **Designed for OOP Viva & Code Reviews**: Built strictly using core Java principles (Encapsulation, Inheritance, Polymorphism, Abstraction, Exception Handling, Collections, and File I/O) without web servers, external frontend languages, or unnecessary frameworks.

---

## 🌟 Highlights & Features

- **💻 Pure Java Interactive Console Menu**: Standard terminal menu using `java.util.Scanner` with options to load files, run validations, inspect row results, and export reports.
- **🖥️ Desktop Swing GUI**: Full Java desktop window (`javax.swing`) with KPI metric cards, searchable table, and file dialogs.
- **🏗️ 100% Core Java File I/O**: Reads CSV files using standard `BufferedReader` and `FileReader` without external CSV parser libraries.
- **🛡️ Custom Exception Hierarchy**: Structured exceptions extending `java.lang.Exception`.
- **🧪 15 Comprehensive JUnit 5 Tests**: 100% pass rate covering models, loaders, validators, and report exporters.

---

## 📁 Project Structure

```
AI-Dataset-Coordinate-Validator/
├── pom.xml                                   # Clean Maven configuration (Java 17, JUnit 5, Gson)
├── mvnw / mvnw.cmd                           # Standard Maven wrapper
├── data/
│   ├── sample_coordinates.csv                # Sample CSV dataset for testing
│   └── sample_coordinates.json               # Sample JSON dataset for testing
└── src/
    ├── main/java/com/oopsproject/validator/
    │   ├── Main.java                         # Main entry point (Console / Desktop GUI)
    │   ├── model/
    │   │   ├── Coordinate.java               # Encapsulated model (private fields, getters/setters)
    │   │   ├── ValidationResult.java         # Result per coordinate row
    │   │   └── ValidationReport.java         # Aggregated metrics & collection of results
    │   ├── service/
    │   │   ├── Validator.java                # Interface defining validation contract (Abstraction)
    │   │   ├── AbstractValidator.java        # Base class (Inheritance & Template Method)
    │   │   ├── RangeValidator.java           # Checks lat [-90, 90] & lon [-180, 180]
    │   │   ├── FormatValidator.java          # Checks missing values and format errors
    │   │   ├── DuplicateValidator.java       # Detects duplicate coordinates using Collections
    │   │   ├── OutlierValidator.java         # Detects statistical anomalies (Z-Score)
    │   │   ├── PrecisionValidator.java       # Enforces minimum decimal precision
    │   │   ├── BoundingBoxValidator.java     # Enforces regional bounding box boundaries
    │   │   ├── ValidationEngine.java         # Aggregates validators & executes pipeline (Polymorphism)
    │   │   ├── DatasetLoader.java            # Interface for dataset loaders
    │   │   ├── DatasetLoaderFactory.java     # Factory pattern dispatching loaders
    │   │   ├── CsvDatasetLoader.java         # Pure Java BufferedReader CSV loader
    │   │   └── JsonDatasetLoader.java        # JSON dataset loader
    │   ├── exception/
    │   │   ├── ValidationException.java      # Root custom checked exception
    │   │   ├── InvalidDatasetException.java  # Thrown when file cannot be loaded
    │   │   ├── DatasetReadException.java     # File read / I/O errors
    │   │   └── UnsupportedFormatException.java # Unsupported file extensions
    │   ├── util/
    │   │   ├── ReportExporter.java           # Strategy interface for exporting
    │   │   ├── TxtReportExporter.java        # Pure Java PrintWriter plain text exporter
    │   │   ├── CsvReportExporter.java        # Pure Java CSV report exporter
    │   │   ├── JsonReportExporter.java       # JSON format exporter
    │   │   └── ReportGenerator.java          # Report orchestration helper
    │   └── ui/
    │       ├── ConsoleMenu.java              # Interactive terminal CLI using java.util.Scanner
    │       └── ValidatorGUI.java             # Pure Java Swing desktop interface
    └── test/java/com/oopsproject/validator/  # JUnit 5 test suite (15 unit tests)
```

---

## 🚀 How to Run

### 1. Run Tests (Verify Everything Works)
```bash
./mvnw clean test
```
*Expected: 15 tests run, 0 failures, BUILD SUCCESS.*

### 2. Run Interactive Console Menu (Default)
```bash
./mvnw exec:java
```
Or directly with Java:
```bash
java -cp target/classes:target/dependency/* com.oopsproject.validator.Main
```
Press `1` and hit **Enter** to load the sample CSV dataset, then `2` to run validation, and `3` to view the report!

### 3. Run with Auto-Loaded Dataset File
```bash
./mvnw exec:java -Dexec.args="data/sample_coordinates.csv"
```

### 4. Run Desktop Swing GUI
```bash
./mvnw exec:java -Dexec.args="--gui"
```

---

## 🎓 How to Explain to Sir (OOP Concepts Cheatsheet)

When Sir asks you to explain the code or make live changes during your viva, use these points:

### 1. Encapsulation
- **Where**: `model/Coordinate.java`
- **Explanation**: All coordinate fields (`latitude`, `longitude`, `label`, `rowNumber`) are declared as **`private`**. They cannot be modified directly from outside the class. Public getters and setters (`getLatitude()`, `getLongitude()`, etc.) control how data is accessed and modified.
- **Data Hiding**: Internal flags like `latitudeMissing` and `latitudeInvalidFormat` track corrupt values without throwing unhandled exceptions.

### 2. Abstraction
- **Where**: `service/Validator.java` and `service/DatasetLoader.java`
- **Explanation**: We use Java `interface` to declare what operations must be performed (`isValid()`, `getErrorMessage()`, `load()`) without showing the internal implementation details.

### 3. Inheritance
- **Where**: `service/AbstractValidator.java` and its subclasses (`RangeValidator`, `FormatValidator`, `DuplicateValidator`, `BoundingBoxValidator`, `OutlierValidator`, `PrecisionValidator`).
- **Explanation**: `AbstractValidator` provides shared validation logic (`isNullOrUnparseable()`). Each concrete validator uses `extends AbstractValidator` to inherit this behavior and implement `doValidate()`.

### 4. Polymorphism
- **Where**: `service/ValidationEngine.java`
- **Explanation**: `ValidationEngine` maintains a `List<Validator>`. When validating a row, it loops through each validator using the common interface:
  ```java
  for (Validator validator : validators) {
      if (!validator.isValid(coord, dataset)) {
          result.addError(validator.getErrorMessage(coord, dataset));
      }
  }
  ```
  The JVM dynamically calls the specific `isValid()` method of the actual subclass at runtime (Dynamic Method Dispatch).

### 5. Custom Exception Handling
- **Where**: `exception/` package
- **Hierarchy**:
  - `ValidationException` (extends `Exception`) — Root checked exception
    - `InvalidDatasetException`
      - `DatasetReadException`
      - `UnsupportedFormatException`
- **Explanation**: Custom exceptions provide meaningful, domain-specific error messages instead of generic crashes. Handled using `try-catch` blocks in `ConsoleMenu.java` and `ValidatorGUI.java`.

### 6. Collections Framework
- **Where**: `List<Coordinate>` (`ArrayList`), `Set<Coordinate>` / `HashSet`
- **Explanation**:
  - `ArrayList` is used to store datasets because it offers fast index-based retrieval and dynamic resizing.
  - In `DuplicateValidator.java`, we compare coordinates to detect duplicate entries across rows.

### 7. File I/O (Input / Output)
- **Where**: `service/CsvDatasetLoader.java` and `util/TxtReportExporter.java`
- **Explanation**:
  - **Reading**: Uses `BufferedReader` wrapped around `FileReader` inside a `try-with-resources` block for automatic resource closing. Rows are read line-by-line using `readLine()` and parsed using `line.split(",")`.
  - **Writing**: Uses `PrintWriter` wrapped around `FileWriter` to write report files to disk.

### 8. Design Patterns Used
- **Strategy Pattern**: Each validation check (`RangeValidator`, `FormatValidator`, etc.) is an independent strategy implementing `Validator`.
- **Factory Pattern**: `DatasetLoaderFactory.getLoader(path)` dynamically returns `CsvDatasetLoader` or `JsonDatasetLoader` based on file extension.

---

## 🛠️ How to Make Common Changes (If Sir Asks in Lab)

1. **"Change the Latitude/Longitude valid range"**:
   - Open `src/main/java/com/oopsproject/validator/service/RangeValidator.java`
   - Modify `MIN_LATITUDE`, `MAX_LATITUDE`, `MIN_LONGITUDE`, or `MAX_LONGITUDE`.

2. **"Add a new Validation Rule"**:
   - Create a new class extending `AbstractValidator`:
     ```java
     public class HemisphereValidator extends AbstractValidator {
         @Override
         public String getRuleName() { return "Northern Hemisphere Check"; }
         @Override
         protected boolean doValidate(Coordinate c, List<Coordinate> d) {
             return c.getLatitude() != null && c.getLatitude() >= 0;
         }
         @Override
         public String getErrorMessage(Coordinate c, List<Coordinate> d) {
             return "Coordinate must be in Northern Hemisphere";
         }
     }
     ```
   - Register it in `ValidationEngine.createDefaultEngine()` with:
     ```java
     engine.addValidator(new HemisphereValidator());
     ```

3. **"Change the Bounding Box boundaries"**:
   - Open `src/main/java/com/oopsproject/validator/service/BoundingBoxValidator.java`
   - Modify the default constructor coordinates (e.g. `minLat`, `maxLat`, `minLon`, `maxLon`).
