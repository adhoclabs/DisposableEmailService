#!/usr/bin/env bash

usage(){
    cat << EOF
usage: "./cicd/scripts/$(basename "$0")" ARGS
Simulate (mostly) the circle container build process
ARGS:
  nodb      don't create a docker database for unit tests
             (you manage a long running shared local db)
  yesdb     create a docker database and run flyway on it
             (you do not manage a long running shared local db)
EXAMPLES:
    "./cicd/scripts/$(basename "$0")" nodb
EOF
    exit 1
}

build(){
    docker stop test_build
    docker rm test_build 
    sbt clean \
    && sbt test \
    && sbt assembly \
    && export ARTIFACT_PATH=`ls target/scala-2.12/*.jar` \
    && docker build --build-arg jar_path=${ARTIFACT_PATH} -t test_build . \
    && echo "try it out with: docker run -ti test_build" \
    && exit 0
}

if [ "$1" == "yesdb" ]; then
    ./cicd/scripts/local_docker_db.sh destroy \
    && ./cicd/scripts/local_docker_db.sh create \
    && build
    exit 1
fi

if [ "$1" == "nodb" ]; then
    build \
    && exit 0
    exit 1
fi

usage

