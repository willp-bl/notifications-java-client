#!/usr/bin/env bash

set -eo pipefail

source environment.sh

if [[ -z "${MAVEN_CENTRAL_PASSWORD}" ]]; then
  # notify-pass is set up by our bash_profile/bashrc/etc files, so isnt available within this shell. have to do the
  # hard work manually
  echo 'Maven password not set. Please run the following and then retry'
  echo 'export MAVEN_CENTRAL_PASSWORD=$(notify-pass show credentials/maven-central/password)'
  exit 1
fi

mvn --settings=maven-settings.xml clean javadoc:jar source:jar deploy
