#!/usr/local/bin/bash

bash --version

export OMS_ROOT=`pwd`

COMPONENT=$1
CONTAINER=$2

if [ "$COMPONENT" == "rest" ]; then

    cd $OMS_ROOT/services/$SVC
    mvn clean package
    cp ./target/${SVC}.war ../target

elif [ "$COMPONENT" == "web" ]; then

    cd $OMS_ROOT/web
    ./create-tgz.sh
    CONTAINER=web

fi


cd $OMS_ROOT/staging/bin
./pull-artifacts.sh

cd $OMS_ROOT/docker/bin
./pull-artifacts.sh

cd $OMS_ROOT/docker
docker-compose build ${COMPONENT}
docker-compose stop ${CONTAINER}
docker-compose rm -f ${CONTAINER}
docker-compose up -d ${CONTAINER}

