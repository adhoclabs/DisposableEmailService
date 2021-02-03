#!/usr/bin/env bash

if [ -z "${NEW_RELIC_PATH}" ]
then
    echo "NEW_RELIC_PATH not set"
    exit 1
fi

if [ -z "${NEW_RELIC_CHECKSUM}" ]
then
    echo "NEW_RELIC_CHECKSUM not set"
    exit 1
fi

aws s3 cp ${NEW_RELIC_PATH} ./cicd/newrelic/newrelic.jar
GOT_NR_SUM=$(sha256sum ./cicd/newrelic/newrelic.jar | awk '{print $1}')
if [ "${GOT_NR_SUM}" != "${NEW_RELIC_CHECKSUM}" ]
then 
    echo "Checksum doesn't match ${GOT_NR_SUM} != ${NEW_RELIC_CHECKSUM}"
    exit 1 
fi

exit 0