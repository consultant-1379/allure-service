#!/bin/bash
set -e

USER=root
PASSWORD="${HOST_PASSWORD}"
HOST=atvts3353.athtem.eei.ericsson.se

SCRIPT_DIR=$(dirname $0)
SERVICE_PATH=/var/allure-service
SERVICE_LOG_PATH=/var/log/allure-service

if [ -z "${HOST_PASSWORD}" ]; then
  echo "# HOST_PASSWORD was not provided for accessing ${HOST}"
  exit 1
fi

if ! ssh-keygen -F ${HOST} > /dev/null; then
    echo "adding keys of ${HOST} to .ssh/known_hosts"
    ssh-keyscan -H ${HOST} >> ~/.ssh/known_hosts
fi

echo "# Create scripts directory on target host ${HOST}"
sshpass -p "${PASSWORD}" \
    ssh ${USER}@${HOST} mkdir -p ${SERVICE_PATH}

echo "# Create ${SERVICE_LOG_PATH} directory on target host ${HOST}"
sshpass -p "${PASSWORD}" \
    ssh ${USER}@${HOST} mkdir -p ${SERVICE_LOG_PATH}

echo "# Uploading deployment artifacts"
sshpass -p "${PASSWORD}" \
    scp -p ${SCRIPT_DIR}/run.sh ${SCRIPT_DIR}/docker-compose.yml ${USER}@${HOST}:${SERVICE_PATH}

echo "# Running deployment commands"
sshpass -p "${PASSWORD}" \
    ssh ${USER}@${HOST} /bin/bash << EOF

  export COMPOSE_FILE=${SERVICE_PATH}/docker-compose.yml
  export COMPOSE_PROJECT_NAME=allure-service
  chmod 755 ${SERVICE_PATH}/run.sh
  ${SERVICE_PATH}/run.sh

EOF
