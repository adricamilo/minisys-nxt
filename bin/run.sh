#!/bin/bash

OMS_ROOT=`pwd`/..
cd $OMS_ROOT

TARGET=$1

echo "-- Start --"

function do_start {
    echo "-- Run Containers --"
    cd $OMS_ROOT/docker
    do_start_infra
    do_start_app
    echo "-- Done --"
}

function do_start_infra {
    echo "-- Run Infra Containers --"
    declare -a arr=("elasticsearch" "kibana" "jaeger-collector" "jaeger-query" "jaeger-agent" "fluentd" "es-exporter" "pg-exporter" "prometheus")
    for container in "${arr[@]}"
    do
	do_start_one $container
	sleep 5s
    done    
    echo "-- Done --"
}

function do_start_app {
    echo "-- Run App Containers --"
    declare -a arr=("web" "lb-web" "eureka" "gateway-svc" "lb-rest" "cassandra" "postgres" "admin-svc" "auth-svc" "product-svc" "order-svc" "inventory-svc" "user-profile-svc")
    for container in "${arr[@]}"
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
    docker-compose stop
    if [ $? != 0 ]; then
	echo "Build failed - Cannot stop containers"
	exit -1;
    fi
    echo "-- Done --"
}

if [ "$TARGET" == "start" ]; then
    do_start
elif [ "$TARGET" == "test" ]; then
    do_test
elif [ "$TARGET" == "stop" ]; then
    do_stop
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
