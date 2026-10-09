#!/bin/bash
# Compile and run the AI Dataset Coordinate Validator
mkdir -p bin
javac -d bin $(find src/main/java -name "*.java")
if [ $? -eq 0 ]; then
    java -cp bin com.oopsproject.validator.Main "$@"
fi
