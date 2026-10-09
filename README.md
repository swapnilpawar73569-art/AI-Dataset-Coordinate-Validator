# AI Dataset Coordinate Validator & Geographic Anomaly Detector

A high-performance Java 17 Object-Oriented Programming (OOP) project that ingests, validates, and visualizes geographic coordinate datasets (CSV/JSON) for AI and machine learning pipelines.

It features **interactive spatial map visualization**, range checks, format checks, duplicate detection, statistical outlier detection (IQR), regional bounding box enforcement, and multi-format reporting.

---

## 🌟 Highlights & Key Features

- **🌐 Interactive Web Dashboard**: Embedded zero-dependency HTTP server serving a modern dark-mode dashboard with:
  - **Leaflet.js World Map**: Real-time pin mapping with color-coded status (Valid, Out-of-Range, Outliers, Duplicates, BBox breaches).
  - **Bounding Box Overlay**: Visual boundary rectangle rendered on the world map.
  - **Executive KPI Cards**: Real-time stat counters (Total, Valid Rate %, Critical Errors, Outliers, Duplicates).
  - **Interactive Data Explorer**: Search & filter table with click-to-focus map animation.
  - **Multi-Format Export**: One-click download as HTML Report, CSV, JSON, or TXT.
- **🖥️ Desktop Swing GUI**: Full-featured desktop GUI with KPI stat cards, real-time table search, and file picker dialogs.
- **💻 Interactive Terminal CLI**: Console menu for terminal-based workflow and batch scripts.
- **🏗️ Pure OOP Architecture**:
  - **Strategy Pattern**: Pluggable validation rules (`RangeValidator`, `FormatValidator`, `DuplicateValidator`, `PrecisionValidator`, `OutlierValidator`, `BoundingBoxValidator`).
  - **Factory Pattern**: `DatasetLoaderFactory` dynamically dispatching CSV and JSON loaders.
  - **Polymorphism & Inheritance**: `AbstractValidator` base class and custom HTTP handlers extending JDK's `HttpHandler`.
  - **Custom Exception Hierarchy**: `ValidationException` root with `DatasetReadException`, `UnsupportedFormatException`, and `InvalidDatasetException`.

---

## 📁 Project Structure

```
AI-Dataset-Coordinate-Validator/
├── pom.xml
├── mvnw / mvnw.cmd                  # Maven wrapper scripts
├── data/
│   ├── sample_coordinates.csv       # Sample CSV dataset for testing
│   └── sample_coordinates.json      # Sample JSON dataset for testing
├── web/                             # Standalone Web Dashboard assets
│   ├── index.html
│   ├── style.css
│   └── app.js
├── src/
│   ├── main/
│   │   ├── java/com/oopsproject/validator/
│   │   │   ├── Main.java            # Entry point supporting --web, --gui, and --cli
│   │   │   ├── model/               # Coordinate, ValidationResult, ValidationReport
│   │   │   ├── service/             # ValidationEngine, DatasetLoaderFactory, Validators
│   │   │   ├── server/              # WebServer with embedded REST API & static file serving
│   │   │   ├── exception/           # Custom exception hierarchy
│   │   │   ├── util/                # Exporters: Html, Csv, Json, Txt ReportExporters
│   │   │   └── ui/                  # ValidatorGUI (Swing) & ConsoleMenu (CLI)
│   │   └── resources/web/           # Packaged Web Dashboard assets inside JAR
│   └── test/java/com/oopsproject/validator/   # JUnit 5 test suite (21 unit tests)
```

---

## 🚀 Quick Start & How to Run

### 1. Build and Run Tests
```bash
./mvnw clean test
```
All 21 JUnit 5 tests covering models, loaders, validators, and REST API endpoints will execute.

### 2. Launch the Application

#### Option A: Web Dashboard (Recommended / Default)
```bash
./mvnw exec:java
```
Or explicitly specify port:
```bash
./mvnw exec:java -Dexec.args="--web -p 8080"
```
Then open your browser at **`http://localhost:8080/`**.

#### Option B: Desktop Swing GUI
```bash
./mvnw exec:java -Dexec.args="--gui"
```

#### Option C: Interactive Terminal CLI
```bash
./mvnw exec:java -Dexec.args="--cli"
```
Or auto-load a dataset:
```bash
./mvnw exec:java -Dexec.args="data/sample_coordinates.csv"
```

---

## 📋 Course Checklist

### Phase 1 — Mid-Sem (Basic Features)
- [x] `Coordinate` model class (encapsulation: private fields + getters/setters)
- [x] `DatasetLoader` — read CSV and JSON into `List<Coordinate>`
- [x] `Validator` interface + multiple implementations:
  - Range check (latitude -90 to 90, longitude -180 to 180)
  - Format/missing-value check
- [x] Basic console output of valid vs invalid rows
- [x] Interactive `ConsoleMenu` to load a file and trigger validation
- [x] Demonstrate core OOP: encapsulation, inheritance, interfaces, polymorphism

### Phase 2 — End-Sem (Advanced Features)
- [x] Additional validators: duplicate detection, IQR outlier/anomaly detection, decimal precision checks, regional bounding box checks
- [x] Custom exception hierarchy (`ValidationException`, `DatasetReadException`, `UnsupportedFormatException`, `InvalidDatasetException`)
- [x] Multi-format export: HTML, CSV, JSON, and TXT
- [x] Desktop GUI using Java Swing with KPI stat cards, search & filter
- [x] Modern Web Dashboard with Leaflet map visualization and REST API
- [x] 21 Unit tests (JUnit 5) with 100% pass rate
- [x] Design patterns: Strategy Pattern for validators, Factory Pattern for loaders, Observer Pattern for UI events
