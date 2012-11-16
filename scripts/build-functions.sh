#!/bin/bash
# this is not really to be executed

# USAGE: fn-git-clone URL [BRANCH [TARGET_DIR] ]
#   URL: file:///gitroot/platform/eclipse.platform.releng.aggregator.git
#   BRANCH: R4_2_maintenance 
#   TARGET_DIR: e.p.releng.aggregator
fn-git-clone () {
	URL="$1"; shift
	if [ $# -gt 0 ]; then
		BRANCH="-b $1"; shift
	fi
	if [ $# -gt 0 ]; then
		TARGET_DIR="$1"; shift
	fi
	echo git clone $BRANCH $URL $TARGET_DIR
	git clone $BRANCH $URL $TARGET_DIR
}

# USAGE: fn-git-checkout BRANCH | TAG
#   BRANCH: R4_2_maintenance 
fn-git-checkout () {
	BRANCH="$1"; shift
	echo git checkout "$BRANCH"
	git checkout "$BRANCH"
}

# USAGE: fn-git-pull
fn-git-pull () {
	echo git pull
	git pull
}

# USAGE: fn-git-submodule-update
fn-git-submodule-update () {
	echo git submodule init
	git submodule init
	echo git submodule update
	git submodule update
}


# USAGE: fn-git-clean 
fn-git-clean () {
	echo git clean -f -d
	git clean -f -d
}

# USAGE: fn-git-reset
fn-git-reset () {
	echo git reset --hard  $@
	git reset --hard  $@
}

# USAGE: fn-git-clean-submodules
fn-git-clean-submodules () {
	echo git submodule foreach git clean -f -d
	git submodule foreach git clean -f -d
}


# USAGE: fn-git-reset-submodules
fn-git-reset-submodules () {
	echo git submodule foreach git reset --hard HEAD
	git submodule foreach git reset --hard HEAD
}

# USAGE: fn-build-id BUILD_TYPE
#   BUILD_TYPE: I, M, N
fn-build-id () {
	BUILD_TYPE="$1"; shift
	echo $BUILD_TYPE$(date +%Y%m%d)-$(date +%H%M)
}

# USAGE: fn-local-repo URL [TO_REPLACE]
#   URL: git://git.eclipse.org/gitroot/platform/eclipse.platform.releng.aggregator.git
#   TO_REPLACE: git://git.eclipse.org
fn-local-repo () {
	TO_REPLACE='git://git.eclipse.org'
	URL="$1"; shift
	if [ $# -gt 0 ]; then
		TO_REPLACE="$1"; shift
	fi
	echo $URL | sed "s!$TO_REPLACE!file://!g"
}

# USAGE: cat repositories.txt | fn-local-repos [TO_REPLACE]
#   TO_REPLACE: git://git.eclipse.org
fn-local-repos () {
	TO_REPLACE='git://git.eclipse.org'
	if [ $# -gt 0 ]; then
		TO_REPLACE="$1"; shift
	fi
	sed "s!$TO_REPLACE!file://!g"
}

# USAGE: fn-git-clone-aggregator GIT_CACHE URL BRANCH 
#   GIT_CACHE: /shared/eclipse/builds/R4_2_maintenance/gitCache
#   URL: file:///gitroot/platform/eclipse.platform.releng.aggregator.git
#   BRANCH: R4_2_maintenance
fn-git-clone-aggregator () {
	GIT_CACHE="$1"; shift
	URL="$1"; shift
	BRANCH="$1"; shift
	if [ ! -e "$GIT_CACHE" ]; then
		mkdir -p "$GIT_CACHE"
	fi
	pushd "$GIT_CACHE"
	fn-git-clone "$URL" "$BRANCH"
	popd
	pushd  $(fn-git-dir "$GIT_CACHE" "$URL" )
	fn-git-submodule-update
	popd
}

# USAGE: fn-git-clean-aggregator AGGREGATOR_DIR BRANCH 
#   AGGREGATOR_DIR: /shared/eclipse/builds/R4_2_maintenance/gitCache/eclipse.platform.releng.aggregator
#   BRANCH: R4_2_maintenance
fn-git-clean-aggregator () {
	AGGREGATOR_DIR="$1"; shift
	BRANCH="$1"; shift
	pushd "$AGGREGATOR_DIR"
	fn-git-clean
	fn-git-clean-submodules
	fn-git-reset-submodules
	fn-git-checkout "$BRANCH"
	fn-git-reset origin/$BRANCH
	popd
}

# USAGE: fn-git-cache ROOT BRANCH
#   ROOT: /shared/eclipse/builds
#   BRANCH: R4_2_maintenance
fn-git-cache () {
	ROOT="$1"; shift
	BRANCH="$1"; shift
	echo $ROOT/$BRANCH/gitCache
}

# USAGE: fn-git-dir GIT_CACHE URL
#   GIT_CACHE: /shared/eclipse/builds/R4_2_maintenance/gitCache
#   URL: file:///gitroot/platform/eclipse.platform.releng.aggregator.git
fn-git-dir () {
	GIT_CACHE="$1"; shift
	URL="$1"; shift
	echo $GIT_CACHE/$( basename "$URL" .git )
}

# USAGE: fn-maven-signer-install REPO_DIR LOCAL_REPO
#   REPO_DIR: /shared/eclipse/builds/R4_2_maintenance/gitCache/org.eclipse.cbi.maven.plugins
#   LOCAL_REPO: /shared/eclipse/builds/R4_2_maintenance/localMavenRepo
fn-maven-signer-install () {
	REPO_DIR="$1"; shift
	LOCAL_REPO="$1"; shift
	pushd "$REPO_DIR"
	mvn -f eclipse-jarsigner-plugin/pom.xml \
    	clean install \
    	-Dmaven.repo.local=$LOCAL_REPO
	popd
}

# USAGE: fn-maven-parent-install REPO_DIR LOCAL_REPO
#   REPO_DIR: /shared/eclipse/builds/R4_2_maintenance/gitCache/eclipse.platform.releng.aggregator
#   LOCAL_REPO: /shared/eclipse/builds/R4_2_maintenance/localMavenRepo
fn-maven-parent-install () {
	REPO_DIR="$1"; shift
	LOCAL_REPO="$1"; shift
	pushd "$REPO_DIR"
	mvn -f eclipse-parent/pom.xml \
    	clean install \
    	-Dmaven.repo.local=$LOCAL_REPO
	popd
}

# USAGE: fn-maven-cbi-install REPO_DIR LOCAL_REPO
#   REPO_DIR: /shared/eclipse/builds/R4_2_maintenance/gitCache/eclipse.platform.releng.aggregator
#   LOCAL_REPO: /shared/eclipse/builds/R4_2_maintenance/localMavenRepo
fn-maven-cbi-install () {
	REPO_DIR="$1"; shift
	LOCAL_REPO="$1"; shift
	pushd "$REPO_DIR"
	mvn -f maven-cbi-plugin/pom.xml \
    	clean install \
    	-Dmaven.repo.local=$LOCAL_REPO
	popd
}

# USAGE: fn-maven-build-aggregator BUILD_ID REPO_DIR LOCAL_REPO VERBOSE SIGNING
#   BUILD_ID: I20121116-0700
#   REPO_DIR: /shared/eclipse/builds/R4_2_maintenance/gitCache/eclipse.platform.releng.aggregator
#   LOCAL_REPO: /shared/eclipse/builds/R4_2_maintenance/localMavenRepo
#   VERBOSE: true
#   SIGNING: true
fn-maven-build-aggregator () {
	BUILD_ID="$1"; shift
	REPO_DIR="$1"; shift
	LOCAL_REPO="$1"; shift
	MARGS="-DbuildId=$BUILD_ID"
	if $VERBOSE; then
		MARGS="$MARGS -X"
	fi
	shift
	if $SIGNING; then
		MARGS="$MARGS -Peclipse-sign"
	fi
	shift
	MARGS="$MARGS -Pbree-libs"
	pushd "$REPO_DIR"
	mvn $MARGS \
	    clean install \
	    -Dmaven.test.skip=true \
	    -Dmaven.repo.local=$LOCAL_REPO
	popd
}

# USAGE: fn-submodule-checkout BUILD_ID REPO_DIR REPOSITORIES_TXT
#   BUILD_ID: M20121116-1100
#   REPO_DIR: /shared/eclipse/builds/R4_2_maintenance/gitCache/eclipse.platform.releng.aggregator
#   SCRIPT: /shared/eclipse/builds/scripts/git-submodule-checkout.sh
#   REPOSITORIES_TXT: /shared/eclipse/builds/scripts/repositories.txt
fn-submodule-checkout () {
	BUILD_ID="$1"; shift
	REPO_DIR="$1"; shift
	SCRIPT="$1"; shift
	REPOSITORIES_TXT="$1"; shift
	pushd "$REPO_DIR"
	git submodule foreach "/bin/bash $SCRIPT $REPOSITORIES_TXT \$name"
	uninit=$( git submodule | grep "^-" | cut -f2 -d" " | sort -u )
	if [ ! -z "$uninit" ]; then
		echo Some modules are not initialized: $uninit
		return
	fi
	conflict=$( git submodule | grep "^U" | cut -f2 -d" " | sort -u )
	if [ ! -z "$conflict" ]; then
		echo Some modules have conflicts: $conflict
		return
	fi
	adds=$( git submodule | grep "^+" | cut -f2 -d" " )
	if [ -z "$adds" ]; then
		echo No updates for the submodules
		return
	fi
	echo git add $adds
	git add $adds
	git commit -m "Update to include new content for $BUILD_ID"
	git push origin HEAD
	popd
}


# USAGE: fn-submodule-checkout BUILD_ID REPO_DIR REPOSITORIES_TXT
#   BUILD_ID: M20121116-1100
#   REPO_DIR: /shared/eclipse/builds/R4_2_maintenance/gitCache/eclipse.platform.releng.aggregator
#   REPOSITORIES_TXT: /shared/eclipse/builds/scripts/repositories.txt
fn-tag-build-inputs () {
	BUILD_ID="$1"; shift
	REPO_DIR="$1"; shift
	REPOSITORIES_TXT="$1"; shift
	pushd "$REPO_DIR"
	git submodule foreach "if grep \"^\${name}:\" $REPOSITORIES_TXT >/dev/null; then git tag $BUILD_ID; git push origin $BUILD_ID; else echo Skipping \$name; fi"
	git tag $BUILD_ID
	git push origin $BUILD_ID
	popd
}
