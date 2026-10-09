#!/bin/bash
# Compile and run all tests
mkdir -p bin
javac -d bin $(find src/main/java -name "*.java")
if [ $? -eq 0 ]; then
    java -cp bin com.oopsproject.validator.TestRunner
fi
