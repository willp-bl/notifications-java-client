#!/bin/bash
DOCKER_IMAGE_NAME=notifications-java-client

docker run \
  --rm \
  -v "`pwd`:/var/project" \
  -v `pwd`/.m2:/root/.m2 \
  --env-file docker.env \
  -it \
  ${DOCKER_IMAGE_NAME} \
  ${@}
