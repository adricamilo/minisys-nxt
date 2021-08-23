#!/bin/bash

function wait_for_port {
    while ! nc -z localhost $1; do   
	sleep 1 # wait for 1/10 of the second before check again
	printf "."
    done
}

PWD=`pwd`

function start_service {
    JAVA_DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:$3"
    echo java $JAVA_DEBUG_OPTIONS -Dserver.port=$2 -jar target/$1 &
    java $JAVA_DEBUG_OPTIONS -Dserver.port=$2 -jar target/$1 &
    wait_for_port $2
}

if [ "$1" == "all" ]; then
    start_service admin.war 8081 6001
    start_service auth.war 8082 6002
    start_service product.war 8083 6003
    start_service order.war 8084 6004
    start_service inventory.war 8085 6005
elif [ "$1" == "admin" ]; then
    start_service admin.war 8081 6001
elif [ "$1" == "auth" ]; then
    start_service auth.war 8082 6002
elif [ "$1" == "product" ]; then
    start_service product.war 8083 6003
elif [ "$1" == "order" ]; then
    start_service order.war 8084 6004
elif [ "$1" == "inventory" ]; then
    start_service inventory.war 8085 6005
else
    echo "Incorrect service name"
fi
