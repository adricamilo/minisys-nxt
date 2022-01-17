#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage push-images.sh <REGISTRY_PATH>"
    echo "Example: push-images.sh asia.gcr.io/<project-name>"
    exit -1
fi

REGISTRY_PATH=$1

function tag_and_push {

    echo "Tag image ntw/$1"
    docker tag $1 ${REGISTRY_PATH}/$1:latest
    if [ $? != 0 ]; then
        echo "Tag image failed - $1"
        exit -1;
    fi

    echo "Push image $1"
    docker push ${REGISTRY_PATH}/$1:latest
    if [ $? != 0 ]; then
        echo "Push image failed - $1"
        exit -1;
    fi

}

for image in $(docker images --format={{.Repository}} | grep "ntw")
do
    tag_and_push $image
done

echo "-- Done --"
