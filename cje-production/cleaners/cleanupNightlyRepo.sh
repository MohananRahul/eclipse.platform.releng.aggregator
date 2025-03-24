#!/bin/bash

#*******************************************************************************
# Copyright (c) 2022 IBM Corporation and others.
#
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     IBM Corporation - initial API and implementation
#*******************************************************************************

# Ensure critical variables are defined
remoteBase="/home/data/httpd/download.eclipse.org"  # Fixed: Define remoteBase explicitly
workspace="$1"  # Passed as the first argument to the script

# Validate workspace directory
if [[ -z "$workspace" || ! -d "$workspace" ]]; then
  echo -e "\n\tERROR: Workspace directory not provided or invalid."
  echo -e "\tUsage: $0 /path/to/workspace"
  exit 1
fi

function writeHeader() {
  compositeRepoDir="$1"
  antBuildFile="$2"
  if [[ -z "${compositeRepoDir}" ]]; then
    echo -e "\n\tERROR: compositeRepoDir not passed to writeHeader function."
    exit 1
  fi
  # Write XML header for Ant script
  echo -e "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > "$antBuildFile"
  echo -e "<project" >> "$antBuildFile"
  echo -e "  basedir=\".\"" >> "$antBuildFile"
  echo -e "  default=\"cleanup\">" >> "$antBuildFile"
  echo -e "  <target name=\"cleanup\">" >> "$antBuildFile"
  echo -e "    <p2.composite.repository>" >> "$antBuildFile"
  echo -e "      <repository location=\"file://${compositeRepoDir}\" />" >> "$antBuildFile"
  echo -e "      <remove>" >> "$antBuildFile"
}

function writeReposToRemove() {
  antBuildFile="$1"
  for repo in "${reposToRemove[@]}"; do
    echo "        <repository location=\"$repo\" />" >> "$antBuildFile"
  done
}

function writeClosing() {
  antBuildFile="$1"
  echo -e "      </remove>" >> "$antBuildFile"
  echo -e "    </p2.composite.repository>" >> "$antBuildFile"
  echo -e "  </target>" >> "$antBuildFile"
  echo -e "</project>" >> "$antBuildFile"
}

function generateCleanupXML() {
  mainRepoDir="$1"
  antBuildFile="$2"
  if [[ -z "${mainRepoDir}" || ! -e "${mainRepoDir}" ]]; then
    echo -e "\n\tERROR: Directory does not exist: ${mainRepoDir}"
    exit 1
  fi
  writeHeader "$mainRepoDir" "$antBuildFile"
  writeReposToRemove "$antBuildFile"
  writeClosing "$antBuildFile"
}

function getReposToRemove() {
  cDir="$1"
  buildType="$2"
  nRetain="$3"
  buildDir="${remoteBase}/eclipse/downloads/drops4"  # Fixed: Use full path

  # Validate directory
  if [[ ! -e "${cDir}" ]]; then
    echo -e "\n\tERROR: Directory not found: ${cDir}" >&2
    exit 1
  fi

  echo -e "\n\tDEBUG: Processing directory: ${cDir}"
  # Find and sort repositories
  sortedallOldRepos=($(find "${cDir}" -maxdepth 1 -type d -name "${buildType}*" -printf "%C@ %f\n" | sort | cut -d' ' -f2))
  nbuilds=${#sortedallOldRepos[@]}
  echo -e "\tNumber of repos found: $nbuilds"

  # Handle unstable I-builds
  if [[ "$buildType" == "I" ]]; then
    stableBuildRepos=()
    for repo in "${sortedallOldRepos[@]}"; do
      if [[ ! -f "${buildDir}/${repo}/buildUnstable" ]]; then
        stableBuildRepos+=("$repo")
      fi
    done
    sortedallOldRepos=("${stableBuildRepos[@]}")
    nbuilds=${#sortedallOldRepos[@]}
    echo -e "\tStable builds remaining: $nbuilds"
  fi

  # Calculate repos to remove
  if [[ $nbuilds -gt $nRetain ]]; then
    nToRemove=$((nbuilds - nRetain))
    reposToRemove=("${sortedallOldRepos[@]:0:$nToRemove}")
    echo -e "\tRepositories to remove: ${#reposToRemove[@]}"
  else
    reposToRemove=()
    echo -e "\tNo repositories to remove"
  fi
}

function cleanRepo() {
  eclipseRepo="$1"
  buildType="$2"
  nRetain="$3"
  dryRun="$4"

  # Validate repository directory
  if [[ -z "$eclipseRepo" || ! -d "$eclipseRepo" ]]; then
    echo -e "\n\tERROR: Invalid repository directory: '$eclipseRepo'"
    exit 1
  fi

  # Define executables
  eclipseexe="${workspace}/eclipse/eclipse"
  javaexe="${JAVA_HOME}/bin/java"
  if [[ ! -x "$eclipseexe" ]]; then
    echo -e "\n\tERROR: Eclipse executable not found: $eclipseexe"
    exit 1
  fi
  if [[ ! -x "$javaexe" ]]; then
    echo -e "\n\tERROR: Java executable not found: $javaexe"
    exit 1
  fi

  antBuildFile="${workspace}/cleanupRepoScript${buildType}.xml"
  antRunner="org.eclipse.ant.core.antRunner"
  devWorkspace="${workspace}/workspace-cleanup"

  echo -e "\n\tStarting cleanup for ${eclipseRepo} (retain ${nRetain} builds)"
  getReposToRemove "$eclipseRepo" "$buildType" "$nRetain"

  if [[ ${#reposToRemove[@]} -gt 0 ]]; then
    generateCleanupXML "$eclipseRepo" "$antBuildFile"
    if [[ -z "$dryRun" ]]; then
      # Execute the Ant script
      "$eclipseexe" -nosplash --launcher.suppressErrors -data "$devWorkspace" -application "$antRunner" -f "$antBuildFile" -vm "$javaexe"
      RC=$?
      if [[ $RC -ne 0 ]]; then
        echo -e "\n\tERROR: Ant script execution failed"
        exit $RC
      fi
      # Remove old directories for N-builds
      if [[ "$buildType" == "N" ]]; then
        for repo in "${reposToRemove[@]}"; do
          echo -e "\tRemoving directory: ${eclipseRepo}/${repo}"
          rm -rf "${eclipseRepo}/${repo}"
        done
      fi
    else
      echo -e "\n\tDry run - generated Ant script:"
      cat "$antBuildFile"
    fi
  fi
}

# Define repository paths with validation
# eclipseIRepo="${remoteBase}/eclipse/updates/4.36-I-builds"
eclipseYRepo="${remoteBase}/eclipse/updates/4.34-Y-builds"
# eclipsePRepo="${remoteBase}/eclipse/updates/4.36-P-builds"
# eclipseBuildTools="${remoteBase}/eclipse/updates/buildtools"

# Verify repository paths before cleaning
for repo in "$eclipseIRepo" "$eclipseYRepo" "$eclipsePRepo" "$eclipseBuildTools"; do
  if [[ ! -d "$repo" ]]; then
    echo -e "\n\tERROR: Repository directory not found: $repo"
    exit 1
  fi
done

# Perform cleanups
doDryrun=""  # Set to non-empty for dry run
cleanRepo "$eclipseIRepo" "I" 2 "$doDryrun"
cleanRepo "$eclipseYRepo" "Y" 2 "$doDryrun"
cleanRepo "$eclipsePRepo" "P" 2 "$doDryrun"
cleanRepo "$eclipseBuildTools" "I" 2 "$doDryrun"

echo -e "\n\tCleanup completed successfully"