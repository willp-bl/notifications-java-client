#!/usr/bin/env bash

set -eo pipefail

source environment.sh

if [[ -z "${MAVEN_CENTRAL_TOKEN_USERNAME}" ]]; then
  # notify-pass is set up by our bash_profile/bashrc/etc files, so isnt available within this shell. have to do the
  # hard work manually
  echo 'Maven token username not set. Please run the following and then retry'
  echo 'export MAVEN_CENTRAL_TOKEN_USERNAME=$(notify-pass show credentials/maven-central/token-username)'
  exit 1
fi

if [[ -z "${MAVEN_CENTRAL_TOKEN_PASSWORD}" ]]; then
  # notify-pass is set up by our bash_profile/bashrc/etc files, so isnt available within this shell. have to do the
  # hard work manually
  echo 'Maven token password not set. Please run the following and then retry'
  echo 'export MAVEN_CENTRAL_TOKEN_PASSWORD=$(notify-pass show credentials/maven-central/token-password)'
  exit 1
fi

mvn --settings=maven-settings.xml clean javadoc:jar source:jar deploy
