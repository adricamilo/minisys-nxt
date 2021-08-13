#!/bin/bash

function wait_for_port {
    while ! nc -z localhost $1; do   
	sleep 1 # wait for 1/10 of the second before check again
	printf "."
    done
}

printf "" > pid.txt

PWD=`pwd`

function start_service {
    JAVA_DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:$3"
    echo java $JAVA_DEBUG_OPTIONS -Dserver.port=$2 -jar target/$1 &
    java $JAVA_DEBUG_OPTIONS -Dserver.port=$2 -jar target/$1 &
    wait_for_port $2

    
    #echo  "cd "${PWD}"; java $JAVA_DEBUG_OPTIONS -Dserver.port=$2 -jar target/$1" > /tmp/$1.command
    #chmod +x /tmp/$1.command
    #open /tmp/$1.command
    
    ps -eaf | grep java | grep $1 | awk '{ print $2}' >> pid.txt
}

start_service admin.war 8081 6001
start_service auth.war 8082 6002
start_service product.war 8083 6003
start_service order.war 8084 6004
start_service inventory.war 8085 6005
