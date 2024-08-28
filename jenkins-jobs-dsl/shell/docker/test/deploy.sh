#!/bin/bash
set -e

echo "# Find latest Allure version"
ALLURE_VERSION=`$M2_HOME/bin/mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec`

echo "allure version = ${ALLURE_VERSION} "
echo "# Running deployment commands"

export ALLURE_VERSION=${ALLURE_VERSION}
cd jenkins-jobs-dsl/shell/docker/test
./run.sh
