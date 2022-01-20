#!/bin/bash

if [ "$1" == "apply" ] || [ "$1" == "create" ] || [ "$1" == "delete" ]; then
    CMD=$1
    if [ -n "$2" ] && [ -d "$2" ]; then
	CONFIG_DIRS=$2
	CONFIG_FILES="all"
    elif [ -n "$2" ] && [ -f "$2" ]; then
	CONFIG_DIRS=$(dirname "$2")
	CONFIG_FILES=$(basename "$2")
    else
	CONFIG_DIRS="all"
	CONFIG_FILES="all"
    fi
else
    echo "First parameter should be apply/create/delete"
    exit 1
fi

if [ -z "${REVISION_ID}" ]; then
    REVISION_ID=latest
    echo "Defaulting revision id to: ${REVISION_ID}"
fi

if [ -z "${IMAGE_REGISTRY_PATH}" ]; then
    IMAGE_REGISTRY_PATH="asia.gcr.io\/$(gcloud config get-value project)\/ntw"
    echo "Defaulting Image Registry Path to: ${IMAGE_REGISTRY_PATH}"
fi

function kube_execute {
    CONFIG_FILE=$1
    echo "-- kubectl $CMD -f $CONFIG_FILE --"
    sed 's/IMAGE_REGISTRY_PATH/'${IMAGE_REGISTRY_PATH}'/g' ${CONFIG_FILE} \
	| sed 's/REVISION_ID/'${REVISION_ID}'/g' \
	| kubectl ${CMD} -f -
}

if [ "${CONFIG_DIRS}" == "all" ]; then
    CONFIG_DIRS=$(ls | xargs)
fi

for CONFIG_DIR in ${CONFIG_DIRS}
do
    pushd $CONFIG_DIR > /dev/null
    echo "------- $CMD $CONFIG_DIR -------"

    if [ "${CONFIG_FILES}" == "all" ]; then
	CONFIG_FILES=$(ls *.yaml | xargs)
    fi
    
    for CONFIG_FILE in ${CONFIG_FILES}
    do
	kube_execute $CONFIG_FILE
    done
    popd > /dev/null
done
