#!/bin/bash

# Check if JAVA_HOME is set
JAVA_HOME = installTemurinJDK('21', 'linux', 'x86_64')
PATH = "${JAVA_HOME}/bin

JavaCMD=${JAVA_HOME}/bin/java


# Print Java version
echo "Using Java from: ${JavaCMD}"
"${JavaCMD}" -version