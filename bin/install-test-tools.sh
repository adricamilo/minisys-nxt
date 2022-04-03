#!/bin/bash

function verify_success {
    if [ $1 != 0 ]; then
	echo "-- Installallation failed for $2 --"
	exit -1;
    else
	echo "-- Successfully installed $2 --"
    fi
}

sudo apt-get update

echo "-- Install jq --"
sudo apt-get install -y jq
verify_success $? jq

echo "-- Install python --"
sudo apt-get install -y python3-pip
verify_success $? python3-pip

sudo pip3 install "django>=3.2,<4" gunicorn python-json-logger 
verify_success $? "django and gunicorn"

sudo pip3 install opentracing jaeger-client django-prometheus
verify_success $? "jaeger tracing and prometheus"

echo "-- Install PostgreSQL --"
sudo apt-get install -y postgresql
verify_success $? postgresql

echo "-- Done --"
