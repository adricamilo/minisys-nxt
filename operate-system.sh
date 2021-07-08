#!/bin/bash

OMS_ROOT=`pwd`
cd $OMS_ROOT

TARGET=$1

declare -a app_containers=("web" "lb-web" "eureka" "gateway-svc" "lb-services" "cassandra" "postgres" "admin-svc" "auth-svc" "product-svc" "order-svc" "inventory-svc")
declare -a infra_containers=("elasticsearch" "kibana" "jaeger-collector" "jaeger-query" "jaeger-agent" "fluentd" "es-exporter" "pg-exporter" "prometheus")

echo "-- Start --"


############################## Start ####################################

function do_start {
    echo "-- Run Containers --"
    cd $OMS_ROOT/docker
    if [ "$1" == "infra" ]; then
	do_start_infra
    elif [ "$1" == "app" ]; then
	do_start_app
    else
	do_start_infra
	do_start_app
    fi
    echo "-- Done --"
}

function do_start_infra {
    echo "-- Run Infra Containers --"
    for container in "${infra_containers[@]}"
    do
	do_start_one $container
	sleep 5s
    done    
    echo "-- Done --"
}

function do_start_app {
    echo "-- Run App Containers --"
    for container in "${app_containers[@]}"
    do
	do_start_one $container
	sleep 10s
    done    
    echo "-- Done --"
}

function do_start_one {
    docker-compose up -d $1
    if [ $? != 0 ]; then
	echo "Build failed - Run $1 container failed"
	exit -1;
    fi
}


############################## Test ####################################

function do_test {
    echo "-- Run tests --"
    cd $OMS_ROOT/docker/jmeter
    docker-compose build
    docker-compose up
    if [ $? != 0 ]; then
	echo "Tests failed - Unable to start Jmeter container"
	exit -1;
    fi
    echo "-- Done --"
}


############################## Stop ####################################

function do_stop {
    echo "-- Stop containers --"
    cd $OMS_ROOT/docker
    if [ "$1" == "infra" ]; then
	do_stop_infra
    elif [ "$1" == "app" ]; then
	do_stop_app
    else
	do_stop_infra
	do_stop_app
    fi
    docker-compose rm -f
    echo "-- Done --"
}

function do_stop_infra {
    echo "-- Stop Infra Containers --"
    for container in "${infra_containers[@]}"
    do
	do_stop_one $container
    done    
    echo "-- Done --"
}

function do_stop_app {
    echo "-- Stop App Containers --"
    for container in "${app_containers[@]}"
    do
	do_stop_one $container
    done    
    echo "-- Done --"
}

function do_stop_one {
    docker-compose stop $1
    if [ $? != 0 ]; then
	echo "Build failed - Stop $1 container failed"
	exit -1;
    fi
}


############################## Update ####################################

function do_update {
    
    COMPONENT=$1

    if [ "$COMPONENT" == "service" ]; then
	SVC=$2
	cd $OMS_ROOT/services/${SVC}
	mvn clean package
	cp ./target/${SVC}.war ../target
	CONTAINER=$2-svc
	COMPONENT=services

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
}



############################## All ####################################

function do_all {
    do_start 
    i="0"
    RESPONSE=$(curl -s -I -X GET http://localhost:9000/GatewaySvc/status | head -n 1)
    echo "$i - $RESPONSE"
    while [ "$(echo $RESPONSE | cut -d$' ' -f2)" != "200" ] && [ $i -lt 120 ];
    do sleep 5;
       RESPONSE=$(curl -s -I -X GET http://localhost:9000/GatewaySvc/status | head -n 1)
       i=$[$i+5]
       echo "$i - $RESPONSE"
    done;
    sleep 5
    do_test
    do_stop
}


############################## Main ####################################

if [ "$TARGET" == "start" ]; then
    do_start $2
elif [ "$TARGET" == "update" ]; then
    do_update $2 $3
elif [ "$TARGET" == "test" ]; then
    do_test
elif [ "$TARGET" == "stop" ]; then
    do_stop $2
else
    read -p "Do you wish to do complete build: " yn
    case $yn in
        [Yy]* ) do_all; break;;
        [Nn]* ) exit;;
        * ) do_all;;
    esac    
fi
    
echo "-- Done --"
