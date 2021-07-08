#!/bin/bash

OMS_ROOT=`pwd`
cd $OMS_ROOT

TARGET=$1
SUB_TARGET=$2

declare -a app_containers=("web" "lb-web" "eureka" "gateway-svc" "lb-services" "cassandra" "postgres" "admin-svc" "auth-svc" "product-svc" "order-svc" "inventory-svc")
declare -a infra_containers=("elasticsearch" "kibana" "jaeger-collector" "jaeger-query" "jaeger-agent" "fluentd" "es-exporter" "pg-exporter" "prometheus")

echo "-- Start --"

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


if [ "$TARGET" == "start" ]; then
    do_start $SUB_TARGET
elif [ "$TARGET" == "test" ]; then
    do_test
elif [ "$TARGET" == "stop" ]; then
    do_stop $SUB_TARGET
else
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
fi
    
echo "-- Done --"
