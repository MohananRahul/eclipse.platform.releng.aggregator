#!/bin/bash

# Check if JAVA_HOME is set

JavaCMD=${JAVA_HOME}/java

echo ${JAVA_HOME}

# Print Java version
echo "Using Java from: ${JavaCMD}"
"${JavaCMD}" -version