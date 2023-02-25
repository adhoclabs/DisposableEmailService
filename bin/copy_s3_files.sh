#!/usr/bin/env bash

if [ -z "${DATADOG_PATH}" ]
then
  echo "DATADOG_PATH not set"
  exit 1
fi

if [ -z "${DATADOG_CHECKSUM}" ]
then
  echo "DATADOG_CHECKSUM not set"
  exit 1
fi

aws s3 cp ${DATADOG_PATH} ./cicd/datadog/dd-agent.jar
GOT_DD_SUM=$(sha256sum ./cicd/datadog/dd-agent.jar | awk '{print $1}')
if [ "${GOT_DD_SUM}" != "${DATADOG_CHECKSUM}" ]
then
  echo "Checksum doesn't match ${GOT_DD_SUM} != ${NEW_DD_CHECKSUM}"
  exit 1
fi
exit 0