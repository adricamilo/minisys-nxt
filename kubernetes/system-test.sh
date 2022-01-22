#!/bin/bash

#REGISTRY_HOST=eu.gcr.io
#REVISION_ID=latest

if [ "$1" = "clean" ] || [ "$1" = "delete" ]; then
    CMD="delete"
else
    CMD="create"
fi

if [ -z "${REGISTRY_HOST}" ]; then
    REGISTRY_HOST="asia.gcr.io"
    echo "Docker Image Registry Host: ${REGISTRY_HOST}"
fi
if [ -z "${REVISION_ID}" ]; then
    REVISION_ID=latest
    echo "Image version id: ${REVISION_ID}"
fi

PROJECT_NAME=$(gcloud config get-value project)
IMAGE_REGISTRY_PATH="${REGISTRY_HOST}\/${PROJECT_NAME}\/ntw"

cd ./test

sed 's/IMAGE_REGISTRY_PATH/'${IMAGE_REGISTRY_PATH}'/g' jmeter.yaml \
    | sed 's/REVISION_ID/'${REVISION_ID}'/g' \
    | kubectl $CMD -f -

if [ "$CMD" != "delete" ]; then
    POD_NAME=$(kubectl get pods --no-headers -o custom-columns=:metadata.name -l job-name=jmeter --namespace=test)
    echo "-- Jmeter test pod launched with name = $POD_NAME --"
    echo "-- Use the following command to check logs--"
    echo "kubectl logs -f" $POD_NAME "--namespace=test"
fi
