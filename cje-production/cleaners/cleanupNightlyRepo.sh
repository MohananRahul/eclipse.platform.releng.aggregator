#!/bin/bash

# Check if JAVA_HOME is set

JavaCMD=${JAVA_HOME}/bin/java


# Print Java version
echo "Using Java from: ${JavaCMD}"
"${JavaCMD}" -version