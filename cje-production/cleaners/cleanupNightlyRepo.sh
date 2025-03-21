#!/bin/bash

# Check if Java is installed
if command -v java &> /dev/null
then
    echo "Java Version:"
    java -version
    echo "JAVA_HOME: $JAVA_HOME"
else
    echo "Java is not installed."
fi