#!/bin/bash

# Check if Java is installed
if command -v java &> /dev/null
then
     $JAVA_HOME/bin/java -version"
else
    echo "Java is not installed."
fi
