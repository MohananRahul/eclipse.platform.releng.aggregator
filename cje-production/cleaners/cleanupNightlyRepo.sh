#!/bin/bash

# Check if JAVA_HOME is set
if [[ -z "${JAVA_HOME}" ]]; then
  echo "JAVA_HOME is not set. Checking system default Java..."
  javaCmd=$(which java)
else
  javaCmd="${JAVA_HOME}/bin/java"
fi

# Verify if Java exists
if [[ ! -x "${javaCmd}" ]]; then
  echo "ERROR: Java executable not found!"
  exit 1
fi

# Print Java version
echo "Using Java from: ${javaCmd}"
"${javaCmd}" -version