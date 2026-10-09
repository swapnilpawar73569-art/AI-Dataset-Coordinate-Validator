# 📍 AI Dataset Coordinate Validator & Geographic Anomaly Detector
### Pure Java Object-Oriented Programming (OOP) Project

A 100% normal, pure Java project built using core Object-Oriented Programming principles.
**Zero external dependencies, zero Maven (`pom.xml`/`mvnw`), zero non-Java languages.**

Just standard Java files (`.java`), compiled with **`javac`** and executed with **`java`**!

---

## 🌟 Features

- **💻 Interactive Terminal Menu (CLI)**: Console-based workflow using standard `java.util.Scanner` to load datasets, run validations, inspect results row-by-row, and export reports.
- **🖥️ Desktop GUI Window (Swing)**: Clean Java desktop GUI (`javax.swing`) with KPI cards, searchable results table, and file pickers.
- **📁 100% Core Java File Handling**:
  - Reads CSV using standard `BufferedReader` and `line.split(",")`.
  - Reads JSON using standard Java string parsing.
  - Exports reports to `.txt`, `.csv`, and `.json` using standard `PrintWriter` and `FileWriter`.
- **🛡️ Custom Exception Hierarchy**: Domain-specific checked exceptions (`ValidationException`, `InvalidDatasetException`, `DatasetReadException`, `UnsupportedFormatException`).
- **🧪 Built-in Test Suite**: 21 built-in test assertions running in standard Java without external testing frameworks.

---

## 📁 Project Structure

```
AI-Dataset-Coordinate-Validator/
├── run.sh / run.bat                          # One-click compile & run script
├── test.sh                                   # One-click test runner script
├── data/
│   ├── sample_coordinates.csv                # Sample CSV dataset for testing
│   └── sample_coordinates.json               # Sample JSON dataset for testing
└── src/main/java/com/oopsproject/validator/
    ├── Main.java                             # Application entry point
    ├── TestRunner.java                       # Self-contained pure Java test suite
    ├── model/
    │   ├── Coordinate.java                   # Encapsulated model (private fields, getters/setters)
    │   ├── ValidationResult.java             # Holds validation outcome per row
    │   └── ValidationReport.java             # Summary metrics & list of results
    ├── service/
    │   ├── Validator.java                    # Interface defining validation contract (Abstraction)
    │   ├── AbstractValidator.java            # Base class (Inheritance & Template Method)
    │   ├── RangeValidator.java               # Checks lat [-90, 90] & lon [-180, 180]
    │   ├── FormatValidator.java              # Checks missing values and format errors
    │   ├── DuplicateValidator.java           # Detects duplicate coordinates using Collections
    │   ├── OutlierValidator.java             # Detects statistical anomalies (Z-Score)
    │   ├── PrecisionValidator.java           # Enforces minimum decimal precision
    │   ├── BoundingBoxValidator.java         # Enforces regional bounding box boundaries
    │   ├── ValidationEngine.java             # Aggregates validators & executes pipeline (Polymorphism)
    │   ├── DatasetLoader.java                # Interface for dataset loaders
    │   ├── DatasetLoaderFactory.java         # Factory pattern dispatching loaders
    │   ├── CsvDatasetLoader.java             # Pure Java BufferedReader CSV loader
    │   └── JsonDatasetLoader.java            # Pure Java JSON loader
    ├── exception/
    │   ├── ValidationException.java          # Root checked exception
    │   ├── InvalidDatasetException.java      # Thrown when dataset fails to load
    │   ├── DatasetReadException.java         # File read / I/O errors
    │   └── UnsupportedFormatException.java   # Unsupported file format errors
    ├── util/
    │   ├── ReportExporter.java               # Interface for exporting reports
    │   ├── TxtReportExporter.java            # Pure Java plain text report exporter
    │   ├── CsvReportExporter.java            # Pure Java CSV report exporter
    │   ├── JsonReportExporter.java           # Pure Java JSON report exporter
    │   └── ReportGenerator.java              # Report generator dispatcher
    └── ui/
        ├── ConsoleMenu.java                  # Interactive CLI menu (java.util.Scanner)
        └── ValidatorGUI.java                 # Pure Java Swing desktop window
```

---

## 🚀 How to Compile & Run

### Quick Run (Mac / Linux):
```bash
./run.sh
```

### Quick Run (Windows):
Double click `run.bat` or run in Command Prompt:
```cmd
run.bat
```

### Or Manually using Standard `javac` and `java`:
1. **Compile**:
   ```bash
   javac -d bin $(find src/main/java -name "*.java")
   ```
   *(On Windows Command Prompt)*:
   ```cmd
   javac -d bin src/main/java/com/oopsproject/validator/*.java src/main/java/com/oopsproject/validator/*/*.java
   ```

2. **Run Interactive Console Menu**:
   ```bash
   java -cp bin com.oopsproject.validator.Main
   ```

3. **Run with Dataset Auto-Loaded**:
   ```bash
   java -cp bin com.oopsproject.validator.Main data/sample_coordinates.csv
   ```

4. **Run Desktop Swing GUI**:
   ```bash
   java -cp bin com.oopsproject.validator.Main --gui
   ```

5. **Run Test Suite**:
   ```bash
   ./test.sh
   # or: java -cp bin com.oopsproject.validator.TestRunner
   ```

---

## 🎓 How to Explain to Sir (OOP Viva Questions)

### Q1: "What is Encapsulation in your project?"
- **Answer:** In `Coordinate.java`, all data members (`latitude`, `longitude`, `label`, `rowNumber`) are `private`. They cannot be accessed directly from outside the class. Public getters and setters (`getLatitude()`, `setLatitude()`, etc.) provide controlled access. Internal validation flags like `latitudeMissing` prevent illegal states.

### Q2: "Where did you use Inheritance?"
- **Answer:** In `AbstractValidator.java` and its subclasses. `AbstractValidator` implements `Validator` and provides common validation methods (`isNullOrUnparseable()`). Specific validator classes (`RangeValidator`, `FormatValidator`, `DuplicateValidator`, `BoundingBoxValidator`, `OutlierValidator`) extend `AbstractValidator` using the `extends` keyword to inherit that functionality.

### Q3: "What is Abstraction and where is it used?"
- **Answer:** In `Validator.java` and `DatasetLoader.java`. We use Java `interface` to declare abstract methods (`isValid()`, `getErrorMessage()`, `load()`) without exposing implementation details. The caller interacts only through the interface.

### Q4: "Where did you implement Polymorphism?"
- **Answer:** In `ValidationEngine.java`. The engine maintains a list of `Validator` references:
  ```java
  for (Validator validator : validators) {
      if (!validator.isValid(coord, dataset)) {
          result.addError(validator.getErrorMessage(coord, dataset));
      }
  }
  ```
  At runtime, the JVM uses Dynamic Method Dispatch to call the specific subclass method (`RangeValidator.isValid()`, `DuplicateValidator.isValid()`, etc.).

### Q5: "How does File Handling work in your project?"
- **Answer:** In `CsvDatasetLoader.java`, we use `BufferedReader` and `FileReader` inside a `try-with-resources` block. We read line by line with `readLine()`, split by comma with `line.split(",")`, and parse coordinates using `Double.parseDouble()`. For report export, we use `PrintWriter` and `FileWriter`.

### Q6: "Which Design Patterns did you use?"
- **Answer:**
  1. **Strategy Pattern:** Each validation rule (`RangeValidator`, `FormatValidator`, etc.) is an interchangeable strategy implementing the `Validator` interface.
  2. **Factory Pattern:** `DatasetLoaderFactory.getLoader(filePath)` inspects the file extension and dynamically returns either `CsvDatasetLoader` or `JsonDatasetLoader`.

### Q7: "How did you handle Exceptions?"
- **Answer:** We created a custom exception hierarchy extending `java.lang.Exception`: `ValidationException`, `InvalidDatasetException`, and `DatasetReadException`. We catch them with `try-catch` blocks in `ConsoleMenu` and display clear error messages.
