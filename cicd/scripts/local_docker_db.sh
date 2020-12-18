#!/usr/bin/env bash
#
# Creates a postgres database container with the name and version specific in the globals at the top
# This script should be executed from the root of the repo
#
# per repo globals
export POSTGRES_VERSION=11.5
export DB=music

# do not modify below
export CONTAINER=local-${DB}-db
export PSQL="docker exec -ti ${CONTAINER}\
    psql postgresql://postgres:password@localhost/postgres"

usage(){
    cat << EOF
usage: "./cicd/scripts/$(basename "$0")" ARGS
Creates or destroys a postgres docker container
ARGS:
  create     Create the database container and run flyway
  destroy    Destroy previously created database container
EXAMPLES:
  To create:
      "./cicd/scripts/$(basename "$0")" create
  To destroy:
      "./cicd/scripts/$(basename "$0")" destroy
EOF
    exit 1
}

# create a new container and run flyway. if any step fails we'll exit and notify the user
create(){
    docker run --name ${CONTAINER} -e POSTGRES_PASSWORD=password -d -p 5432:5432 postgres:${POSTGRES_VERSION} \
    && sleep 3 \
    && $PSQL -c "CREATE ROLE ${DB} WITH SUPERUSER CREATEDB CREATEROLE LOGIN ENCRYPTED PASSWORD '${DB}';" \
    && $PSQL -c "CREATE DATABASE ${DB};" \
    && flyway migrate \
    && exit 0
    echo "can't create container ${CONTAINER}, perhaps it's already running..."
    exit 1
}

# be naive and destroy container even if it doesn't exist
destroy() {
    docker stop ${CONTAINER}
    docker rm ${CONTAINER}
    exit 0
}

if [ "$1" == "create" ]; then
    create
fi

if [ "$1" == "destroy" ]; then
    destroy
fi

usage
