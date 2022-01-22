#!/bin/bash

#REGISTRY_HOST=eu.gcr.io
#REVISION_ID=latest

############### Arg Validations ###############

if [ "$1" != "apply" ] && [ "$1" != "delete" ]; then
    echo "First parameter should be apply or delete"; exit 1;
fi

if [ -n "$2" ] && [ ! -d "$2" ] && [ ! -f "$2" ]; then
    echo "Second arg should be a relative dir or file path"; exit 1;
fi

if [ -z "${REVISION_ID}" ]; then
    REVISION_ID=latest
    echo "Image revision id set to: ${REVISION_ID}"
fi

if [ -z "${REGISTRY_HOST}" ]; then
    REGISTRY_HOST="asia.gcr.io"
    echo "Image Registry Host set to: ${REGISTRY_HOST}"
fi

###############################################

CMD=$1

if [ -z "$2" ]; then
    CONFIG_FILES_PATH="./config"
else
    CONFIG_FILES_PATH=$2
fi

PROJECT_NAME=$(gcloud config get-value project)
IMAGE_REGISTRY_PATH="${REGISTRY_HOST}\/${PROJECT_NAME}\/ntw"

###############################################

if [ "$CMD" = "delete" ]; then ORDER="-r"; fi

if [ -d $CONFIG_FILES_PATH ]; then
    CONFIG_FILES=$(find ${CONFIG_FILES_PATH%/} -name "*.yaml" | sort $ORDER | xargs)
elif [ -f $CONFIG_FILES_PATH ]; then
    CONFIG_FILES=$CONFIG_FILES_PATH
fi

function kube_execute {
    CONFIG_FILE=$1
    echo "-- kubectl $CMD -f $CONFIG_FILE --"
    sed 's/IMAGE_REGISTRY_PATH/'${IMAGE_REGISTRY_PATH}'/g' ${CONFIG_FILE} \
	| sed 's/REVISION_ID/'${REVISION_ID}'/g' \
	| kubectl ${CMD} -f -
}

for CONFIG_FILE in ${CONFIG_FILES}
do
    kube_execute $CONFIG_FILE
done
