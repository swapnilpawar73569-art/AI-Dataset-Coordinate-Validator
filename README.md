# AI Dataset Coordinate Validator

A Java OOP course project that validates geographic coordinate datasets (CSV/JSON) for
correctness — range checks, format checks, duplicates, and anomalies — and generates
a validation report.

## Team

| Name | GitHub Username | Module |
|------|------------------|--------|
| _fill in_ | | Model + Data Loading |
| _fill in_ | | Validation Rules |
| _fill in_ | | Exception Handling + Reporting |
| _fill in_ | | UI (Console/GUI) + Integration |

## Project Structure

```
AI-Dataset-Coordinate-Validator/
├── pom.xml
├── data/
│   └── sample_coordinates.csv       # sample dataset for testing
├── docs/                            # project plan, diagrams, phase notes
├── src/
│   ├── main/java/com/oopsproject/validator/
│   │   ├── Main.java                # entry point
│   │   ├── model/                   # Coordinate and other data classes
│   │   ├── service/                 # DatasetLoader, Validator interface + implementations
│   │   ├── exception/                # custom exceptions
│   │   ├── util/                    # ReportGenerator, helpers
│   │   └── ui/                      # ConsoleMenu (Phase 1) / GUI (Phase 2)
│   └── test/java/com/oopsproject/validator/   # JUnit tests
```

## Getting Started (for teammates)

1. **Clone the repo:**
   ```bash
   git clone <PASTE_YOUR_REPO_URL_HERE>
   cd AI-Dataset-Coordinate-Validator
   ```
2. **Open in your IDE** (IntelliJ IDEA recommended) as a Maven project — it will
   auto-detect `pom.xml` and download dependencies.
3. **Build from command line** (optional, needs Maven + JDK 17):
   ```bash
   mvn clean install
   ```
4. **Run:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.oopsproject.validator.Main"
   ```
5. **Create your own branch before making changes:**
   ```bash
   git checkout -b feature/<your-module-name>
   ```
6. Make your changes, then:
   ```bash
   git add .
   git commit -m "Describe your change"
   git push -u origin feature/<your-module-name>
   ```
7. Open a Pull Request on GitHub into `main` and get a teammate to review before merging.

## Phase 1 — Mid-Sem (Basic Features)

- [ ] `Coordinate` model class (encapsulation: private fields + getters/setters)
- [ ] `DatasetLoader` — read CSV (and/or JSON) into a `List<Coordinate>`
- [ ] `Validator` interface + at least 2 implementations:
  - Range check (latitude -90 to 90, longitude -180 to 180)
  - Format/missing-value check
- [ ] Basic console output of valid vs invalid rows
- [ ] Simple `ConsoleMenu` to load a file and trigger validation
- [ ] Demonstrate core OOP: encapsulation, inheritance or interfaces, polymorphism

## Phase 2 — End-Sem (Advanced Features)

- [ ] Additional validators: duplicate detection, outlier/anomaly detection, precision checks
- [ ] Custom exception hierarchy (`InvalidDatasetException`, etc.) with proper try/catch flow
- [ ] Export validation report to a file (CSV/JSON/TXT)
- [ ] (Optional) Simple GUI using JavaFX or Swing instead of console menu
- [ ] Unit tests (JUnit 5) for each validator
- [ ] Design pattern usage (e.g. Strategy pattern for validators, Factory for loaders)

## Dependencies

- Java 17
- Maven
- Gson (JSON parsing)
- Apache Commons CSV (CSV parsing)
- JUnit 5 (testing)
