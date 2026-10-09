@echo off
REM Compile and run the AI Dataset Coordinate Validator (Windows)
if not exist bin mkdir bin
javac -d bin src/main/java/com/oopsproject/validator/*.java src/main/java/com/oopsproject/validator/*/*.java
if %ERRORLEVEL% EQU 0 (
    java -cp bin com.oopsproject.validator.Main %*
)
